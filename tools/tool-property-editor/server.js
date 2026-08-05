const fs = require("fs");
const childProcess = require("child_process");
const http = require("http");
const path = require("path");

const namespace = "candycraftmod";
const embeddedProjectRoot = "C:\\Users\\10424\\Documents\\Codex\\2026-05-26\\1-8-9forge-1-20-1forge\\CandyCraftExtendedEdition-clean";
const defaultPort = Number(process.env.PORT || 4316);

function resolveProjectRoot() {
  const candidates = [process.env.CANDYCRAFT_ROOT, process.cwd(), path.dirname(process.execPath), embeddedProjectRoot, path.resolve(__dirname, "..", "..")].filter(Boolean);
  for (const candidate of candidates) {
    const resolved = path.resolve(candidate);
    if (fs.existsSync(path.join(resolved, "src", "main", "java", "com", "valentin4311", "candycraftmod"))) return resolved;
  }
  return path.resolve(candidates[0]);
}

const root = resolveProjectRoot();
const resourcesRoot = path.join(root, "src", "main", "resources");
const assetsRoot = path.join(resourcesRoot, "assets", namespace);
const configPath = path.join(resourcesRoot, "data", namespace, "tool_properties.json");

const ENCHANTMENTS = [
  ["minecraft:sharpness", "锋利", 5, "近战"], ["minecraft:smite", "亡灵杀手", 5, "近战"],
  ["minecraft:bane_of_arthropods", "节肢杀手", 5, "近战"], ["minecraft:knockback", "击退", 2, "近战"],
  ["minecraft:fire_aspect", "火焰附加", 2, "近战"], ["minecraft:looting", "抢夺", 3, "近战"],
  ["minecraft:sweeping", "横扫之刃", 3, "近战"], ["minecraft:efficiency", "效率", 5, "采掘"],
  ["minecraft:silk_touch", "精准采集", 1, "采掘"], ["minecraft:fortune", "时运", 3, "采掘"],
  ["minecraft:power", "力量", 5, "远程"], ["minecraft:punch", "冲击", 2, "远程"],
  ["minecraft:flame", "火矢", 1, "远程"], ["minecraft:infinity", "无限", 1, "远程"],
  ["minecraft:quick_charge", "快速装填", 3, "远程"], ["minecraft:multishot", "多重射击", 1, "远程"],
  ["minecraft:piercing", "穿透", 4, "远程"], ["minecraft:impaling", "穿刺", 5, "三叉戟"],
  ["minecraft:loyalty", "忠诚", 3, "三叉戟"], ["minecraft:riptide", "激流", 3, "三叉戟"],
  ["minecraft:channeling", "引雷", 1, "三叉戟"], ["minecraft:protection", "保护", 4, "盔甲"],
  ["minecraft:fire_protection", "火焰保护", 4, "盔甲"], ["minecraft:blast_protection", "爆炸保护", 4, "盔甲"],
  ["minecraft:projectile_protection", "弹射物保护", 4, "盔甲"], ["minecraft:respiration", "水下呼吸", 3, "盔甲"],
  ["minecraft:aqua_affinity", "水下速掘", 1, "盔甲"], ["minecraft:thorns", "荆棘", 3, "盔甲"],
  ["minecraft:swift_sneak", "迅捷潜行", 3, "盔甲"], ["minecraft:feather_falling", "摔落缓冲", 4, "盔甲"],
  ["minecraft:depth_strider", "深海探索者", 3, "盔甲"], ["minecraft:frost_walker", "冰霜行者", 2, "盔甲"],
  ["minecraft:soul_speed", "灵魂疾行", 3, "盔甲"], ["minecraft:unbreaking", "耐久", 3, "通用"],
  ["minecraft:mending", "经验修补", 1, "通用"], ["minecraft:binding_curse", "绑定诅咒", 1, "诅咒"],
  ["minecraft:vanishing_curse", "消失诅咒", 1, "诅咒"]
].map(([id, name, maxLevel, group]) => ({ id, name, maxLevel, group }));

const common = ["minecraft:unbreaking", "minecraft:mending", "minecraft:vanishing_curse"];
const ENCHANTS_BY_TYPE = {
  sword: ["minecraft:sharpness", "minecraft:smite", "minecraft:bane_of_arthropods", "minecraft:knockback", "minecraft:fire_aspect", "minecraft:looting", "minecraft:sweeping", ...common],
  spear: ["minecraft:sharpness", "minecraft:smite", "minecraft:bane_of_arthropods", "minecraft:knockback", "minecraft:fire_aspect", "minecraft:looting", ...common],
  fork: ["minecraft:unbreaking", "minecraft:mending"],
  axe: ["minecraft:sharpness", "minecraft:smite", "minecraft:bane_of_arthropods", "minecraft:efficiency", "minecraft:silk_touch", "minecraft:fortune", ...common],
  pickaxe: ["minecraft:efficiency", "minecraft:silk_touch", "minecraft:fortune", ...common],
  shovel: ["minecraft:efficiency", "minecraft:silk_touch", "minecraft:fortune", ...common],
  hoe: ["minecraft:efficiency", "minecraft:silk_touch", "minecraft:fortune", ...common],
  bow: ["minecraft:power", "minecraft:punch", "minecraft:flame", "minecraft:infinity", ...common],
  crossbow: ["minecraft:quick_charge", "minecraft:multishot", "minecraft:piercing", ...common],
  wand: ["minecraft:unbreaking", "minecraft:mending", "minecraft:vanishing_curse"],
  projectile: [], other: []
};

function armorEnchantments(type) {
  const values = ["minecraft:protection", "minecraft:fire_protection", "minecraft:blast_protection", "minecraft:projectile_protection", "minecraft:thorns", "minecraft:unbreaking", "minecraft:mending", "minecraft:binding_curse", "minecraft:vanishing_curse"];
  if (type === "helmet") values.push("minecraft:respiration", "minecraft:aqua_affinity");
  if (type === "leggings") values.push("minecraft:swift_sneak");
  if (type === "boots") values.push("minecraft:feather_falling", "minecraft:depth_strider", "minecraft:frost_walker", "minecraft:soul_speed");
  return values;
}

function item(id, category, toolType, values, referenceMaterial) {
  return {
    id: `${namespace}:${id}`, category, toolType, referenceMaterial,
    defaults: {
      category, toolType, referenceMaterial,
      durability: values.durability == null ? 0 : values.durability,
      attackDamage: values.attackDamage == null ? null : values.attackDamage,
      attackSpeed: values.attackSpeed == null ? null : values.attackSpeed,
      armor: values.armor == null ? null : values.armor,
      armorToughness: values.armorToughness == null ? null : values.armorToughness,
      knockbackResistance: values.knockbackResistance == null ? null : values.knockbackResistance,
      enchantability: values.enchantability == null ? 0 : values.enchantability,
      useVanillaEnchantments: false,
      allowedEnchantments: values.allowedEnchantments || (category === "armor" ? armorEnchantments(toolType) : ENCHANTS_BY_TYPE[toolType] || [])
    }
  };
}

function toolSet(prefix, tier, referenceMaterial, axeDamage = 5, axeSpeed = 0.9) {
  return [
    item(`${prefix}_sword`, "melee", "sword", { durability: tier.durability, attackDamage: tier.bonus + 4, attackSpeed: 1.6, enchantability: tier.enchantability }, referenceMaterial),
    item(`${prefix}_shovel`, "tool", "shovel", { durability: tier.durability, attackDamage: tier.bonus + 2.5, attackSpeed: 1.0, enchantability: tier.enchantability }, referenceMaterial),
    item(`${prefix}_pickaxe`, "tool", "pickaxe", { durability: tier.durability, attackDamage: tier.bonus + 2, attackSpeed: 1.2, enchantability: tier.enchantability }, referenceMaterial),
    item(`${prefix}_axe`, "tool", "axe", { durability: tier.durability, attackDamage: tier.bonus + axeDamage + 1, attackSpeed: axeSpeed, enchantability: tier.enchantability }, referenceMaterial),
    item(`${prefix}_hoe`, "tool", "hoe", { durability: tier.durability, attackDamage: tier.bonus - 1, attackSpeed: 3.0, enchantability: tier.enchantability }, referenceMaterial)
  ];
}

const TIERS = {
  marshmallow: { durability: 131, bonus: 1, enchantability: 8 },
  chocolate: { durability: 750, bonus: 2.5, enchantability: 25 },
  cotton: { durability: 5, bonus: 5, enchantability: 65 },
  licorice: { durability: 250, bonus: 2, enchantability: 12 },
  honey: { durability: 220, bonus: 2, enchantability: 18 },
  pez: { durability: 561, bonus: 3, enchantability: 14 }
};

const DEFINITIONS = [
  item("fork", "melee", "fork", { durability: 326, attackDamage: 5.5, attackSpeed: 1.1, enchantability: 8 }, "trident"),
  ...toolSet("marshmallow", TIERS.marshmallow, "stone"),
  ...toolSet("milk_chocolate", TIERS.chocolate, "iron", 5.5, 1.0),
  ...toolSet("white_chocolate", TIERS.chocolate, "iron", 5.5, 1.0),
  ...toolSet("dark_chocolate", TIERS.chocolate, "iron", 5.5, 1.0),
  ...toolSet("cotton_candy", TIERS.cotton, "gold", 5, 1.0),
  ...toolSet("licorice", TIERS.licorice, "iron"),
  item("licorice_spear", "melee", "spear", { durability: 250, attackDamage: 5, attackSpeed: 1.8, enchantability: 12 }, "trident"),
  ...toolSet("honey", TIERS.honey, "iron"),
  ...toolSet("pez", TIERS.pez, "diamond"),
  item("caramel_bow", "ranged", "bow", { durability: 384, enchantability: 1 }, "bow"),
  item("caramel_crossbow", "ranged", "crossbow", { durability: 465, enchantability: 1 }, "crossbow"),
  item("honey_arrow", "ranged", "projectile", { durability: 0, enchantability: 0 }, "arrow"),
  item("honey_bolt", "ranged", "projectile", { durability: 0, enchantability: 0 }, "arrow"),
  item("jelly_wand", "ranged", "wand", { durability: 0, enchantability: 0 }, "bow"),
  item("jump_wand", "ranged", "wand", { durability: 0, enchantability: 0 }, "bow"),
  item("dragibus_stick", "other", "other", { durability: 25, enchantability: 0 }, "shield"),
  item("alchemy_mixer_blade", "other", "other", { durability: 0, enchantability: 0 }, "shield")
];

function armorSet(prefix, material, referenceMaterial) {
  const types = [["helmet", material.helmet], ["plate", material.chestplate], ["leggings", material.leggings], ["boots", material.boots]];
  return types.map(([suffix, values]) => item(`${prefix}_${suffix}`, "armor", suffix === "plate" ? "chestplate" : suffix, {
    ...values, armorToughness: material.toughness, knockbackResistance: material.knockbackResistance, enchantability: material.enchantability
  }, referenceMaterial));
}

DEFINITIONS.push(
  ...armorSet("honey", { helmet: { durability: 143, armor: 2 }, chestplate: { durability: 195, armor: 6 }, leggings: { durability: 208, armor: 5 }, boots: { durability: 156, armor: 2 }, toughness: 0, knockbackResistance: 0, enchantability: 18 }, "iron"),
  ...armorSet("licorice", { helmet: { durability: 169, armor: 2 }, chestplate: { durability: 208, armor: 6 }, leggings: { durability: 234, armor: 5 }, boots: { durability: 195, armor: 2 }, toughness: 0, knockbackResistance: 0, enchantability: 12 }, "iron"),
  ...armorSet("pez", { helmet: { durability: 208, armor: 3 }, chestplate: { durability: 260, armor: 8 }, leggings: { durability: 286, armor: 6 }, boots: { durability: 234, armor: 3 }, toughness: 1, knockbackResistance: 0, enchantability: 14 }, "diamond"),
  item("jelly_boots", "armor", "boots", { durability: 143, armor: 2, armorToughness: 0, knockbackResistance: 0, enchantability: 20 }, "iron"),
  item("jelly_crown", "armor", "helmet", { durability: 169, armor: 3, armorToughness: 0, knockbackResistance: 0, enchantability: 20 }, "diamond"),
  item("water_mask", "armor", "helmet", { durability: 169, armor: 1, armorToughness: 0, knockbackResistance: 0, enchantability: 20 }, "turtle")
);

const VANILLA_REFERENCES = {
  materials: {
    wood: { name: "木制", durability: 59, enchantability: 15, bonus: 0 },
    stone: { name: "石制", durability: 131, enchantability: 5, bonus: 1 },
    iron: { name: "铁制", durability: 250, enchantability: 14, bonus: 2 },
    gold: { name: "金制", durability: 32, enchantability: 22, bonus: 0 },
    diamond: { name: "钻石", durability: 1561, enchantability: 10, bonus: 3 },
    netherite: { name: "下界合金", durability: 2031, enchantability: 15, bonus: 4 }
  },
  toolTypes: {
    sword: { damageOffset: 4, speed: 1.6 }, shovel: { damageOffset: 2.5, speed: 1.0 },
    pickaxe: { damageOffset: 2, speed: 1.2 }, axe: { damageOffset: 7, speed: 0.8, overrides: { stone: [9, 0.8], iron: [9, 0.9], diamond: [9, 1.0], netherite: [10, 1.0], gold: [7, 1.0], wood: [7, 0.8] } },
    hoe: { damageOffset: 1, speed: 1.0, overrides: { stone: [1, 2.0], iron: [1, 3.0], diamond: [1, 4.0], netherite: [1, 4.0], gold: [1, 1.0], wood: [1, 1.0] } }
  },
  fixed: {
    trident: { name: "三叉戟", durability: 250, attackDamage: 9, attackSpeed: 1.1, enchantability: 1 },
    bow: { name: "弓", durability: 384, enchantability: 1 }, crossbow: { name: "弩", durability: 465, enchantability: 1 },
    arrow: { name: "箭", durability: 0, enchantability: 0 }, shield: { name: "盾牌", durability: 336, enchantability: 1 }
  },
  armor: {
    leather: { name: "皮革", enchantability: 15, toughness: 0, knockbackResistance: 0, defense: [1, 3, 2, 1], durability: [55, 80, 75, 65] },
    chain: { name: "锁链", enchantability: 12, toughness: 0, knockbackResistance: 0, defense: [2, 5, 4, 1], durability: [165, 240, 225, 195] },
    gold: { name: "金制", enchantability: 25, toughness: 0, knockbackResistance: 0, defense: [2, 5, 3, 1], durability: [77, 112, 105, 91] },
    iron: { name: "铁制", enchantability: 9, toughness: 0, knockbackResistance: 0, defense: [2, 6, 5, 2], durability: [165, 240, 225, 195] },
    diamond: { name: "钻石", enchantability: 10, toughness: 2, knockbackResistance: 0, defense: [3, 8, 6, 3], durability: [363, 528, 495, 429] },
    netherite: { name: "下界合金", enchantability: 15, toughness: 3, knockbackResistance: 0.1, defense: [3, 8, 6, 3], durability: [407, 592, 555, 481] },
    turtle: { name: "海龟壳", enchantability: 9, toughness: 0, knockbackResistance: 0, defense: [2, 0, 0, 0], durability: [275, 0, 0, 0] }
  }
};

function readText(file) { return fs.existsSync(file) ? fs.readFileSync(file, "utf8") : ""; }
function readJson(file, fallback) { try { return JSON.parse(readText(file).replace(/^\uFEFF/, "")); } catch { return fallback; } }
function writeJsonAtomic(file, value) {
  fs.mkdirSync(path.dirname(file), { recursive: true });
  const temp = `${file}.tmp-${process.pid}`;
  fs.writeFileSync(temp, JSON.stringify(value, null, 2) + "\n", "utf8");
  fs.renameSync(temp, file);
}

function textureFor(id) {
  const name = id.slice(namespace.length + 1);
  const model = readJson(path.join(assetsRoot, "models", "item", `${name}.json`), null);
  if (model && model.textures) {
    let texture = model.textures.layer0 || model.textures.texture || model.textures.all;
    if (texture && !texture.startsWith("#") && (!texture.includes(":") || texture.startsWith(`${namespace}:`))) {
      if (texture.startsWith(`${namespace}:`)) texture = texture.slice(namespace.length + 1);
      return `/asset/${texture}.png`;
    }
  }
  return fs.existsSync(path.join(assetsRoot, "textures", "item", `${name}.png`)) ? `/asset/item/${name}.png` : null;
}

function normalizeAttackSpeed(value, fallback = null) {
  const speed = Number(value);
  if (!Number.isFinite(speed)) return fallback;
  if (speed < 0 && speed > -4) return Number((speed + 4).toFixed(2));
  return speed > 0 ? speed : fallback;
}

function normalizedItems(saved) {
  const stored = saved && saved.items && typeof saved.items === "object" ? saved.items : {};
  return Object.fromEntries(DEFINITIONS.map(definition => {
    const profile = { ...definition.defaults, ...(stored[definition.id] || {}), category: definition.category, toolType: definition.toolType };
    if (profile.attackSpeed != null) {
      profile.attackSpeed = normalizeAttackSpeed(profile.attackSpeed, definition.defaults.attackSpeed);
    }
    return [definition.id, profile];
  }));
}

function buildState() {
  const zh = readJson(path.join(assetsRoot, "lang", "zh_cn.json"), {});
  const en = readJson(path.join(assetsRoot, "lang", "en_us.json"), {});
  const fallbackNames = { alchemy_mixer_blade: "炼金搅拌器刀片" };
  const fallbackTextures = { alchemy_mixer_blade: "/asset/item/mixer_light_silver_16x16.png" };
  const profiles = normalizedItems(readJson(configPath, null));
  const items = DEFINITIONS.map(definition => {
    const name = definition.id.slice(namespace.length + 1);
    return {
      id: definition.id,
      name: zh[`item.${namespace}.${name}`] || en[`item.${namespace}.${name}`] || fallbackNames[name] || name,
      texture: textureFor(definition.id) || fallbackTextures[name] || null,
      defaults: definition.defaults,
      profile: profiles[definition.id]
    };
  });
  return { root, configPath, items, enchantments: ENCHANTMENTS, vanillaReferences: VANILLA_REFERENCES };
}

function finiteNumber(value, key, id, min, max, nullable = true) {
  if (value == null && nullable) return null;
  const number = Number(value);
  if (!Number.isFinite(number) || number < min || number > max) throw new Error(`${id}: ${key} 超出范围 ${min} - ${max}`);
  return number;
}

function saveConfig(payload) {
  if (!payload || !payload.items || typeof payload.items !== "object") throw new Error("缺少工具属性数据");
  const items = {};
  for (const definition of DEFINITIONS) {
    const value = payload.items[definition.id];
    if (!value) throw new Error(`缺少 ${definition.id}`);
    items[definition.id] = {
      category: definition.category,
      toolType: definition.toolType,
      referenceMaterial: typeof value.referenceMaterial === "string" ? value.referenceMaterial : definition.referenceMaterial,
      durability: Math.round(finiteNumber(value.durability, "耐久", definition.id, 0, 1000000, false)),
      attackDamage: finiteNumber(value.attackDamage, "攻击伤害", definition.id, -1000, 1000),
      attackSpeed: finiteNumber(normalizeAttackSpeed(value.attackSpeed), "攻击速度", definition.id, 0.1, 100),
      armor: finiteNumber(value.armor, "盔甲值", definition.id, 0, 1000),
      armorToughness: finiteNumber(value.armorToughness, "盔甲韧性", definition.id, 0, 1000),
      knockbackResistance: finiteNumber(value.knockbackResistance, "击退抗性", definition.id, 0, 10),
      enchantability: Math.round(finiteNumber(value.enchantability, "附魔能力值", definition.id, 0, 1000, false)),
      useVanillaEnchantments: Boolean(value.useVanillaEnchantments),
      allowedEnchantments: [...new Set(Array.isArray(value.allowedEnchantments) ? value.allowedEnchantments.filter(id => /^minecraft:[a-z0-9_]+$/.test(id)) : [])].sort()
    };
  }
  writeJsonAtomic(configPath, { format: 1, items });
  return { ok: true, configPath, count: Object.keys(items).length };
}

function sendJson(res, value, status = 200) {
  res.writeHead(status, { "content-type": "application/json; charset=utf-8", "cache-control": "no-store" });
  res.end(JSON.stringify(value, null, 2));
}
function serveFile(res, file, type) {
  if (!fs.existsSync(file)) { res.writeHead(404); return res.end("not found"); }
  res.writeHead(200, { "content-type": type, "cache-control": "no-store" });
  res.end(fs.readFileSync(file));
}
function openBrowser(target) {
  if (process.env.CANDYCRAFT_NO_BROWSER === "1") return;
  const child = childProcess.spawn("rundll32.exe", ["url.dll,FileProtocolHandler", target], { detached: true, stdio: "ignore", windowsHide: true });
  child.unref();
}

const server = http.createServer((req, res) => {
  const parsed = new URL(req.url, "http://127.0.0.1");
  if (req.method === "GET" && parsed.pathname === "/") return serveFile(res, path.join(__dirname, "index.html"), "text/html; charset=utf-8");
  if (req.method === "GET" && parsed.pathname === "/style.css") return serveFile(res, path.join(__dirname, "style.css"), "text/css; charset=utf-8");
  if (req.method === "GET" && parsed.pathname === "/app.js") return serveFile(res, path.join(__dirname, "app.js"), "text/javascript; charset=utf-8");
  if (req.method === "GET" && parsed.pathname === "/api/state") return sendJson(res, buildState());
  if (req.method === "GET" && parsed.pathname.startsWith("/asset/")) {
    const textureRoot = path.resolve(assetsRoot, "textures");
    const file = path.resolve(textureRoot, decodeURIComponent(parsed.pathname.slice(7)).replace(/\//g, path.sep));
    if (file !== textureRoot && !file.startsWith(textureRoot + path.sep)) return sendJson(res, { ok: false, error: "bad asset path" }, 400);
    return serveFile(res, file, "image/png");
  }
  if (req.method === "POST" && parsed.pathname === "/api/save") {
    let body = "";
    req.on("data", chunk => { body += chunk; if (body.length > 3_000_000) req.destroy(); });
    req.on("end", () => { try { sendJson(res, saveConfig(JSON.parse(body))); } catch (error) { sendJson(res, { ok: false, error: String(error.message || error) }, 400); } });
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

async function startAvailableServer() {
  try {
    return await startServer(defaultPort);
  } catch (error) {
    if (!error || error.code !== "EADDRINUSE") throw error;
    return startServer(0);
  }
}

if (require.main === module) {
  const shouldOpen = !process.argv.includes("--no-browser");
  startAvailableServer().then(({ port }) => {
    console.log(`CandyCraft tool property editor: http://127.0.0.1:${port}`);
    if (shouldOpen) openBrowser(`http://127.0.0.1:${port}/?v=1`);
  }).catch(error => {
    console.error(error); process.exitCode = 1;
  });
}

module.exports = { buildState, saveConfig, startServer, startAvailableServer, definitions: DEFINITIONS, normalizeAttackSpeed };
