const fs = require("fs");
const http = require("http");
const path = require("path");
const url = require("url");
const zlib = require("zlib");

const port = Number(process.env.PORT || 4327);
const embeddedProjectRoot = "C:\\Users\\10424\\Documents\\Codex\\2026-05-26\\1-8-9forge-1-20-1forge\\CandyCraftExtendedEdition-clean";

function projectRoot() {
  const candidates = [process.env.CANDYCRAFT_ROOT, process.cwd(), embeddedProjectRoot, path.resolve(__dirname, "..", "..")].filter(Boolean);
  return candidates.map(value => path.resolve(value)).find(value => fs.existsSync(path.join(value, "src", "main", "resources"))) || path.resolve(candidates[0]);
}

const root = projectRoot();
const resources = path.join(root, "src", "main", "resources");
const dataRoot = path.join(resources, "data", "candycraftmod");
const chestRoot = path.join(dataRoot, "loot_tables", "chests");
const assetsRoot = path.join(resources, "assets");
const candyAssets = path.join(assetsRoot, "candycraftmod");

function readText(file) {
  return fs.existsSync(file) ? fs.readFileSync(file, "utf8").replace(/^\uFEFF/, "") : "";
}

function readJson(file, fallback = null) {
  try { return JSON.parse(readText(file)); } catch { return fallback; }
}

function walk(directory, extension = null) {
  if (!fs.existsSync(directory)) return [];
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry => {
    const file = path.join(directory, entry.name);
    if (entry.isDirectory()) return walk(file, extension);
    return !extension || entry.name.endsWith(extension) ? [file] : [];
  });
}

function slash(value) {
  return value.split(path.sep).join("/");
}

function tableIdFromFile(file) {
  return slash(path.relative(chestRoot, file)).replace(/\.json$/i, "");
}

function validTableId(value) {
  return /^[a-z0-9_.-]+(?:\/[a-z0-9_.-]+)*$/.test(String(value || "")) && !String(value).split("/").includes("..");
}

function tableFile(id) {
  if (!validTableId(id)) return null;
  const base = path.resolve(chestRoot);
  const file = path.resolve(base, `${id}.json`);
  return file.startsWith(`${base}${path.sep}`) ? file : null;
}

function resourceId(value, fallbackNamespace = "candycraftmod") {
  const source = String(value || "");
  const separator = source.indexOf(":");
  return separator >= 0
    ? { namespace: source.slice(0, separator), name: source.slice(separator + 1) }
    : { namespace: fallbackNamespace, name: source };
}

function validResourceId(value) {
  return /^[a-z0-9_.-]+:[a-z0-9_./-]+$/.test(String(value || ""));
}

function safeAssetFile(namespace, category, name, extension) {
  if (!/^[a-z0-9_.-]+$/.test(namespace) || !/^[a-z0-9_./-]+$/.test(name)) return null;
  const base = path.resolve(assetsRoot, namespace, category);
  const file = path.resolve(base, `${name}${extension}`);
  return file.startsWith(`${base}${path.sep}`) && fs.existsSync(file) ? file : null;
}

function resolveModel(modelId, fallbackNamespace, seen = new Set()) {
  const parsed = resourceId(modelId, fallbackNamespace);
  const key = `${parsed.namespace}:${parsed.name}`;
  if (seen.has(key)) return { textures: {}, namespace: parsed.namespace };
  seen.add(key);
  const file = safeAssetFile(parsed.namespace, "models", parsed.name, ".json");
  if (!file) return { textures: {}, namespace: parsed.namespace };
  const model = readJson(file, {}) || {};
  let inherited = { textures: {}, namespace: parsed.namespace };
  if (model.parent && !String(model.parent).startsWith("builtin/")) inherited = resolveModel(model.parent, parsed.namespace, seen);
  return { textures: { ...inherited.textures, ...(model.textures || {}) }, namespace: parsed.namespace };
}

function textureReference(value, textures) {
  let current = value;
  const seen = new Set();
  while (typeof current === "string" && current.startsWith("#") && !seen.has(current)) {
    seen.add(current);
    current = textures[current.slice(1)];
  }
  return typeof current === "string" && !current.startsWith("#") ? current : null;
}

function resolveItemIcon(id) {
  const parsed = resourceId(id, "minecraft");
  const model = resolveModel(`${parsed.namespace}:item/${parsed.name}`, parsed.namespace);
  const keys = [...new Set(["layer0", "particle", "all", "top", "side", "end", ...Object.keys(model.textures)])];
  for (const key of keys) {
    const reference = textureReference(model.textures[key], model.textures);
    if (!reference) continue;
    const texture = resourceId(reference, model.namespace);
    const file = safeAssetFile(texture.namespace, "textures", texture.name, ".png");
    if (file) return file;
  }
  for (const folder of ["item", "items", "block", "blocks"]) {
    const direct = safeAssetFile(parsed.namespace, "textures", `${folder}/${parsed.name}`, ".png");
    if (direct) return direct;
  }
  return null;
}

function parseRegistryIds(fileName) {
  const file = path.join(root, "src", "main", "java", "com", "valentin4311", "candycraftmod", "registry", fileName);
  const text = readText(file);
  const ids = new Set();
  const explicit = /\bregister(?:SweetscapeFood|SweetscapeSimple|SweetscapeTool|Food|Simple|ToolItem|Tool|PortItem|SeedItem|SpawnEgg|Armor|Record|Emblem|BlockItem|NoItem)?\("([a-z0-9_./-]+)"/g;
  let match;
  while ((match = explicit.exec(text))) ids.add(`candycraftmod:${match[1]}`);
  const toolSets = new Map();
  const setPattern = /ToolSet\s+([A-Z0-9_]+)\s*=\s*registerToolSet\("([a-z0-9_./-]+)"/g;
  while ((match = setPattern.exec(text))) toolSets.set(match[1], match[2]);
  const aliases = /RegistryObject<[^>]+>\s+[A-Z0-9_]+\s*=\s*([A-Z0-9_]+)\.(sword|shovel|pickaxe|axe|hoe)\s*;/g;
  while ((match = aliases.exec(text))) if (toolSets.has(match[1])) ids.add(`candycraftmod:${toolSets.get(match[1])}_${match[2]}`);
  return ids;
}

function collectLootItemIds() {
  const ids = new Set();
  function visit(value, key = "") {
    if (Array.isArray(value)) return value.forEach(entry => visit(entry, key));
    if (!value || typeof value !== "object") return;
    for (const [childKey, child] of Object.entries(value)) {
      if ((childKey === "name" || childKey === "item") && typeof child === "string" && validResourceId(child)) ids.add(child);
      visit(child, childKey);
    }
  }
  for (const file of walk(path.join(dataRoot, "loot_tables"), ".json")) visit(readJson(file, {}));
  return ids;
}

function localizedItems() {
  const zh = readJson(path.join(candyAssets, "lang", "zh_cn.json"), {}) || {};
  const en = readJson(path.join(candyAssets, "lang", "en_us.json"), {}) || {};
  const ids = new Set([...parseRegistryIds("CCItems.java"), ...parseRegistryIds("CCBlocks.java"), ...collectLootItemIds()]);
  return [...ids].sort().map(id => {
    const parsed = resourceId(id, "minecraft");
    let name = id;
    if (parsed.namespace === "candycraftmod") {
      name = zh[`item.candycraftmod.${parsed.name}`] || zh[`block.candycraftmod.${parsed.name}`]
        || en[`item.candycraftmod.${parsed.name}`] || en[`block.candycraftmod.${parsed.name}`] || parsed.name;
    }
    return { id, name, icon: resolveItemIcon(id) ? `/api/item-icon?id=${encodeURIComponent(id)}` : null };
  });
}

class NbtReader {
  constructor(buffer) { this.buffer = buffer; this.offset = 0; }
  ensure(bytes) { if (this.offset + bytes > this.buffer.length) throw new Error("NBT data ended unexpectedly"); }
  byte() { this.ensure(1); return this.buffer.readInt8(this.offset++); }
  ubyte() { this.ensure(1); return this.buffer.readUInt8(this.offset++); }
  short() { this.ensure(2); const value = this.buffer.readInt16BE(this.offset); this.offset += 2; return value; }
  ushort() { this.ensure(2); const value = this.buffer.readUInt16BE(this.offset); this.offset += 2; return value; }
  int() { this.ensure(4); const value = this.buffer.readInt32BE(this.offset); this.offset += 4; return value; }
  skip(bytes) { this.ensure(bytes); this.offset += bytes; }
  string() { const length = this.ushort(); this.ensure(length); const value = this.buffer.toString("utf8", this.offset, this.offset + length); this.offset += length; return value; }
  payload(type) {
    switch (type) {
      case 1: return this.byte();
      case 2: return this.short();
      case 3: return this.int();
      case 4: this.skip(8); return null;
      case 5: this.skip(4); return null;
      case 6: this.skip(8); return null;
      case 7: { const length = this.int(); this.skip(Math.max(0, length)); return null; }
      case 8: return this.string();
      case 9: {
        const childType = this.ubyte();
        const length = this.int();
        if (length < 0 || length > 1000000) throw new Error("Invalid NBT list length");
        return Array.from({ length }, () => this.payload(childType));
      }
      case 10: {
        const value = {};
        while (true) {
          const childType = this.ubyte();
          if (childType === 0) return value;
          const name = this.string();
          value[name] = this.payload(childType);
        }
      }
      case 11: { const length = this.int(); this.skip(Math.max(0, length) * 4); return null; }
      case 12: { const length = this.int(); this.skip(Math.max(0, length) * 8); return null; }
      default: throw new Error(`Unsupported NBT tag ${type}`);
    }
  }
  root() {
    const type = this.ubyte();
    if (type === 0) return {};
    this.string();
    return this.payload(type);
  }
}

function parseNbt(file) {
  let buffer = fs.readFileSync(file);
  if (buffer.length >= 2 && buffer[0] === 0x1f && buffer[1] === 0x8b) buffer = zlib.gunzipSync(buffer);
  return new NbtReader(buffer).root();
}

function collectNbtLootTables(value, out = new Set()) {
  if (Array.isArray(value)) {
    value.forEach(child => collectNbtLootTables(child, out));
  } else if (value && typeof value === "object") {
    for (const [key, child] of Object.entries(value)) {
      if (key === "LootTable" && typeof child === "string") out.add(child);
      collectNbtLootTables(child, out);
    }
  }
  return out;
}

function sourceReference(file, tableId, kind, label, detail = "") {
  const text = readText(file);
  const needle = `chests/${tableId}`;
  const index = text.indexOf(needle);
  const line = index < 0 ? null : text.slice(0, index).split(/\r?\n/).length;
  return { kind, label, detail, source: slash(path.relative(root, file)), line };
}

function structureReferences(tableIds) {
  const references = Object.fromEntries(tableIds.map(id => [id, []]));
  const structures = path.join(dataRoot, "structures");
  for (const file of walk(structures, ".nbt")) {
    try {
      for (const resource of collectNbtLootTables(parseNbt(file))) {
        const prefix = "candycraftmod:chests/";
        if (!resource.startsWith(prefix)) continue;
        const id = resource.slice(prefix.length);
        if (!references[id]) references[id] = [];
        references[id].push({
          kind: "template",
          label: `candycraftmod:${slash(path.relative(structures, file)).replace(/\.nbt$/i, "")}`,
          detail: "结构模板中的箱子 NBT",
          source: slash(path.relative(root, file)),
          line: null
        });
      }
    } catch (error) {
      console.warn(`Could not inspect structure ${file}: ${error.message}`);
    }
  }

  const javaRoot = path.join(root, "src", "main", "java");
  const pattern = /(?:ResourceLocation\s+([A-Z0-9_]+)\s*=\s*)?new ResourceLocation\(CandyCraft\.MODID,\s*"chests\/([a-z0-9_./-]+)"\)/g;
  for (const file of walk(javaRoot, ".java")) {
    const text = readText(file);
    let match;
    while ((match = pattern.exec(text))) {
      const id = match[2];
      if (!references[id]) references[id] = [];
      const className = path.basename(file, ".java");
      references[id].push(sourceReference(file, id, "code", `candycraftmod:${id}`, `${className}${match[1] ? ` · ${match[1]}` : ""}`));
    }
  }

  const worldgenStructures = path.join(dataRoot, "worldgen", "structure");
  for (const file of walk(worldgenStructures, ".json")) {
    const json = readJson(file, {});
    const feature = String(json.feature || "");
    const structureId = slash(path.relative(worldgenStructures, file)).replace(/\.json$/i, "");
    const candidate = feature.startsWith("candycraftmod:") ? feature.slice("candycraftmod:".length) : structureId;
    if (!references[candidate]) continue;
    references[candidate].push({
      kind: "worldgen",
      label: `candycraftmod:${structureId}`,
      detail: "世界生成结构",
      source: slash(path.relative(root, file)),
      line: null
    });
  }

  for (const entries of Object.values(references)) {
    const seen = new Set();
    for (let index = entries.length - 1; index >= 0; index--) {
      const key = `${entries[index].kind}:${entries[index].source}:${entries[index].label}`;
      if (seen.has(key)) entries.splice(index, 1); else seen.add(key);
    }
  }
  return references;
}

function loadTables() {
  return walk(chestRoot, ".json").map(file => {
    const json = readJson(file, null);
    return { id: tableIdFromFile(file), file: slash(path.relative(root, file)), json, error: json ? null : "JSON 无法解析" };
  }).sort((a, b) => a.id.localeCompare(b.id));
}

function buildState() {
  const tables = loadTables();
  return {
    root,
    tables,
    items: localizedItems(),
    references: structureReferences(tables.map(table => table.id))
  };
}

function validateTable(id, json) {
  if (!validTableId(id)) throw new Error("ID 只能使用小写字母、数字、下划线、点、短横线和目录斜杠");
  if (!json || typeof json !== "object" || Array.isArray(json)) throw new Error("战利品表必须是 JSON 对象");
  if (json.type !== "minecraft:chest") throw new Error("箱子战利品表 type 必须为 minecraft:chest");
  if (!Array.isArray(json.pools) || json.pools.length === 0) throw new Error("至少需要一个奖池");
  json.pools.forEach((pool, poolIndex) => {
    if (!pool || typeof pool !== "object" || !Array.isArray(pool.entries) || pool.entries.length === 0) throw new Error(`奖池 ${poolIndex + 1} 至少需要一个条目`);
    const rolls = pool.rolls;
    const validRolls = Number.isFinite(Number(rolls)) || (rolls && Number.isFinite(Number(rolls.min)) && Number.isFinite(Number(rolls.max)));
    if (!validRolls) throw new Error(`奖池 ${poolIndex + 1} 的抽取次数无效`);
    pool.entries.forEach((entry, entryIndex) => {
      if (!entry || !["minecraft:item", "minecraft:empty"].includes(entry.type)) throw new Error(`奖池 ${poolIndex + 1} 条目 ${entryIndex + 1} 类型不受支持`);
      if (entry.type === "minecraft:item" && !validResourceId(entry.name)) throw new Error(`奖池 ${poolIndex + 1} 条目 ${entryIndex + 1} 的物品 ID 无效`);
      if (!Number.isFinite(Number(entry.weight ?? 1)) || Number(entry.weight ?? 1) < 0) throw new Error(`奖池 ${poolIndex + 1} 条目 ${entryIndex + 1} 的权重无效`);
    });
  });
}

function sendJson(res, status, data) {
  res.writeHead(status, { "content-type": "application/json; charset=utf-8", "cache-control": "no-store" });
  res.end(JSON.stringify(data, null, 2));
}

function serveFile(res, file, type) {
  if (!fs.existsSync(file)) { res.writeHead(404); res.end("not found"); return; }
  res.writeHead(200, { "content-type": type, "cache-control": "no-store" });
  res.end(fs.readFileSync(file));
}

function readBody(req) {
  return new Promise((resolve, reject) => {
    let body = "";
    req.setEncoding("utf8");
    req.on("data", chunk => {
      body += chunk;
      if (body.length > 2 * 1024 * 1024) reject(new Error("请求内容过大"));
    });
    req.on("end", () => {
      try { resolve(JSON.parse(body || "{}")); } catch { reject(new Error("请求 JSON 无效")); }
    });
    req.on("error", reject);
  });
}

async function handleApi(req, res, pathname, query) {
  if (req.method === "GET" && pathname === "/api/state") return sendJson(res, 200, buildState());
  if (req.method === "GET" && pathname === "/api/item-icon") {
    const file = resolveItemIcon(String(query.id || ""));
    if (!file) return sendJson(res, 404, { error: "没有可用图标" });
    return serveFile(res, file, "image/png");
  }
  if (req.method === "POST" && pathname === "/api/save") {
    const body = await readBody(req);
    const id = String(body.id || "").trim();
    const previousId = body.previousId == null ? null : String(body.previousId);
    validateTable(id, body.json);
    const target = tableFile(id);
    const previous = previousId ? tableFile(previousId) : null;
    if (!target || (previousId && !previous)) throw new Error("战利品表路径无效");
    if (fs.existsSync(target) && path.resolve(target) !== path.resolve(previous || "")) throw new Error(`战利品表 ${id} 已存在`);
    fs.mkdirSync(path.dirname(target), { recursive: true });
    fs.writeFileSync(target, `${JSON.stringify(body.json, null, 2)}\n`, "utf8");
    if (previous && path.resolve(previous) !== path.resolve(target) && fs.existsSync(previous)) fs.unlinkSync(previous);
    return sendJson(res, 200, { ok: true, id, file: slash(path.relative(root, target)) });
  }
  if (req.method === "POST" && pathname === "/api/delete") {
    const body = await readBody(req);
    const file = tableFile(String(body.id || ""));
    if (!file || !fs.existsSync(file)) throw new Error("找不到要删除的战利品表");
    fs.unlinkSync(file);
    return sendJson(res, 200, { ok: true });
  }
  return sendJson(res, 404, { error: "未知接口" });
}

const contentTypes = { ".html": "text/html; charset=utf-8", ".css": "text/css; charset=utf-8", ".js": "text/javascript; charset=utf-8" };
const server = http.createServer(async (req, res) => {
  const parsed = url.parse(req.url, true);
  try {
    if (parsed.pathname.startsWith("/api/")) return await handleApi(req, res, parsed.pathname, parsed.query);
    const names = { "/": "index.html", "/index.html": "index.html", "/style.css": "style.css", "/app.js": "app.js" };
    const name = names[parsed.pathname];
    if (!name) { res.writeHead(404); res.end("not found"); return; }
    serveFile(res, path.join(__dirname, name), contentTypes[path.extname(name)]);
  } catch (error) {
    sendJson(res, 400, { error: error.message || String(error) });
  }
});

if (require.main === module) {
  server.listen(port, "127.0.0.1", () => console.log(`CandyCraft loot table editor: http://127.0.0.1:${port}`));
}

module.exports = { buildState, collectNbtLootTables, parseNbt, structureReferences, validateTable, server };
