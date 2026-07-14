const fs = require("fs");
const http = require("http");
const path = require("path");
const url = require("url");

const root = process.env.CANDYCRAFT_ROOT
  ? path.resolve(process.env.CANDYCRAFT_ROOT)
  : path.resolve(__dirname, "..", "..");
const assetsRoot = path.join(root, "src", "main", "resources", "assets", "candycraftmod");
const orderPath = path.join(root, "src", "main", "resources", "data", "candycraftmod", "creative_tabs", "order.json");
const defaultPort = Number(process.env.PORT || 4311);

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

function parseRegistryIds(javaFile, kind) {
  const text = readText(path.join(root, "src", "main", "java", "com", "valentin4311", "candycraftmod", "registry", javaFile));
  const items = [];
  const regex = /public\s+static\s+final\s+RegistryObject<[^>]+>\s+([A-Z0-9_]+)\s*=\s*([^;]+);/g;
  let match;
  while ((match = regex.exec(text))) {
    const constant = match[1];
    const expr = match[2];
    const name = expr.match(/\bregister(?:Food|Simple|Tool|PortItem|SeedItem|SpawnEgg|Armor|Record|Emblem|BlockItem)?\("([^"]+)"/);
    if (!name || expr.includes("registerNoItem(")) {
      continue;
    }
    items.push({
      id: `candycraftmod:${name[1]}`,
      constant,
      kind,
      registryFile: javaFile,
      list: expr.includes("registerTool(") ? "tool"
        : expr.includes("registerSimple(") || expr.includes("registerFood(") ? "simple"
        : kind === "block" ? "block"
        : "item"
    });
  }
  return items;
}

function modelPath(modelId, defaultFolder) {
  if (!modelId) {
    return null;
  }
  let namespace = "minecraft";
  let name = modelId;
  if (modelId.includes(":")) {
    const parts = modelId.split(":");
    namespace = parts[0];
    name = parts[1];
  }
  if (namespace !== "candycraftmod") {
    return null;
  }
  const rel = name.includes("/") ? name : `${defaultFolder}/${name}`;
  return path.join(assetsRoot, "models", `${rel}.json`);
}

function mergeTextures(child, parent) {
  return { ...(parent || {}), ...(child || {}) };
}

function resolveTextureReference(value, textures) {
  let current = value;
  const seen = new Set();
  while (typeof current === "string" && current.startsWith("#")) {
    const key = current.slice(1);
    if (seen.has(key)) {
      return null;
    }
    seen.add(key);
    current = textures[key];
  }
  return current || null;
}

function loadModel(modelId, defaultFolder, seen = new Set()) {
  const file = modelPath(modelId, defaultFolder);
  if (!file || seen.has(file)) {
    return null;
  }
  seen.add(file);
  const model = readJson(file, null);
  if (!model) {
    return null;
  }
  const parent = loadModel(model.parent, defaultFolder, seen);
  return {
    ...model,
    textures: mergeTextures(model.textures, parent && parent.textures),
    parentModel: parent
  };
}

function textureUrl(textureId) {
  if (!textureId || textureId.startsWith("#")) {
    return null;
  }
  let id = textureId;
  if (id.startsWith("candycraftmod:")) {
    id = id.slice("candycraftmod:".length);
  } else if (id.includes(":")) {
    return null;
  }
  return `/asset/${id}.png`;
}

function textureFromModel(id) {
  const name = id.split(":")[1];
  const model = loadModel(`candycraftmod:item/${name}`, "item");
  if (!model) {
    return null;
  }
  const textures = model.textures || {};
  const keys = ["layer0", "all", "texture", "side", "top", "end", "particle", "front", "0", "1", "2"];
  for (const key of keys) {
    const resolved = resolveTextureReference(textures[key], textures);
    const url = textureUrl(resolved);
    if (url) {
      return url;
    }
  }
  return null;
}

function defaultOrders(items) {
  const byConstant = new Map(items.map(item => [`${item.source}.${item.constant}`, item.id]));
  const byId = new Set(items.map(item => item.id));
  const movedCandyCanes = new Set([
    "WHITE_CANDY_CANE",
    "RED_CANDY_CANE",
    "GREEN_CANDY_CANE",
    "WHITE_RED_CANDY_CANE",
    "WHITE_GREEN_CANDY_CANE",
    "RED_GREEN_CANDY_CANE"
  ]);
  const candycraftConstants = [
    "HONEY_SHARD", "HONEYCOMB", "NOUGAT_POWDER", "PEZ", "PEZ_DUST",
    "LICORICE", "CHOCOLATE_COIN", "CRANBERRY_SCALE", "SUGAR_CRYSTAL",
    "WAFFLE_NUGGET", "MARSHMALLOW_STICK",
    "LOLLIPOP_SEEDS", "DRAGIBUS", "MARSHMALLOW_FLOWER",
    "CANDIED_CHERRY", "CANDY_CANE",
    "sweetscape.WHITE_CANDY_CANE", "sweetscape.RED_CANDY_CANE", "sweetscape.GREEN_CANDY_CANE",
    "sweetscape.WHITE_RED_CANDY_CANE", "sweetscape.WHITE_GREEN_CANDY_CANE", "sweetscape.RED_GREEN_CANDY_CANE",
    "CHEWING_GUM", "COTTON_CANDY",
    "CRANBERRY_FISH", "CRANBERRY_FISH_COOKED", "DRAGIBUS_STICK", "GUMMY",
    "HOT_GUMMY", "LOLLIPOP", "SUGAR_PILL", "WAFFLE",
    "MARSHMALLOW_SWORD", "MARSHMALLOW_SHOVEL", "MARSHMALLOW_PICKAXE", "MARSHMALLOW_AXE", "MARSHMALLOW_HOE",
    "LICORICE_SWORD", "LICORICE_SHOVEL", "LICORICE_PICKAXE", "LICORICE_AXE", "LICORICE_HOE",
    "HONEY_SWORD", "HONEY_SHOVEL", "HONEY_PICKAXE", "HONEY_AXE", "HONEY_HOE",
    "PEZ_SWORD", "PEZ_SHOVEL", "PEZ_PICKAXE", "PEZ_AXE", "PEZ_HOE",
    "FORK", "LICORICE_SPEAR", "CARAMEL_BOW", "CARAMEL_CROSSBOW", "HONEY_ARROW", "HONEY_BOLT",
    "GUMMY_BALL", "DYNAMITE", "GLUE_DYNAMITE",
    "HONEY_HELMET", "HONEY_PLATE", "HONEY_LEGGINGS", "HONEY_BOOTS",
    "LICORICE_HELMET", "LICORICE_PLATE", "LICORICE_LEGGINGS", "LICORICE_BOOTS",
    "PEZ_HELMET", "PEZ_PLATE", "PEZ_LEGGINGS", "PEZ_BOOTS", "JELLY_BOOTS",
    "BEETLE_KEY", "JELLY_KEY", "JELLY_SENTRY_KEY", "JELLY_BOSS_KEY",
    "SUGUARD_KEY", "SUGUARD_SENTRY_KEY", "SUGUARD_BOSS_KEY", "SKY_KEY",
    "CHEWING_GUM_EMBLEM", "CRANBERRY_EMBLEM", "GINGERBREAD_EMBLEM", "HONEY_EMBLEM",
    "JELLY_EMBLEM", "SKY_EMBLEM", "SUGUARD_EMBLEM", "WATER_EMBLEM",
    "JELLY_CROWN", "WATER_MASK", "JELLY_WAND", "JUMP_WAND",
    "RECORD_1", "RECORD_2", "RECORD_3", "RECORD_4",
    "CARAMEL_BUCKET", "GRENADINE_BUCKET"
  ];
  const candycraft = [];
  for (const constant of candycraftConstants) {
    const key = constant.includes(".") ? constant : `candycraft.${constant}`;
    const id = byConstant.get(key);
    if (id && !candycraft.includes(id)) {
      candycraft.push(id);
    }
  }
  for (const item of items) {
    if (item.source === "candycraft" && item.kind === "block" && shouldShowCandycraftBlock(item.id)) {
      candycraft.push(item.id);
    }
  }

  const sweetscape = [];
  for (const group of ["simple", "tool", "block"]) {
    for (const item of items) {
      if (item.source !== "sweetscape" || item.list !== group) {
        continue;
      }
      if (movedCandyCanes.has(item.constant)) {
        continue;
      }
      sweetscape.push(item.id);
    }
  }
  for (const id of [...candycraft, ...sweetscape]) {
    byId.delete(id);
  }
  return { candycraft, sweetscape };
}

function shouldShowCandycraftBlock(id) {
  const pathName = id.split(":")[1];
  return pathName !== "candy_portal"
    && pathName !== "block_teleporter"
    && pathName !== "licorice_furnace_on"
    && pathName !== "cherry_block"
    && pathName !== "sweet_grass"
    && !/^caramel_(glass|pane)_[0-9]+$/.test(pathName)
    && !pathName.includes("double_slab")
    && !pathName.includes(".");
}

function buildState() {
  const zh = readJson(path.join(assetsRoot, "lang", "zh_cn.json"), {});
  const en = readJson(path.join(assetsRoot, "lang", "en_us.json"), {});
  const registry = new Map();

  for (const item of parseRegistryIds("CCItems.java", "item")) {
    registry.set(item.id, { ...item, source: "candycraft" });
  }
  for (const item of parseRegistryIds("CCSweetscapeItems.java", "item")) {
    registry.set(item.id, { ...item, source: "sweetscape" });
  }
  for (const block of parseRegistryIds("CCBlocks.java", "block")) {
    registry.set(block.id, { ...block, source: "candycraft" });
  }
  for (const block of parseRegistryIds("CCSweetscapeBlocks.java", "block")) {
    registry.set(block.id, { ...block, source: "sweetscape" });
  }
  const items = [...registry.values()]
    .map(item => {
      const pathName = item.id.split(":")[1];
      return {
        ...item,
        name: zh[`item.candycraftmod.${pathName}`] || zh[`block.candycraftmod.${pathName}`] ||
          en[`item.candycraftmod.${pathName}`] || en[`block.candycraftmod.${pathName}`] || pathName,
        texture: textureFromModel(item.id),
      };
    });

  const saved = readJson(orderPath, null);
  const tabs = saved && Array.isArray(saved.blocks) && Array.isArray(saved.food) && Array.isArray(saved.tools_armor) && Array.isArray(saved.misc)
    ? saved
    : defaultOrders(items);
  return { items, tabs, orderPath };
}

function sendJson(res, data) {
  res.writeHead(200, { "content-type": "application/json; charset=utf-8" });
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

function escapeHtml(value) {
  return String(value).replace(/[&<>"']/g, char => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#39;"
  })[char]);
}

function renderSlot(item, place) {
  const image = item.texture ? `<img src="${escapeHtml(item.texture)}" alt="">` : "";
  return `<button class="slot" draggable="true" data-id="${escapeHtml(item.id)}" data-place="${escapeHtml(place)}" title="${escapeHtml(item.name)}&#10;${escapeHtml(item.id)}">
    <span class="sprite">${image}</span>
    <span class="tooltip">${escapeHtml(item.name)}<br><small>${escapeHtml(item.id.replace("candycraftmod:", ""))}</small></span>
  </button>`;
}

function renderEmptySlots(count) {
  let html = "";
  for (let i = 0; i < count; i++) {
    html += `<div class="slot empty"></div>`;
  }
  return html;
}

function initialRenderedGrids(state) {
  const byId = new Map(state.items.map(item => [item.id, item]));
  const activeItems = (state.tabs.blocks || []).map(id => byId.get(id)).filter(Boolean);
  const used = new Set(Object.values(state.tabs).flat());
  const libraryItems = state.items
    .filter(item => !used.has(item.id))
    .sort((a, b) => a.name.localeCompare(b.name, "zh-Hans-CN"));
  const minSlots = Math.max(54, Math.ceil(activeItems.length / 9) * 9);
  return {
    activeSlots: activeItems.map(item => renderSlot(item, "active")).join("") + renderEmptySlots(Math.max(0, minSlots - activeItems.length)),
    librarySlots: libraryItems.map(item => renderSlot(item, "library")).join(""),
    activeCount: `${activeItems.length} items`,
    libraryCount: `${libraryItems.length} items`
  };
}

const server = http.createServer((req, res) => {
  const parsed = url.parse(req.url, true);
  if (req.method === "GET" && parsed.pathname === "/") {
    const state = buildState();
    const rendered = initialRenderedGrids(state);
    const html = readText(path.join(__dirname, "index.html"))
      .replace("__INITIAL_STATE__", JSON.stringify(state).replace(/</g, "\\u003c"))
      .replace("__ACTIVE_SLOTS__", rendered.activeSlots)
      .replace("__LIBRARY_SLOTS__", rendered.librarySlots)
      .replace("__ACTIVE_COUNT__", rendered.activeCount)
      .replace("__LIBRARY_COUNT__", rendered.libraryCount);
    res.writeHead(200, { "content-type": "text/html; charset=utf-8", "cache-control": "no-store" });
    return res.end(html);
  }
  if (req.method === "GET" && parsed.pathname === "/style.css") {
    return serveFile(res, path.join(__dirname, "style.css"), "text/css; charset=utf-8");
  }
  if (req.method === "GET" && parsed.pathname === "/api/state") {
    return sendJson(res, buildState());
  }
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
      if (body.length > 2_000_000) {
        req.destroy();
      }
    });
    req.on("end", () => {
      try {
        const data = JSON.parse(body);
        const clean = {
          blocks: Array.isArray(data.blocks) ? data.blocks.filter(id => typeof id === "string") : [],
          food: Array.isArray(data.food) ? data.food.filter(id => typeof id === "string") : [],
          tools_armor: Array.isArray(data.tools_armor) ? data.tools_armor.filter(id => typeof id === "string") : [],
          misc: Array.isArray(data.misc) ? data.misc.filter(id => typeof id === "string") : []
        };
        fs.mkdirSync(path.dirname(orderPath), { recursive: true });
        fs.writeFileSync(orderPath, JSON.stringify(clean, null, 2) + "\n", "utf8");
        sendJson(res, { ok: true, orderPath });
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
      console.log(`CandyCraft creative tab editor: http://127.0.0.1:${actualPort}`);
      resolve({ server, port: actualPort });
    });
  });
}

if (require.main === module) {
  startServer().catch(error => {
    console.error(error);
    process.exitCode = 1;
  });
}

module.exports = { startServer };
