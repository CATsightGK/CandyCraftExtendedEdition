const fs = require("fs");
const childProcess = require("child_process");
const http = require("http");
const path = require("path");

const embeddedProjectRoot = "C:\\Users\\10424\\Documents\\Codex\\2026-05-26\\1-8-9forge-1-20-1forge\\CandyCraftExtendedEdition-clean";
const namespace = "candycraftmod";
const defaultPort = Number(process.env.PORT || 4312);

function resolveProjectRoot() {
  const candidates = [process.env.CANDYCRAFT_ROOT, process.cwd(), path.dirname(process.execPath), embeddedProjectRoot, path.resolve(__dirname, "..", "..")].filter(Boolean);
  for (const candidate of candidates) {
    const resolved = path.resolve(candidate);
    if (fs.existsSync(path.join(resolved, "src", "main", "resources", "data", namespace))) return resolved;
  }
  return path.resolve(candidates[0]);
}

const root = resolveProjectRoot();
const resourcesRoot = path.join(root, "src", "main", "resources");
const assetsRoot = path.join(resourcesRoot, "assets", namespace);
const advancementsRoot = path.join(resourcesRoot, "data", namespace, "advancements");
const layoutPath = path.join(root, "tools", "advancement-editor", "layout.json");
const zhPath = path.join(assetsRoot, "lang", "zh_cn.json");
const enPath = path.join(assetsRoot, "lang", "en_us.json");

function readText(file) { return fs.existsSync(file) ? fs.readFileSync(file, "utf8") : ""; }
function readJson(file, fallback) {
  try { return JSON.parse(readText(file).replace(/^\uFEFF/, "")); } catch { return fallback; }
}
function writeJsonAtomic(file, value) {
  fs.mkdirSync(path.dirname(file), { recursive: true });
  const temp = `${file}.tmp-${process.pid}`;
  fs.writeFileSync(temp, JSON.stringify(value, null, 2) + "\n", "utf8");
  fs.renameSync(temp, file);
}

function openInDefaultBrowser(target) {
  if (process.env.CANDYCRAFT_NO_BROWSER === "1") return;
  const opener = process.platform === "win32"
    ? { command: "rundll32.exe", args: ["url.dll,FileProtocolHandler", target] }
    : process.platform === "darwin" ? { command: "open", args: [target] } : { command: "xdg-open", args: [target] };
  const child = childProcess.spawn(opener.command, opener.args, { detached: true, stdio: "ignore", windowsHide: true });
  child.unref();
}

function parseRegistryIds(javaFile, kind) {
  const file = path.join(root, "src", "main", "java", "com", "valentin4311", "candycraftmod", "registry", javaFile);
  const text = readText(file);
  const ids = [];
  const toolSets = new Map();
  for (const match of text.matchAll(/ToolSet\s+([A-Z0-9_]+)\s*=\s*registerToolSet\("([^"]+)"/g)) toolSets.set(match[1], match[2]);
  for (const match of text.matchAll(/public\s+static\s+final\s+RegistryObject<[^>]+>\s+([A-Z0-9_]+)\s*=\s*([^;]+);/g)) {
    const expr = match[2];
    const named = expr.match(/\bregister(?:SweetscapeFood|SweetscapeSimple|SweetscapeTool|Food|Simple|ToolItem|Tool|PortItem|SeedItem|SpawnEgg|Armor|Record|Emblem|BlockItem)?\("([^"]+)"/);
    const alias = expr.match(/^\s*([A-Z0-9_]+)\.(sword|shovel|pickaxe|axe|hoe)\s*$/);
    if ((!named && !alias) || expr.includes("registerNoItem(")) continue;
    const name = named ? named[1] : `${toolSets.get(alias[1])}_${alias[2]}`;
    if (name && !name.startsWith("undefined_")) ids.push({ id: `${namespace}:${name}`, kind });
  }
  return ids;
}

function modelFile(modelId, defaultFolder) {
  if (!modelId) return null;
  let ns = "minecraft";
  let name = modelId;
  if (modelId.includes(":")) [ns, name] = modelId.split(":", 2);
  if (ns !== namespace) return null;
  const rel = name.includes("/") ? name : `${defaultFolder}/${name}`;
  return path.join(assetsRoot, "models", `${rel}.json`);
}

function loadModel(modelId, defaultFolder, seen = new Set()) {
  const file = modelFile(modelId, defaultFolder);
  if (!file || seen.has(file)) return null;
  seen.add(file);
  const model = readJson(file, null);
  if (!model) return null;
  const parent = loadModel(model.parent, defaultFolder, seen);
  return { ...parent, ...model, textures: { ...((parent && parent.textures) || {}), ...(model.textures || {}) } };
}

function resolveTexture(value, textures) {
  const seen = new Set();
  let current = value;
  while (typeof current === "string" && current.startsWith("#")) {
    const key = current.slice(1);
    if (seen.has(key)) return null;
    seen.add(key);
    current = textures[key];
  }
  if (!current || current.includes(":") && !current.startsWith(`${namespace}:`)) return null;
  return current.startsWith(`${namespace}:`) ? current.slice(namespace.length + 1) : current;
}

function textureFromModel(id) {
  if (!id.startsWith(`${namespace}:`)) return null;
  const name = id.slice(namespace.length + 1);
  const model = loadModel(`${namespace}:item/${name}`, "item") || loadModel(`${namespace}:block/${name}`, "block");
  if (!model) return null;
  const textures = model.textures || {};
  for (const key of ["layer0", "all", "texture", "side", "top", "end", "particle", "front", "0", "1", "2"]) {
    const texture = resolveTexture(textures[key], textures);
    if (texture) return `/asset/${texture}.png`;
  }
  return null;
}

function componentKey(component) { return component && typeof component === "object" ? component.translate || "" : ""; }
function componentText(component, lang) {
  if (typeof component === "string") return component;
  if (!component || typeof component !== "object") return "";
  return component.text || lang[component.translate] || component.translate || "";
}

function buildItems(zh, en, advancements) {
  const registry = new Map();
  for (const entry of [...parseRegistryIds("CCItems.java", "item"), ...parseRegistryIds("CCBlocks.java", "block")]) registry.set(entry.id, entry);
  for (const advancement of advancements) {
    const id = advancement.data.display && advancement.data.display.icon && advancement.data.display.icon.item;
    if (id && !registry.has(id)) registry.set(id, { id, kind: "icon" });
  }
  return [...registry.values()].map(entry => {
    const name = entry.id.split(":")[1];
    return {
      ...entry,
      name: zh[`item.${namespace}.${name}`] || zh[`block.${namespace}.${name}`] || en[`item.${namespace}.${name}`] || en[`block.${namespace}.${name}`] || name,
      texture: textureFromModel(entry.id)
    };
  }).sort((a, b) => a.name.localeCompare(b.name, "zh-Hans-CN"));
}

function buildState() {
  const zh = readJson(zhPath, {});
  const en = readJson(enPath, {});
  const savedLayout = readJson(layoutPath, {});
  const advancements = fs.existsSync(advancementsRoot) ? fs.readdirSync(advancementsRoot, { withFileTypes: true })
    .filter(entry => entry.isFile() && entry.name.endsWith(".json"))
    .map(entry => {
      const id = entry.name.slice(0, -5);
      const data = readJson(path.join(advancementsRoot, entry.name), {});
      const display = data.display || {};
      return {
        id,
        data,
        titleKey: componentKey(display.title) || `advancements.candycraft.${id}.title`,
        descriptionKey: componentKey(display.description) || `advancements.candycraft.${id}.desc`,
        titleZh: componentText(display.title, zh),
        titleEn: componentText(display.title, en),
        descriptionZh: componentText(display.description, zh),
        descriptionEn: componentText(display.description, en),
        position: savedLayout[id] || null
      };
    }).sort((a, b) => a.id.localeCompare(b.id)) : [];
  return { root, advancementsRoot, advancements, items: buildItems(zh, en, advancements) };
}

function validateNode(node, ids) {
  if (!node || !/^[a-z0-9_.\/-]+$/.test(node.id || "") || node.id.includes("..")) throw new Error(`Invalid advancement id: ${node && node.id}`);
  const data = node.data;
  if (!data || typeof data !== "object" || Array.isArray(data)) throw new Error(`${node.id}: advancement data must be an object`);
  if (!data.display || !data.display.icon || typeof data.display.icon.item !== "string") throw new Error(`${node.id}: display icon is required`);
  if (!data.criteria || typeof data.criteria !== "object" || !Object.keys(data.criteria).length) throw new Error(`${node.id}: at least one criterion is required`);
  for (const criterion of Object.values(data.criteria)) {
    if (!criterion || typeof criterion.trigger !== "string") throw new Error(`${node.id}: every criterion requires a trigger`);
  }
  if (data.parent && data.parent.startsWith(`${namespace}:`) && !ids.has(data.parent.slice(namespace.length + 1))) throw new Error(`${node.id}: parent ${data.parent} does not exist`);
}

function saveState(payload) {
  if (!payload || !Array.isArray(payload.advancements)) throw new Error("Missing advancements list");
  const ids = new Set();
  for (const node of payload.advancements) {
    if (ids.has(node.id)) throw new Error(`Duplicate advancement id: ${node.id}`);
    ids.add(node.id);
  }
  for (const node of payload.advancements) validateNode(node, ids);

  const byId = new Map(payload.advancements.map(node => [node.id, node]));
  for (const node of payload.advancements) {
    const chain = new Set([node.id]);
    let current = node;
    while (current && current.data.parent && current.data.parent.startsWith(`${namespace}:`)) {
      const parentId = current.data.parent.slice(namespace.length + 1);
      if (chain.has(parentId)) throw new Error(`${node.id}: parent relationship contains a cycle`);
      chain.add(parentId);
      current = byId.get(parentId);
    }
  }

  const zh = readJson(zhPath, {});
  const en = readJson(enPath, {});
  const layout = {};
  const existing = new Set(fs.existsSync(advancementsRoot) ? fs.readdirSync(advancementsRoot).filter(name => name.endsWith(".json")) : []);
  for (const node of payload.advancements) {
    const data = JSON.parse(JSON.stringify(node.data));
    data.display.title = { translate: node.titleKey || `advancements.candycraft.${node.id}.title` };
    data.display.description = { translate: node.descriptionKey || `advancements.candycraft.${node.id}.desc` };
    zh[data.display.title.translate] = String(node.titleZh || node.id);
    en[data.display.title.translate] = String(node.titleEn || node.titleZh || node.id);
    zh[data.display.description.translate] = String(node.descriptionZh || "");
    en[data.display.description.translate] = String(node.descriptionEn || node.descriptionZh || "");
    writeJsonAtomic(path.join(advancementsRoot, `${node.id}.json`), data);
    existing.delete(`${node.id}.json`);
    if (node.position && Number.isFinite(node.position.x) && Number.isFinite(node.position.y)) layout[node.id] = { x: Math.round(node.position.x), y: Math.round(node.position.y) };
  }
  for (const removed of existing) fs.unlinkSync(path.join(advancementsRoot, removed));
  writeJsonAtomic(zhPath, zh);
  writeJsonAtomic(enPath, en);
  writeJsonAtomic(layoutPath, layout);
  return { ok: true, count: ids.size, advancementsRoot };
}

function sendJson(res, data, status = 200) {
  res.writeHead(status, { "content-type": "application/json; charset=utf-8", "cache-control": "no-store" });
  res.end(JSON.stringify(data, null, 2));
}
function serveFile(res, file, type) {
  if (!fs.existsSync(file)) { res.writeHead(404); return res.end("not found"); }
  res.writeHead(200, { "content-type": type, "cache-control": "no-store" });
  res.end(fs.readFileSync(file));
}

const server = http.createServer((req, res) => {
  const parsed = new URL(req.url, "http://127.0.0.1");
  if (req.method === "GET" && parsed.pathname === "/") {
    const html = readText(path.join(__dirname, "index.html")).replace("__INITIAL_STATE__", JSON.stringify(buildState()).replace(/</g, "\\u003c"));
    res.writeHead(200, { "content-type": "text/html; charset=utf-8", "cache-control": "no-store" });
    return res.end(html);
  }
  if (req.method === "GET" && parsed.pathname === "/style.css") return serveFile(res, path.join(__dirname, "style.css"), "text/css; charset=utf-8");
  if (req.method === "GET" && parsed.pathname === "/api/state") return sendJson(res, buildState());
  if (req.method === "GET" && parsed.pathname.startsWith("/asset/")) {
    const rel = decodeURIComponent(parsed.pathname.slice(7)).replace(/\//g, path.sep);
    const textureRoot = path.resolve(assetsRoot, "textures");
    const file = path.resolve(textureRoot, rel);
    if (file !== textureRoot && !file.startsWith(textureRoot + path.sep)) return sendJson(res, { ok: false, error: "Bad asset path" }, 400);
    return serveFile(res, file, "image/png");
  }
  if (req.method === "POST" && parsed.pathname === "/api/save") {
    let body = "";
    req.on("data", chunk => { body += chunk; if (body.length > 5_000_000) req.destroy(); });
    req.on("end", () => {
      try { return sendJson(res, saveState(JSON.parse(body))); }
      catch (error) { return sendJson(res, { ok: false, error: String(error && error.message || error) }, 400); }
    });
    return;
  }
  res.writeHead(404); res.end("not found");
});

function startServer(port = defaultPort) {
  return new Promise((resolve, reject) => {
    server.once("error", reject);
    server.listen(port, "127.0.0.1", () => resolve({ server, port: server.address().port }));
  });
}

if (require.main === module) {
  const shouldOpen = !process.argv.includes("--no-browser");
  startServer().then(({ port }) => {
    console.log(`CandyCraft advancement editor: http://127.0.0.1:${port}`);
    if (shouldOpen) openInDefaultBrowser(`http://127.0.0.1:${port}/?v=1`);
  }).catch(error => {
    if (error && error.code === "EADDRINUSE") {
      if (shouldOpen) openInDefaultBrowser(`http://127.0.0.1:${defaultPort}/?v=1`);
      return;
    }
    console.error(error); process.exitCode = 1;
  });
}

module.exports = { buildState, saveState, startServer };
