const fs = require("fs");
const http = require("http");
const path = require("path");
const url = require("url");

const root = path.resolve(__dirname, "..", "..");
const assetsRoot = path.join(root, "src", "main", "resources", "assets", "candycraftmod");
const dataRoot = path.join(root, "src", "main", "resources", "data", "candycraftmod");
const classicAssetsRoot = path.join(root, "src", "main", "resources", "resourcepacks", "candycraft_classic", "assets", "candycraftmod");
const zhPath = path.join(assetsRoot, "lang", "zh_cn.json");
const enPath = path.join(assetsRoot, "lang", "en_us.json");
const classicZhPath = path.join(classicAssetsRoot, "lang", "zh_cn.json");
const classicEnPath = path.join(classicAssetsRoot, "lang", "en_us.json");
const port = Number(process.env.PORT || 4312);

function readText(file) {
  return fs.existsSync(file) ? fs.readFileSync(file, "utf8") : "";
}

function readJson(file, fallback) {
  try {
    return JSON.parse(readText(file).replace(/^\uFEFF/, ""));
  } catch {
    return fallback;
  }
}

function writeJsonPreservingOrder(file, current, updates) {
  const clean = {};
  for (const key of Object.keys(current)) {
    clean[key] = Object.prototype.hasOwnProperty.call(updates, key) ? updates[key] : current[key];
  }
  const extraKeys = Object.keys(updates).filter(key => !Object.prototype.hasOwnProperty.call(clean, key)).sort();
  for (const key of extraKeys) {
    clean[key] = updates[key];
  }
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, JSON.stringify(clean, null, 2) + "\n", "utf8");
}

function walk(dir, out = []) {
  if (!fs.existsSync(dir)) {
    return out;
  }
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      walk(full, out);
    } else {
      out.push(full);
    }
  }
  return out;
}

function registryFile(name) {
  return path.join(root, "src", "main", "java", "com", "valentin4311", "candycraftmod", "registry", name);
}

function parseRegistryObjects(javaFile, kind) {
  const text = readText(registryFile(javaFile));
  const items = [];
  const regex = /public\s+static\s+final\s+RegistryObject<[\s\S]*?>>*\s+([A-Z0-9_]+)\s*=\s*([\s\S]*?);/g;
  let match;
  while ((match = regex.exec(text))) {
    const constant = match[1];
    const expr = match[2];
    let name = expr.match(/\bregister(?:NoItem|Food|Simple|Tool|PortItem|SeedItem|SpawnEgg|Armor|Record|Emblem|BlockItem)?\("([^"]+)"/);
    if (!name && kind === "entity") {
      name = expr.match(/\b(?:basicZombie|basicSpider|basicSlime|ENTITY_TYPES\.register)\("([^"]+)"/);
    }
    if (!name) {
      continue;
    }
    items.push({
      id: `candycraftmod:${name[1]}`,
      key: `${kind === "entity" ? "entity" : kind}.candycraftmod.${name[1]}`,
      constant,
      kind,
      source: javaFile,
      noItem: expr.includes("registerNoItem(")
    });
  }
  return items;
}

function discoverAdvancementRows(zh, en) {
  const roots = [
    { dir: path.join(dataRoot, "advancements"), source: "data" },
    { dir: path.join(assetsRoot, "advancements"), source: "assets" }
  ];
  const seen = new Map();
  for (const rootInfo of roots) {
    for (const file of walk(rootInfo.dir).filter(file => file.endsWith(".json"))) {
      const json = readJson(file, null);
      const display = json && json.display;
      if (!display) {
        continue;
      }
      const rel = path.relative(rootInfo.dir, file).replace(/\\/g, "/").replace(/\.json$/, "");
      for (const part of ["title", "description"]) {
        const value = display[part];
        const key = value && typeof value === "object" && value.translate
          ? value.translate
          : `advancements.candycraft.${rel}.${part === "title" ? "title" : "desc"}`;
        const mapKey = `${key}::${part}`;
        const current = seen.get(mapKey);
        const sourceName = `${rootInfo.source}/${rel}.json`;
        if (current) {
          current.source += `, ${sourceName}`;
          continue;
        }
        seen.set(mapKey, makeRow({
          category: "advancement",
          id: rel,
          key,
          source: sourceName,
          label: part === "title" ? "进度标题" : "进度描述",
          zh,
          en
        }));
      }
    }
  }
  return [...seen.values()].sort((a, b) => a.id.localeCompare(b.id) || a.key.localeCompare(b.key));
}

function makeRow({ category, id, key, source, label, zh, en }) {
  return {
    category,
    id,
    key,
    source,
    label,
    zh: zh[key] || "",
    en: en[key] || "",
    missingZh: !Object.prototype.hasOwnProperty.call(zh, key) || !zh[key],
    missingEn: !Object.prototype.hasOwnProperty.call(en, key) || !en[key]
  };
}

function niceLabel(kind) {
  return {
    block: "方块",
    entity: "实体",
    advancement: "进度",
    text: "文本"
  }[kind] || kind;
}

function buildState() {
  const zh = readJson(zhPath, {});
  const en = readJson(enPath, {});
  const rows = [];
  const knownKeys = new Set();

  for (const block of [
    ...parseRegistryObjects("CCBlocks.java", "block"),
    ...parseRegistryObjects("CCSweetscapeBlocks.java", "block")
  ]) {
    const row = makeRow({ category: "block", id: block.id, key: block.key, source: block.source, label: niceLabel("block"), zh, en });
    rows.push(row);
    knownKeys.add(row.key);
  }

  for (const entity of parseRegistryObjects("CCEntityTypes.java", "entity")) {
    const row = makeRow({ category: "entity", id: entity.id, key: entity.key, source: entity.source, label: niceLabel("entity"), zh, en });
    rows.push(row);
    knownKeys.add(row.key);
  }

  const advancementRows = discoverAdvancementRows(zh, en);
  for (const row of advancementRows) {
    rows.push(row);
    knownKeys.add(row.key);
  }

  const allLangKeys = new Set([...Object.keys(zh), ...Object.keys(en)]);
  for (const key of [...allLangKeys].sort()) {
    if (knownKeys.has(key)) {
      continue;
    }
    let category = "text";
    if (key.startsWith("item.candycraftmod.")) {
      category = "item";
    } else if (key.startsWith("block.candycraftmod.")) {
      category = "block";
    } else if (key.startsWith("entity.candycraftmod.")) {
      category = "entity";
    } else if (key.startsWith("advancements.")) {
      category = "advancement";
    }
    rows.push(makeRow({
      category,
      id: key.replace(/^(item|block|entity)\.candycraftmod\./, "candycraftmod:"),
      key,
      source: "lang",
      label: niceLabel(category),
      zh,
      en
    }));
  }

  rows.sort((a, b) => {
    const order = { block: 0, entity: 1, advancement: 2, item: 3, text: 4 };
    return (order[a.category] ?? 9) - (order[b.category] ?? 9) || a.key.localeCompare(b.key);
  });

  return {
    rows,
    counts: rows.reduce((acc, row) => {
      acc[row.category] = (acc[row.category] || 0) + 1;
      return acc;
    }, {}),
    paths: {
      zh: zhPath,
      en: enPath,
      classicZh: classicZhPath,
      classicEn: classicEnPath
    }
  };
}

function sendJson(res, data) {
  res.writeHead(200, { "content-type": "application/json; charset=utf-8", "cache-control": "no-store" });
  res.end(JSON.stringify(data, null, 2));
}

function serveFile(res, file, type) {
  if (!fs.existsSync(file)) {
    res.writeHead(404);
    res.end("not found");
    return;
  }
  res.writeHead(200, { "content-type": type, "cache-control": "no-store" });
  res.end(fs.readFileSync(file));
}

const server = http.createServer((req, res) => {
  const parsed = url.parse(req.url, true);
  if (req.method === "GET" && parsed.pathname === "/") {
    return serveFile(res, path.join(__dirname, "index.html"), "text/html; charset=utf-8");
  }
  if (req.method === "GET" && parsed.pathname === "/style.css") {
    return serveFile(res, path.join(__dirname, "style.css"), "text/css; charset=utf-8");
  }
  if (req.method === "GET" && parsed.pathname === "/api/state") {
    return sendJson(res, buildState());
  }
  if (req.method === "POST" && parsed.pathname === "/api/save") {
    let body = "";
    req.on("data", chunk => {
      body += chunk;
      if (body.length > 4_000_000) {
        req.destroy();
      }
    });
    req.on("end", () => {
      try {
        const data = JSON.parse(body);
        if (!Array.isArray(data.rows)) {
          throw new Error("rows must be an array");
        }
        const zh = readJson(zhPath, {});
        const en = readJson(enPath, {});
        const zhUpdates = {};
        const enUpdates = {};
        for (const row of data.rows) {
          if (!row || typeof row.key !== "string") {
            continue;
          }
          zhUpdates[row.key] = typeof row.zh === "string" ? row.zh : "";
          enUpdates[row.key] = typeof row.en === "string" ? row.en : "";
        }
        writeJsonPreservingOrder(zhPath, zh, zhUpdates);
        writeJsonPreservingOrder(enPath, en, enUpdates);
        if (data.syncClassic) {
          writeJsonPreservingOrder(classicZhPath, readJson(classicZhPath, {}), zhUpdates);
          writeJsonPreservingOrder(classicEnPath, readJson(classicEnPath, {}), enUpdates);
        }
        return sendJson(res, { ok: true, changed: data.rows.length });
      } catch (error) {
        res.writeHead(400, { "content-type": "application/json; charset=utf-8" });
        return res.end(JSON.stringify({ ok: false, error: String(error && error.message || error) }));
      }
    });
    return;
  }
  res.writeHead(404);
  res.end("not found");
});

server.listen(port, "127.0.0.1", () => {
  console.log(`CandyCraft localization editor: http://127.0.0.1:${port}`);
});
