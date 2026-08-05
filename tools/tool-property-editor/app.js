const ui = Object.fromEntries(["projectPath", "configPath", "statusText", "filters", "searchInput", "itemCount", "dirtyCount", "itemList", "emptyState", "editor", "selectedIcon", "selectedCategory", "selectedName", "selectedId", "resetSelectedButton", "statHint", "statGrid", "vanillaEnchantToggle", "enchantCount", "enchantSearch", "enchantmentGrid", "referenceSelect", "referenceVisual", "comparisonTable", "saveButton", "resetAllButton", "importButton", "importFile", "exportButton", "toast"].map(id => [id, document.getElementById(id)]));

const categoryLabels = { all: "全部", melee: "近战", tool: "工具", ranged: "远程", armor: "盔甲", other: "其他" };
const typeLabels = { sword: "剑", shovel: "锹", pickaxe: "镐", axe: "斧", hoe: "锄", spear: "长矛", fork: "叉子", bow: "弓", crossbow: "弩", wand: "魔杖", projectile: "弹药", helmet: "头盔", chestplate: "胸甲", leggings: "护腿", boots: "靴子", other: "其他" };
const fieldDefinitions = {
  durability: { label: "耐久度", detail: "0 表示不可损坏", step: 1, digits: 0 },
  attackDamage: { label: "攻击伤害", detail: "游戏提示显示的最终伤害", step: 0.5, digits: 2 },
  attackSpeed: { label: "攻击速度", detail: "每秒攻击次数，必须大于 0", step: 0.1, digits: 2, min: 0.1 },
  armor: { label: "盔甲值", detail: "该部位提供的护甲点数", step: 1, digits: 1 },
  armorToughness: { label: "盔甲韧性", detail: "降低高伤害穿透", step: 0.5, digits: 2 },
  knockbackResistance: { label: "击退抗性", detail: "1.0 等于 100%", step: 0.05, digits: 2 },
  enchantability: { label: "附魔能力值", detail: "数值越高越容易获得高等级附魔", step: 1, digits: 0 }
};

const state = { data: null, items: [], byId: new Map(), selectedId: null, category: "all", dirty: new Set() };
const clone = value => JSON.parse(JSON.stringify(value));
const shortId = id => id.replace("candycraftmod:", "");

function normalizeAttackSpeed(value, fallback = null) {
  const speed = Number(value);
  if (!Number.isFinite(speed)) return fallback;
  if (speed < 0 && speed > -4) return Number((speed + 4).toFixed(2));
  return speed > 0 ? speed : fallback;
}

function normalizeProfile(profile, defaults) {
  const normalized = { ...profile };
  if (normalized.attackSpeed != null) {
    normalized.attackSpeed = normalizeAttackSpeed(normalized.attackSpeed, defaults.attackSpeed);
  }
  return normalized;
}

function showToast(message) { ui.toast.textContent = message; ui.toast.className = "show"; setTimeout(() => ui.toast.className = "", 1800); }
function format(value, digits = 2) { if (value == null) return "—"; return Number.isInteger(Number(value)) ? String(value) : Number(value).toFixed(digits).replace(/0+$/, "").replace(/\.$/, ""); }
function markDirty(id) { state.dirty.add(id); updateSummary(); renderList(); ui.statusText.textContent = "有未保存修改"; }
function updateSummary() { ui.dirtyCount.textContent = state.dirty.size ? `${state.dirty.size} 项已修改` : "未修改"; }

function buildFilters() {
  ui.filters.innerHTML = "";
  for (const category of Object.keys(categoryLabels)) {
    const button = document.createElement("button");
    button.type = "button"; button.textContent = categoryLabels[category]; button.classList.toggle("active", state.category === category);
    button.addEventListener("click", () => { state.category = category; buildFilters(); renderList(); });
    ui.filters.appendChild(button);
  }
}

function visibleItems() {
  const query = ui.searchInput.value.trim().toLocaleLowerCase();
  return state.items.filter(item => (state.category === "all" || item.profile.category === state.category) && (!query || `${item.name} ${item.id}`.toLocaleLowerCase().includes(query)));
}

function renderList() {
  const visible = visibleItems();
  ui.itemCount.textContent = `${visible.length} 项`;
  ui.itemList.innerHTML = "";
  for (const item of visible) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = `item-row${item.id === state.selectedId ? " active" : ""}${state.dirty.has(item.id) ? " dirty" : ""}`;
    button.innerHTML = `${item.texture ? `<img src="${item.texture}" alt="">` : "<span></span>"}<span><strong>${escapeHtml(item.name)}</strong><small>${shortId(item.id)}</small></span><i class="dirty-dot"></i>`;
    button.addEventListener("click", () => selectItem(item.id));
    ui.itemList.appendChild(button);
  }
}

function escapeHtml(value) { return String(value).replace(/[&<>"']/g, char => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[char])); }

function selectItem(id) {
  state.selectedId = id;
  renderList(); renderEditor();
}

function currentItem() { return state.byId.get(state.selectedId); }

function renderEditor() {
  const item = currentItem();
  ui.emptyState.hidden = Boolean(item); ui.editor.hidden = !item;
  if (!item) { renderReference(null); return; }
  ui.selectedIcon.src = item.texture || ""; ui.selectedIcon.style.visibility = item.texture ? "visible" : "hidden";
  ui.selectedCategory.textContent = `${categoryLabels[item.profile.category] || item.profile.category} · ${typeLabels[item.profile.toolType] || item.profile.toolType}`;
  ui.selectedName.textContent = item.name; ui.selectedId.textContent = item.id;
  renderStats(item); renderEnchantments(item); renderReference(item);
}

function relevantFields(profile) {
  const fields = ["durability"];
  if (profile.attackDamage != null || ["melee", "tool"].includes(profile.category)) fields.push("attackDamage", "attackSpeed");
  if (profile.category === "armor") fields.push("armor", "armorToughness", "knockbackResistance");
  fields.push("enchantability");
  return fields;
}

function renderStats(item) {
  ui.statGrid.innerHTML = "";
  const fields = relevantFields(item.profile);
  ui.statHint.textContent = `${fields.length} 个可编辑属性`;
  for (const key of fields) {
    const definition = fieldDefinitions[key];
    const row = document.createElement("div"); row.className = "stat-field";
    const minimum = definition.min == null ? "" : ` min="${definition.min}"`;
    row.innerHTML = `<div><label>${definition.label}</label><small>${definition.detail}</small></div><div class="number-control"><button type="button" aria-label="减少">−</button><input type="number" step="${definition.step}"${minimum} value="${item.profile[key] == null ? 0 : item.profile[key]}"><button type="button" aria-label="增加">+</button></div>`;
    const input = row.querySelector("input"); const [minus, plus] = row.querySelectorAll("button");
    const apply = value => {
      const bounded = definition.min == null ? value : Math.max(definition.min, value);
      item.profile[key] = definition.digits === 0 ? Math.round(bounded) : Number(bounded.toFixed(definition.digits));
      input.value = item.profile[key]; markDirty(item.id); renderReference(item);
    };
    input.addEventListener("input", () => {
      if (input.value === "" || !Number.isFinite(Number(input.value))) return;
      item.profile[key] = definition.digits === 0 ? Math.round(Number(input.value)) : Number(Number(input.value).toFixed(definition.digits));
      markDirty(item.id); renderReference(item);
    });
    input.addEventListener("change", () => apply(Number(input.value) || 0));
    minus.addEventListener("click", () => apply((Number(input.value) || 0) - definition.step));
    plus.addEventListener("click", () => apply((Number(input.value) || 0) + definition.step));
    ui.statGrid.appendChild(row);
  }
}

function renderEnchantments(item) {
  const profile = item.profile;
  ui.vanillaEnchantToggle.checked = Boolean(profile.useVanillaEnchantments);
  ui.enchantmentGrid.classList.toggle("disabled", profile.useVanillaEnchantments);
  const query = ui.enchantSearch.value.trim().toLocaleLowerCase();
  const selected = new Set(profile.allowedEnchantments || []);
  ui.enchantCount.textContent = profile.useVanillaEnchantments ? "由物品类型自动判断" : `${selected.size} 项已允许`;
  ui.enchantmentGrid.innerHTML = "";
  let previousGroup = null;
  for (const enchantment of state.data.enchantments.filter(value => !query || `${value.name} ${value.id}`.toLocaleLowerCase().includes(query))) {
    if (enchantment.group !== previousGroup) {
      const heading = document.createElement("div"); heading.className = "enchant-group"; heading.textContent = enchantment.group; ui.enchantmentGrid.appendChild(heading); previousGroup = enchantment.group;
    }
    const label = document.createElement("label"); label.className = "enchant-option";
    label.innerHTML = `<input type="checkbox" ${selected.has(enchantment.id) ? "checked" : ""}><span>${enchantment.name}</span><small>最高 ${enchantment.maxLevel}</small>`;
    label.querySelector("input").addEventListener("change", event => {
      if (event.target.checked) selected.add(enchantment.id); else selected.delete(enchantment.id);
      profile.allowedEnchantments = [...selected]; markDirty(item.id); renderEnchantments(item);
    });
    ui.enchantmentGrid.appendChild(label);
  }
}

function referenceOptions(item) {
  if (!item) return [];
  const profile = item.profile; const refs = state.data.vanillaReferences;
  if (profile.category === "armor") return Object.entries(refs.armor).map(([id, value]) => ({ id, name: value.name }));
  if (refs.toolTypes[profile.toolType]) return Object.entries(refs.materials).map(([id, value]) => ({ id, name: `${value.name}${typeLabels[profile.toolType]}` }));
  const fixedId = ["spear", "fork"].includes(profile.toolType) ? "trident" : profile.toolType === "wand" ? "bow" : profile.referenceMaterial;
  const fixed = refs.fixed[fixedId]; return fixed ? [{ id: fixedId, name: fixed.name }] : [];
}

function computeReference(item) {
  if (!item) return null;
  const p = item.profile, refs = state.data.vanillaReferences;
  if (p.category === "armor") {
    const key = refs.armor[p.referenceMaterial] ? p.referenceMaterial : "iron"; const value = refs.armor[key];
    const index = { helmet: 0, chestplate: 1, leggings: 2, boots: 3 }[p.toolType] || 0;
    return { key, name: `${value.name}${typeLabels[p.toolType]}`, durability: value.durability[index], armor: value.defense[index], armorToughness: value.toughness, knockbackResistance: value.knockbackResistance, enchantability: value.enchantability };
  }
  if (refs.toolTypes[p.toolType]) {
    const key = refs.materials[p.referenceMaterial] ? p.referenceMaterial : "iron", material = refs.materials[key], type = refs.toolTypes[p.toolType], override = type.overrides && type.overrides[key];
    return { key, name: `${material.name}${typeLabels[p.toolType]}`, durability: material.durability, attackDamage: override ? override[0] : material.bonus + type.damageOffset, attackSpeed: override ? override[1] : type.speed, enchantability: material.enchantability };
  }
  const key = ["spear", "fork"].includes(p.toolType) ? "trident" : p.toolType === "wand" ? "bow" : p.referenceMaterial;
  return refs.fixed[key] ? { key, ...refs.fixed[key] } : null;
}

function renderReference(item) {
  const options = referenceOptions(item); ui.referenceSelect.innerHTML = options.map(option => `<option value="${option.id}">${option.name}</option>`).join("");
  if (!item || !options.length) { ui.referenceVisual.innerHTML = "<span>无对应原版物品</span>"; ui.comparisonTable.innerHTML = ""; ui.referenceSelect.disabled = true; return; }
  ui.referenceSelect.disabled = options.length <= 1;
  if (!options.some(option => option.id === item.profile.referenceMaterial)) item.profile.referenceMaterial = options[0].id;
  ui.referenceSelect.value = item.profile.referenceMaterial;
  const reference = computeReference(item);
  ui.referenceVisual.innerHTML = `<div class="reference-glyph">${typeGlyph(item.profile.toolType)}<small>${escapeHtml(reference.name)}</small></div>`;
  const fields = relevantFields(item.profile).filter(key => reference[key] != null);
  ui.comparisonTable.innerHTML = `<div class="comparison-head"><span>属性</span><span>当前</span><span>原版</span><span>差值</span></div>` + fields.map(key => {
    const current = Number(item.profile[key] || 0), base = Number(reference[key] || 0), delta = current - base, css = delta > 0 ? "up" : delta < 0 ? "down" : "same";
    const difference = key === "attackSpeed" && delta !== 0
      ? `${format(Math.abs(delta))} ${delta > 0 ? "更快" : "更慢"}`
      : `${delta > 0 ? "+" : ""}${format(delta)}`;
    return `<div class="comparison-row"><span>${fieldDefinitions[key].label}</span><span>${format(current)}</span><span>${format(base)}</span><span class="delta ${css}">${difference}</span></div>`;
  }).join("");
}

function typeGlyph(type) { return ({ sword: "S", spear: "T", fork: "F", shovel: "V", pickaxe: "P", axe: "A", hoe: "H", bow: "B", crossbow: "X", wand: "W", projectile: "→", helmet: "H", chestplate: "C", leggings: "L", boots: "B" }[type] || "I"); }

async function save() {
  ui.saveButton.disabled = true; ui.statusText.textContent = "正在保存";
  try {
    const items = Object.fromEntries(state.items.map(item => [item.id, item.profile]));
    const response = await fetch("/api/save", { method: "POST", headers: { "content-type": "application/json" }, body: JSON.stringify({ items }) });
    const result = await response.json(); if (!result.ok) throw new Error(result.error || "保存失败");
    state.dirty.clear(); updateSummary(); renderList(); ui.statusText.textContent = `已保存 ${result.count} 项`; showToast("已保存 tool_properties.json");
  } catch (error) { ui.statusText.textContent = "保存失败"; alert(error.message || error); }
  finally { ui.saveButton.disabled = false; }
}

function resetSelected() { const item = currentItem(); if (!item) return; item.profile = clone(item.defaults); markDirty(item.id); renderEditor(); }
function resetAll() { if (!confirm("将全部工具恢复为当前模组默认值？")) return; for (const item of state.items) { item.profile = clone(item.defaults); state.dirty.add(item.id); } updateSummary(); renderList(); renderEditor(); }
function exportJson() {
  const data = { format: 1, items: Object.fromEntries(state.items.map(item => [item.id, item.profile])) };
  const link = document.createElement("a"); link.href = URL.createObjectURL(new Blob([JSON.stringify(data, null, 2) + "\n"], { type: "application/json" })); link.download = "candycraft-tool-properties.json"; link.click(); URL.revokeObjectURL(link.href);
}
async function importJson(file) {
  try {
    const data = JSON.parse(await file.text()); if (!data.items) throw new Error("JSON 中没有 items");
    for (const item of state.items) if (data.items[item.id]) {
      item.profile = normalizeProfile({ ...item.profile, ...clone(data.items[item.id]), category: item.defaults.category, toolType: item.defaults.toolType }, item.defaults);
      state.dirty.add(item.id);
    }
    updateSummary(); renderList(); renderEditor(); showToast("已导入，尚未保存");
  } catch (error) { alert(`导入失败：${error.message || error}`); }
}

async function initialize() {
  try {
    const response = await fetch("/api/state"); state.data = await response.json(); state.items = state.data.items.map(item => ({ ...item, defaults: clone(item.defaults), profile: normalizeProfile(clone(item.profile), item.defaults) })); state.byId = new Map(state.items.map(item => [item.id, item]));
    ui.projectPath.textContent = state.data.root; ui.configPath.textContent = state.data.configPath; ui.statusText.textContent = `已加载 ${state.items.length} 项`;
    buildFilters(); renderList(); if (state.items.length) selectItem(state.items[0].id);
  } catch (error) { ui.statusText.textContent = "连接失败"; ui.emptyState.textContent = error.message || error; }
}

ui.searchInput.addEventListener("input", renderList);
ui.enchantSearch.addEventListener("input", () => { const item = currentItem(); if (item) renderEnchantments(item); });
ui.vanillaEnchantToggle.addEventListener("change", () => { const item = currentItem(); if (!item) return; item.profile.useVanillaEnchantments = ui.vanillaEnchantToggle.checked; markDirty(item.id); renderEnchantments(item); });
ui.referenceSelect.addEventListener("change", () => { const item = currentItem(); if (!item) return; item.profile.referenceMaterial = ui.referenceSelect.value; markDirty(item.id); renderReference(item); });
ui.saveButton.addEventListener("click", save); ui.resetSelectedButton.addEventListener("click", resetSelected); ui.resetAllButton.addEventListener("click", resetAll); ui.exportButton.addEventListener("click", exportJson);
ui.importButton.addEventListener("click", () => ui.importFile.click()); ui.importFile.addEventListener("change", () => { if (ui.importFile.files[0]) importJson(ui.importFile.files[0]); ui.importFile.value = ""; });
document.addEventListener("keydown", event => { if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === "s") { event.preventDefault(); save(); } });
window.addEventListener("beforeunload", event => { if (state.dirty.size) { event.preventDefault(); event.returnValue = ""; } });
initialize();
