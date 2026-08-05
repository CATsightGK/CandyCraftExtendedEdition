const fs = require("fs");
const http = require("http");
const path = require("path");
const url = require("url");
const childProcess = require("child_process");

const embeddedProjectRoot = "C:\\Users\\10424\\Documents\\Codex\\2026-05-26\\1-8-9forge-1-20-1forge\\CandyCraftExtendedEdition-clean";
const port = Number(process.env.PORT || 4326);
const typeToCategory = {
  "minecraft:crafting_shaped": "workbench",
  "minecraft:crafting_shapeless": "workbench",
  "candycraftmod:licorice_smelting": "smelting",
  "candycraftmod:licorice_fuel": "fuel",
  "candycraftmod:alchemy_mixing": "alchemy",
  "candycraftmod:sugar_factory": "sugar_factory"
};
const categoryFolders = {
  workbench: "workbench",
  smelting: "licorice_smelting",
  fuel: "licorice_fuel",
  alchemy: "alchemy_mixing",
  sugar_factory: "sugar_factory"
};

function projectRoot() {
  const candidates = [process.env.CANDYCRAFT_ROOT, process.cwd(), embeddedProjectRoot, path.resolve(__dirname, "..", "..")].filter(Boolean);
  return candidates.map(value => path.resolve(value)).find(value => fs.existsSync(path.join(value, "src", "main", "resources"))) || path.resolve(candidates[0]);
}

const root = projectRoot();
const resources = path.join(root, "src", "main", "resources");
const recipeRoot = path.join(resources, "data", "candycraftmod", "recipes");
const resourceAssets = path.join(resources, "assets");
const candyAssets = path.join(resourceAssets, "candycraftmod");

function readText(file) {
  return fs.existsSync(file) ? fs.readFileSync(file, "utf8").replace(/^\uFEFF/, "") : "";
}

function readJson(file, fallback = null) {
  try { return JSON.parse(readText(file)); } catch { return fallback; }
}

function walk(directory, extension = ".json") {
  if (!fs.existsSync(directory)) return [];
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry => {
    const file = path.join(directory, entry.name);
    return entry.isDirectory() ? walk(file, extension) : entry.name.endsWith(extension) ? [file] : [];
  });
}

function relativeRecipe(file) {
  return path.relative(recipeRoot, file).split(path.sep).join("/");
}

function resourceId(value, fallbackNamespace = "candycraftmod") {
  const parts = String(value || "").split(":", 2);
  return parts.length === 2 ? { namespace: parts[0], name: parts[1] } : { namespace: fallbackNamespace, name: parts[0] };
}

function validResourceId(value) {
  return /^[a-z0-9_.-]+:[a-z0-9_./-]+$/.test(String(value || ""));
}

function safeAssetFile(namespace, category, name, extension) {
  if (!/^[a-z0-9_.-]+$/.test(namespace) || !/^[a-z0-9_./-]+$/.test(name)) return null;
  const base = path.resolve(resourceAssets, namespace, category);
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
    seen.add(current); current = textures[current.slice(1)];
  }
  return typeof current === "string" && !current.startsWith("#") ? current : null;
}

function resolveItemIcon(id) {
  const parsed = resourceId(id, "minecraft");
  const model = resolveModel(`${parsed.namespace}:item/${parsed.name}`, parsed.namespace);
  for (const key of [...new Set(["layer0", "particle", "all", "top", "side", "end", ...Object.keys(model.textures)])]) {
    const reference = textureReference(model.textures[key], model.textures);
    if (!reference) continue;
    const texture = resourceId(reference, model.namespace);
    const file = safeAssetFile(texture.namespace, "textures", texture.name, ".png");
    if (file) return file;
  }
  for (const folder of ["item", "items"]) {
    const direct = safeAssetFile(parsed.namespace, "textures", `${folder}/${parsed.name}`, ".png");
    if (direct) return direct;
  }
  return null;
}

function parseRegistryIds(fileName) {
  const file = path.join(root, "src", "main", "java", "com", "valentin4311", "candycraftmod", "registry", fileName);
  const text = readText(file);
  const ids = new Set();
  const regex = /\bregister(?:SweetscapeFood|SweetscapeSimple|SweetscapeTool|Food|Simple|ToolItem|Tool|PortItem|SeedItem|SpawnEgg|Armor|Record|Emblem|BlockItem|NoItem)?\("([a-z0-9_./-]+)"/g;
  let match;
  while ((match = regex.exec(text))) ids.add(`candycraftmod:${match[1]}`);
  return ids;
}

function collectRecipeResources() {
  const items = new Set(["minecraft:sugar", "minecraft:stick", "minecraft:gold_nugget", "minecraft:bucket"]);
  const tags = new Set();
  function visit(value, key = "") {
    if (Array.isArray(value)) return value.forEach(entry => visit(entry, key));
    if (!value || typeof value !== "object") return;
    for (const [childKey, child] of Object.entries(value)) {
      if (childKey === "item" && typeof child === "string" && validResourceId(child)) items.add(child);
      if (childKey === "tag" && typeof child === "string" && validResourceId(child)) tags.add(child);
      visit(child, childKey);
    }
  }
  for (const file of walk(path.join(resources, "data"))) visit(readJson(file, {}));
  return { items, tags };
}

function localizedItems() {
  const zh = readJson(path.join(candyAssets, "lang", "zh_cn.json"), {}) || {};
  const en = readJson(path.join(candyAssets, "lang", "en_us.json"), {}) || {};
  const found = collectRecipeResources();
  const ids = new Set([...parseRegistryIds("CCItems.java"), ...parseRegistryIds("CCBlocks.java"), ...found.items]);
  return [...ids].sort().map(id => {
    const parsed = resourceId(id, "minecraft");
    const name = parsed.name;
    const label = parsed.namespace === "candycraftmod"
      ? zh[`item.candycraftmod.${name}`] || zh[`block.candycraftmod.${name}`] || en[`item.candycraftmod.${name}`] || en[`block.candycraftmod.${name}`] || id
      : id;
    return { id, name: label, icon: resolveItemIcon(id) ? `/api/item-icon?id=${encodeURIComponent(id)}` : null };
  });
}

const vanillaEffectNames = {
  speed: "速度", slowness: "缓慢", haste: "急迫", mining_fatigue: "挖掘疲劳", strength: "力量",
  instant_health: "瞬间治疗", instant_damage: "瞬间伤害", jump_boost: "跳跃提升", nausea: "反胃",
  regeneration: "生命恢复", resistance: "抗性提升", fire_resistance: "防火", water_breathing: "水下呼吸",
  invisibility: "隐身", blindness: "失明", night_vision: "夜视", hunger: "饥饿", weakness: "虚弱",
  poison: "中毒", wither: "凋零", health_boost: "生命提升", absorption: "伤害吸收", saturation: "饱和",
  glowing: "发光", levitation: "飘浮", luck: "幸运", slow_falling: "缓降", darkness: "黑暗"
};

function localizedEffects() {
  return Object.entries(vanillaEffectNames).map(([id, name]) => ({ id: `minecraft:${id}`, name }));
}

const liquids = [
  ["grenadine", "番石榴糖浆"], ["water", "水"], ["milk", "牛奶"], ["chocolate", "液态巧克力"],
  ["liquid_candy", "液态粉色糖浆"], ["lava", "熔岩"], ["caramel", "焦糖"]
].map(([id, name]) => ({ id, name }));

function ingredient(value) {
  if (Array.isArray(value)) value = value[0];
  if (!value || typeof value !== "object") return { kind: "item", id: "" };
  return value.tag ? { kind: "tag", id: value.tag } : { kind: "item", id: value.item || "" };
}

function result(value) {
  if (typeof value === "string") return { item: value, count: 1 };
  return { item: value?.item || "", count: Number(value?.count || 1) };
}

function normalizeWorkbench(id, json, file) {
  const shaped = json.type === "minecraft:crafting_shaped";
  const recipe = { category: "workbench", id, file, mode: shaped ? "shaped" : "shapeless", group: json.group || "", book_category: json.category || "misc", result: result(json.result) };
  if (shaped) {
    const cells = Array.from({ length: 9 }, () => null);
    const pattern = Array.isArray(json.pattern) ? json.pattern : [];
    pattern.slice(0, 3).forEach((row, y) => [...row].slice(0, 3).forEach((symbol, x) => {
      if (symbol !== " " && json.key?.[symbol]) cells[y * 3 + x] = ingredient(json.key[symbol]);
    }));
    recipe.cells = cells;
  } else {
    const grouped = new Map();
    for (const entry of json.ingredients || []) {
      const value = ingredient(entry); const key = `${value.kind}:${value.id}`;
      if (!grouped.has(key)) grouped.set(key, { ...value, count: 0 });
      grouped.get(key).count += 1;
    }
    recipe.ingredients = [...grouped.values()];
  }
  return recipe;
}

function normalize(id, json, file) {
  const category = typeToCategory[json.type];
  if (category === "workbench") return normalizeWorkbench(id, json, file);
  if (category === "smelting") return { category, id, file, ingredient: ingredient(json.ingredient), result: result(json.result), experience: Number(json.experience || 0), cooking_time: Number(json.cookingtime || 200), book_category: json.category || "misc", group: json.group || "" };
  if (category === "fuel") return { category, id, file, ingredient: ingredient(json.ingredient), burn_time: Number(json.burn_time || 300) };
  if (category === "sugar_factory") return { category, id, file, ingredient: ingredient(json.ingredient), input_count: Number(json.input_count || 1), result: result(json.result), processing_time: Number(json.processing_time || 240), factory: json.factory || "both" };
  if (category === "alchemy") return {
    category, id, file, liquid: json.liquid || "grenadine", mixing_time: Number(json.mixing_time || 400), sugar_mixing_time: Number(json.sugar_mixing_time || 200),
    ingredients: (json.ingredients || []).map(entry => ({ ...ingredient(entry.ingredient), count: Number(entry.count || 1) })),
    result: json.result || { type: "item", item: "candycraftmod:sugar_pill", count: 1 }
  };
  return null;
}

function loadRecipes() {
  const recipes = { workbench: [], smelting: [], fuel: [], alchemy: [], sugar_factory: [] };
  for (const file of walk(recipeRoot)) {
    const json = readJson(file, null);
    const category = json && typeToCategory[json.type];
    if (!category) continue;
    const relative = relativeRecipe(file);
    const id = relative.replace(/\.json$/, "").split("/").pop();
    recipes[category].push(normalize(id, json, relative));
  }
  Object.values(recipes).forEach(values => values.sort((a, b) => a.id.localeCompare(b.id)));
  return recipes;
}

function safeId(value) {
  const id = String(value || "").trim().toLowerCase();
  if (!/^[a-z0-9_.-]+$/.test(id)) throw new Error("配方 ID 只能包含小写字母、数字、下划线、点和短横线");
  return id;
}

function checkedIngredient(value, label = "材料") {
  const kind = value?.kind === "tag" ? "tag" : "item";
  const id = String(value?.id || "").trim();
  if (!validResourceId(id)) throw new Error(`${label} ID 格式不正确`);
  return { kind, id };
}

function bounded(value, label, min, max) {
  const number = Math.round(Number(value));
  if (!Number.isFinite(number) || number < min || number > max) throw new Error(`${label}必须在 ${min} 到 ${max} 之间`);
  return number;
}

function checkedResult(value) {
  const item = String(value?.item || "").trim();
  if (!validResourceId(item)) throw new Error("输出物品 ID 格式不正确");
  return { item, count: bounded(value?.count || 1, "输出数量", 1, 64) };
}

function ingredientJson(value) {
  const checked = checkedIngredient(value);
  return { [checked.kind]: checked.id };
}

function serializeWorkbench(recipe) {
  const output = checkedResult(recipe.result);
  const base = { type: recipe.mode === "shapeless" ? "minecraft:crafting_shapeless" : "minecraft:crafting_shaped", category: recipe.book_category || "misc" };
  if (recipe.group) base.group = String(recipe.group);
  if (recipe.mode === "shapeless") {
    const entries = [];
    for (const entry of recipe.ingredients || []) {
      const count = bounded(entry.count || 1, "材料数量", 1, 9);
      for (let i = 0; i < count; i++) entries.push(ingredientJson(entry));
    }
    if (!entries.length || entries.length > 9) throw new Error("无序配方需要 1 到 9 个材料");
    return { ...base, ingredients: entries, result: output };
  }
  const cells = Array.from({ length: 9 }, (_, index) => recipe.cells?.[index] || null);
  const used = cells.map((cell, index) => cell ? index : -1).filter(index => index >= 0);
  if (!used.length) throw new Error("有序配方至少需要一个材料");
  const xs = used.map(index => index % 3), ys = used.map(index => Math.floor(index / 3));
  const minX = Math.min(...xs), maxX = Math.max(...xs), minY = Math.min(...ys), maxY = Math.max(...ys);
  const symbols = "ABCDEFGHI"; const keys = new Map(); const keyJson = {};
  const pattern = [];
  for (let y = minY; y <= maxY; y++) {
    let row = "";
    for (let x = minX; x <= maxX; x++) {
      const cell = cells[y * 3 + x];
      if (!cell) { row += " "; continue; }
      const checked = checkedIngredient(cell);
      const identity = `${checked.kind}:${checked.id}`;
      if (!keys.has(identity)) {
        const symbol = symbols[keys.size]; keys.set(identity, symbol); keyJson[symbol] = { [checked.kind]: checked.id };
      }
      row += keys.get(identity);
    }
    pattern.push(row);
  }
  return { ...base, pattern, key: keyJson, result: output };
}

function serialize(category, recipe) {
  if (category === "workbench") return serializeWorkbench(recipe);
  if (category === "smelting") return {
    type: "candycraftmod:licorice_smelting", group: String(recipe.group || ""), category: recipe.book_category || "misc",
    ingredient: ingredientJson(recipe.ingredient), result: checkedResult(recipe.result),
    experience: Math.max(0, Number(recipe.experience || 0)), cookingtime: bounded(recipe.cooking_time, "烧制时间", 1, 72000)
  };
  if (category === "fuel") return { type: "candycraftmod:licorice_fuel", ingredient: ingredientJson(recipe.ingredient), burn_time: bounded(recipe.burn_time, "燃烧时间", 1, 72000) };
  if (category === "sugar_factory") {
    if (!["normal", "advanced", "both"].includes(recipe.factory)) throw new Error("请选择制糖机类型");
    return { type: "candycraftmod:sugar_factory", ingredient: ingredientJson(recipe.ingredient), input_count: bounded(recipe.input_count, "输入数量", 1, 64), result: checkedResult(recipe.result), processing_time: bounded(recipe.processing_time, "处理时间", 1, 72000), factory: recipe.factory };
  }
  if (category === "alchemy") {
    const entries = recipe.ingredients || [];
    const total = entries.reduce((sum, entry) => sum + Number(entry.count || 0), 0);
    if (total !== 4) throw new Error("搅拌锅材料数量合计必须为 4");
    if (!liquids.some(value => value.id === recipe.liquid)) throw new Error("请选择锅内液体");
    const normal = bounded(recipe.mixing_time, "普通搅拌时间", 1, 72000);
    const sugar = bounded(recipe.sugar_mixing_time, "加糖搅拌时间", 1, 72000);
    if (sugar > normal) throw new Error("加糖搅拌时间不能长于普通搅拌时间");
    const value = { type: "candycraftmod:alchemy_mixing", liquid: recipe.liquid, mixing_time: normal, sugar_mixing_time: sugar, ingredients: entries.map(entry => ({ ingredient: ingredientJson(entry), count: bounded(entry.count, "材料数量", 1, 4) })) };
    const resultValue = recipe.result || {};
    if (resultValue.type === "sugar_pill") {
      value.result = { type: "sugar_pill", count: bounded(resultValue.count || 1, "输出数量", 1, 64), effects: (resultValue.effects || []).map(effect => ({ id: checkedIngredient({ kind: "item", id: effect.id }, "药效").id, duration: bounded(effect.duration, "药效时间", 1, 72000), amplifier: bounded(effect.amplifier || 0, "药效等级", 0, 255) })), colors: (resultValue.colors || ["#ffffff", "#ffffff", "#ffffff", "#ffffff"]).slice(0, 4) };
    } else {
      value.result = { type: "item", ...checkedResult(resultValue) };
    }
    return value;
  }
  throw new Error("未知配方类型");
}

function resolvePrevious(category, relative) {
  if (!relative) return null;
  const normalized = String(relative).replace(/\\/g, "/");
  if (normalized.includes("..") || path.isAbsolute(normalized)) throw new Error("原配方路径无效");
  const file = path.resolve(recipeRoot, normalized);
  if (!file.startsWith(`${path.resolve(recipeRoot)}${path.sep}`) || !fs.existsSync(file)) throw new Error("原配方文件不存在");
  const json = readJson(file, null);
  if (!json || typeToCategory[json.type] !== category) throw new Error("原配方类型不匹配");
  return file;
}

function jsonResponse(res, status, value) {
  res.writeHead(status, { "content-type": "application/json; charset=utf-8", "cache-control": "no-store" });
  res.end(JSON.stringify(value, null, 2));
}

function body(req) {
  return new Promise((resolve, reject) => {
    let value = "";
    req.on("data", chunk => { value += chunk; if (value.length > 2 * 1024 * 1024) reject(new Error("请求过大")); });
    req.on("end", () => { try { resolve(JSON.parse(value || "{}")); } catch { reject(new Error("JSON 无效")); } });
    req.on("error", reject);
  });
}

const server = http.createServer(async (req, res) => {
  const requestUrl = url.parse(req.url, true);
  const route = requestUrl.pathname;
  try {
    if (req.method === "GET" && route === "/api/state") {
      const resourcesFound = collectRecipeResources();
      return jsonResponse(res, 200, { root, recipeRoot, recipes: loadRecipes(), items: localizedItems(), tags: [...resourcesFound.tags].sort(), effects: localizedEffects(), liquids });
    }
    if (req.method === "GET" && route === "/api/item-icon") {
      const file = resolveItemIcon(requestUrl.query.id);
      if (!file) { res.writeHead(404); return res.end("not found"); }
      res.writeHead(200, { "content-type": "image/png", "cache-control": "no-store" });
      return fs.createReadStream(file).pipe(res);
    }
    if (req.method === "POST" && route === "/api/save") {
      const data = await body(req);
      const category = String(data.category || "");
      if (!categoryFolders[category]) throw new Error("配方类型无效");
      const id = safeId(data.recipe?.id);
      const previous = resolvePrevious(category, data.previousFile);
      const previousId = previous ? path.basename(previous, ".json") : null;
      const target = previous && previousId === id ? previous : path.join(recipeRoot, categoryFolders[category], `${id}.json`);
      if (fs.existsSync(target) && target !== previous) throw new Error(`配方 ${id} 已存在`);
      const output = serialize(category, data.recipe || {});
      fs.mkdirSync(path.dirname(target), { recursive: true });
      fs.writeFileSync(target, `${JSON.stringify(output, null, 2)}\n`, "utf8");
      if (previous && previous !== target) fs.rmSync(previous, { force: true });
      return jsonResponse(res, 200, { ok: true, file: relativeRecipe(target), id });
    }
    if (req.method === "POST" && route === "/api/delete") {
      const data = await body(req);
      const file = resolvePrevious(String(data.category || ""), data.file);
      if (!file) throw new Error("请选择已保存的配方");
      fs.rmSync(file, { force: true });
      return jsonResponse(res, 200, { ok: true });
    }
    const staticFiles = { "/": ["index.html", "text/html; charset=utf-8"], "/style.css": ["style.css", "text/css; charset=utf-8"] };
    if (staticFiles[route]) {
      res.writeHead(200, { "content-type": staticFiles[route][1], "cache-control": "no-store" });
      return res.end(fs.readFileSync(path.join(__dirname, staticFiles[route][0])));
    }
    res.writeHead(404); res.end("not found");
  } catch (error) {
    jsonResponse(res, 400, { ok: false, error: error.message });
  }
});

server.listen(port, "127.0.0.1", () => {
  const target = `http://127.0.0.1:${port}`;
  console.log(`CandyCraft recipe editor: ${target}`);
  if (!process.argv.includes("--no-browser") && process.env.CANDYCRAFT_NO_BROWSER !== "1") {
    const child = childProcess.spawn("rundll32.exe", ["url.dll,FileProtocolHandler", target], { detached: true, stdio: "ignore", windowsHide: true });
    child.unref();
  }
});
