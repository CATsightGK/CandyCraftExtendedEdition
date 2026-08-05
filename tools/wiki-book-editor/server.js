const fs = require("fs");
const childProcess = require("child_process");
const http = require("http");
const path = require("path");
const url = require("url");

const embeddedProjectRoot = "C:\\Users\\10424\\Documents\\Codex\\2026-05-26\\1-8-9forge-1-20-1forge\\CandyCraftExtendedEdition-clean";

function resolveProjectRoot() {
  for (const candidate of [process.env.CANDYCRAFT_ROOT, process.cwd(), path.dirname(process.execPath), embeddedProjectRoot, path.resolve(__dirname, "..", "..")].filter(Boolean)) {
    const resolved = path.resolve(candidate);
    if (fs.existsSync(path.join(resolved, "src", "main", "resources"))) return resolved;
  }
  return embeddedProjectRoot;
}

const root = resolveProjectRoot();
const assetsRoot = path.join(root, "src", "main", "resources", "assets", "candycraftmod");
const pagesPath = path.join(assetsRoot, "wiki", "pages.json");
const defaultPort = Number(process.env.PORT || 4313);

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

function openInDefaultBrowser(target) {
  if (process.env.CANDYCRAFT_NO_BROWSER === "1") return;
  const opener = process.platform === "win32"
    ? { command: "rundll32.exe", args: ["url.dll,FileProtocolHandler", target] }
    : process.platform === "darwin"
      ? { command: "open", args: [target] }
      : { command: "xdg-open", args: [target] };
  const child = childProcess.spawn(opener.command, opener.args, { detached: true, stdio: "ignore", windowsHide: true });
  child.unref();
}

function parseRegistryIds(javaFile, kind) {
  const text = readText(path.join(root, "src", "main", "java", "com", "valentin4311", "candycraftmod", "registry", javaFile));
  const found = [];
  const toolSets = new Map();
  for (const match of text.matchAll(/ToolSet\s+([A-Z0-9_]+)\s*=\s*registerToolSet\("([^"]+)"/g)) {
    toolSets.set(match[1], match[2]);
  }
  for (const match of text.matchAll(/public\s+static\s+final\s+RegistryObject<[^>]+>\s+([A-Z0-9_]+)\s*=\s*([^;]+);/g)) {
    const constant = match[1];
    const expr = match[2];
    if (expr.includes("registerNoItem(")) continue;
    const direct = expr.match(/\bregister(?:SweetscapeFood|SweetscapeSimple|SweetscapeTool|Food|Simple|ToolItem|Tool|PortItem|SeedItem|SpawnEgg|Armor|Record|Emblem|BlockItem)?\("([^"]+)"/);
    const toolAlias = expr.match(/^\s*([A-Z0-9_]+)\.(sword|shovel|pickaxe|axe|hoe)\s*$/);
    const id = direct ? direct[1] : toolAlias ? `${toolSets.get(toolAlias[1])}_${toolAlias[2]}` : null;
    if (!id || id.startsWith("undefined_")) continue;
    found.push({ id: `candycraftmod:${id}`, constant, kind });
  }
  return found;
}

function parseEntityIds() {
  const text = readText(path.join(root, "src", "main", "java", "com", "valentin4311", "candycraftmod", "registry", "CCEntityTypes.java"));
  const entities = [];
  for (const match of text.matchAll(/public\s+static\s+final\s+RegistryObject<EntityType<[^>]+>>\s+([A-Z0-9_]+)\s*=\s*[^"]*"([^"]+)"/g)) {
    entities.push({ id: `candycraftmod:${match[2]}`, constant: match[1], kind: "entity" });
  }
  return entities;
}

function modelPath(modelId, defaultFolder) {
  if (!modelId) return null;
  let namespace = "minecraft";
  let name = modelId;
  if (modelId.includes(":")) {
    [namespace, name] = modelId.split(":");
  }
  if (namespace !== "candycraftmod") return null;
  const rel = name.includes("/") ? name : `${defaultFolder}/${name}`;
  return path.join(assetsRoot, "models", `${rel}.json`);
}

function loadModel(modelId, defaultFolder, seen = new Set()) {
  const file = modelPath(modelId, defaultFolder);
  if (!file || seen.has(file)) return null;
  seen.add(file);
  const model = readJson(file, null);
  if (!model) return null;
  const parent = loadModel(model.parent, defaultFolder, seen);
  return {
    ...(parent || {}),
    ...model,
    textures: { ...((parent && parent.textures) || {}), ...(model.textures || {}) }
  };
}

function resolveTextureReference(value, textures) {
  let current = value;
  const seen = new Set();
  while (typeof current === "string" && current.startsWith("#")) {
    const key = current.slice(1);
    if (seen.has(key)) return null;
    seen.add(key);
    current = textures[key];
  }
  return current || null;
}

function textureUrl(textureId) {
  if (!textureId || textureId.startsWith("#")) return null;
  let id = textureId;
  if (id.startsWith("candycraftmod:")) id = id.slice("candycraftmod:".length);
  else if (id.includes(":")) return null;
  return `/asset/${id}.png`;
}

function previewFromModel(id, kind) {
  const name = id.split(":")[1];
  const model = loadModel(`candycraftmod:item/${name}`, "item");
  if (!model) return { texture: null, render: null };
  const textures = model.textures || {};
  const pick = keys => {
    for (const key of keys) {
      const texture = textureUrl(resolveTextureReference(textures[key], textures));
      if (texture) return texture;
    }
    return null;
  };
  const texture = pick(["layer0", "all", "texture", "side", "top", "end", "particle", "front", "0", "1", "2"]);
  const top = pick(["top", "end", "all", "texture", "side", "particle"]);
  const side = pick(["side", "all", "texture", "front", "end", "particle"]);
  const front = pick(["front", "side", "all", "texture", "end", "particle"]);
  const cubeLike = kind === "block" && !textures.layer0 && !textures.cross && top && side;
  return {
    texture,
    render: cubeLike ? { kind: "block", top, side, front: front || side } : { kind: "flat", texture }
  };
}

function displayName(id, zh, en) {
  const name = id.split(":")[1];
  return zh[`item.candycraftmod.${name}`] || zh[`block.candycraftmod.${name}`] ||
    en[`item.candycraftmod.${name}`] || en[`block.candycraftmod.${name}`] || name;
}

function defaultPages() {
  return {
    pages: [
      { key: "portal", elements: [{ type: "text", x: 146, y: 18, width: 116, key: "wiki.candycraftmod.portal.body", color: "#3F251F", scale: 1 }] }
    ]
  };
}

function buildState() {
  const zh = readJson(path.join(assetsRoot, "lang", "zh_cn.json"), {});
  const en = readJson(path.join(assetsRoot, "lang", "en_us.json"), {});
  const registry = new Map();
  for (const item of parseRegistryIds("CCItems.java", "item")) registry.set(item.id, item);
  for (const block of parseRegistryIds("CCBlocks.java", "block")) registry.set(block.id, block);
  const items = [...registry.values()].sort((a, b) => a.id.localeCompare(b.id)).map(entry => {
    const preview = previewFromModel(entry.id, entry.kind);
    return {
      ...entry,
      name: displayName(entry.id, zh, en),
      texture: preview.texture,
      render: preview.render
    };
  });
  const entities = parseEntityIds().sort((a, b) => a.id.localeCompare(b.id)).map(entry => ({
    ...entry,
    name: zh[`entity.candycraftmod.${entry.id.split(":")[1]}`] || en[`entity.candycraftmod.${entry.id.split(":")[1]}`] || entry.id.split(":")[1]
  }));
  return { pagesPath, pages: readJson(pagesPath, defaultPages()), items, entities };
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

function cleanElement(element) {
  const type = String(element.type || "text");
  const itemElement = type === "item" || type === "item_slot";
  const fixedSlot = itemElement && element.slot !== false;
  const clean = {
    type,
    x: Number(element.x) || 0,
    y: Number(element.y) || 0
  };
  for (const key of ["width", "height", "size"]) if (element[key] !== undefined) clean[key] = Number(element[key]) || 0;
  if (element.scale !== undefined) clean.scale = Number(element.scale) || 1;
  if (element.rotation !== undefined) clean.rotation = Number(element.rotation) || 0;
  if (element.color) clean.color = String(element.color);
  if (element.key) clean.key = String(element.key);
  if (element.text) clean.text = String(element.text);
  if (element.id) clean.id = String(element.id);
  if (element.slot !== undefined) clean.slot = Boolean(element.slot);
  if (element.theme) clean.theme = String(element.theme);
  if (element.asset) clean.asset = String(element.asset);
  if (fixedSlot) {
    clean.width = 38;
    clean.height = 38;
    clean.size = 38;
    clean.scale = 2;
    clean.slot = true;
  } else if (itemElement) {
    const fallback = Math.max(12, Number(element.size) || Math.round(16 * (Number(element.scale) || 2)));
    clean.width = Math.max(12, Number(element.width) || fallback);
    clean.height = Math.max(12, Number(element.height) || fallback);
    clean.slot = false;
  }
  return clean;
}

function cleanPages(input) {
  const legacyPage = Array.isArray(input.pages)
    ? input.pages.find(page => Number.isFinite(Number(page.previousButtonX)) || Number.isFinite(Number(page.nextButtonX))) || {}
    : {};
  const sourceButtons = input.pageButtons || {};
  const coordinate = (value, legacyValue, fallback) => Number.isFinite(Number(value))
    ? Number(value)
    : Number.isFinite(Number(legacyValue)) ? Number(legacyValue) : fallback;
  return {
    pageButtons: {
      previousX: coordinate(sourceButtons.previousX, legacyPage.previousButtonX, 16),
      previousY: coordinate(sourceButtons.previousY, legacyPage.previousButtonY, 168),
      nextX: coordinate(sourceButtons.nextX, legacyPage.nextButtonX, 264),
      nextY: coordinate(sourceButtons.nextY, legacyPage.nextButtonY, 168)
    },
    pages: Array.isArray(input.pages) ? input.pages.map((page, index) => ({
      key: String(page.key || `page_${index + 1}`),
      titleKey: page.titleKey ? String(page.titleKey) : undefined,
      title: page.title ? String(page.title) : undefined,
      titleX: Number(page.titleX) || 0,
      titleY: Number(page.titleY) || 0,
      elements: Array.isArray(page.elements) ? page.elements.map(cleanElement) : []
    })) : []
  };
}

const server = http.createServer((req, res) => {
  const parsed = url.parse(req.url, true);
  if (req.method === "GET" && parsed.pathname === "/") {
    const html = readText(path.join(__dirname, "index.html")).replace("__INITIAL_STATE__", JSON.stringify(buildState()).replace(/</g, "\\u003c"));
    res.writeHead(200, { "content-type": "text/html; charset=utf-8", "cache-control": "no-store" });
    return res.end(html);
  }
  if (req.method === "GET" && parsed.pathname === "/style.css") return serveFile(res, path.join(__dirname, "style.css"), "text/css; charset=utf-8");
  if (req.method === "GET" && parsed.pathname === "/api/state") return sendJson(res, buildState());
  if (req.method === "GET" && parsed.pathname.startsWith("/asset/")) {
    const rel = decodeURIComponent(parsed.pathname.slice("/asset/".length)).replace(/\//g, path.sep);
    const file = path.normalize(path.join(assetsRoot, "textures", rel));
    if (!file.startsWith(path.join(assetsRoot, "textures"))) {
      res.writeHead(400);
      return res.end("bad path");
    }
    return serveFile(res, file, "image/png");
  }
  if (req.method === "POST" && parsed.pathname === "/api/save") {
    let body = "";
    req.on("data", chunk => {
      body += chunk;
      if (body.length > 4_000_000) req.destroy();
    });
    req.on("end", () => {
      try {
        const clean = cleanPages(JSON.parse(body));
        fs.mkdirSync(path.dirname(pagesPath), { recursive: true });
        fs.writeFileSync(pagesPath, JSON.stringify(clean, null, 2) + "\n", "utf8");
        sendJson(res, { ok: true, pagesPath });
      } catch (error) {
        res.writeHead(400, { "content-type": "application/json; charset=utf-8" });
        res.end(JSON.stringify({ ok: false, error: String(error && error.message || error) }));
      }
    });
    return;
  }
  res.writeHead(404);
  res.end("not found");
});

function startServer(port = defaultPort) {
  return new Promise((resolve, reject) => {
    const onError = error => reject(error);
    server.once("error", onError);
    server.listen(port, "127.0.0.1", () => {
      server.removeListener("error", onError);
      const address = server.address();
      const actualPort = address && typeof address === "object" ? address.port : port;
      console.log(`CandyCraft guide book editor: http://127.0.0.1:${actualPort}`);
      resolve({ server, port: actualPort });
    });
  });
}

if (require.main === module) {
  const shouldOpenBrowser = !process.argv.includes("--no-browser");
  startServer()
    .then(({ port }) => {
      if (shouldOpenBrowser) openInDefaultBrowser(`http://127.0.0.1:${port}/?v=1`);
    })
    .catch(error => {
      if (error && error.code === "EADDRINUSE") {
        if (shouldOpenBrowser) openInDefaultBrowser(`http://127.0.0.1:${defaultPort}/?v=1`);
        return;
      }
      console.error(error);
      process.exitCode = 1;
    });
}

module.exports = { startServer };
