const $ = id => document.getElementById(id);
const clone = value => JSON.parse(JSON.stringify(value));

const state = {
  tables: [],
  items: [],
  itemMap: new Map(),
  references: {},
  current: null,
  previousId: null,
  dirty: false,
  view: "visual",
  pickerTarget: null
};

function node(tag, className = "", text = "") {
  const element = document.createElement(tag);
  if (className) element.className = className;
  if (text !== "") element.textContent = text;
  return element;
}

function button(className, text, title, onClick) {
  const element = node("button", className, text);
  element.type = "button";
  if (title) element.title = title;
  element.addEventListener("click", onClick);
  return element;
}

function field(type, value, onInput, options = {}) {
  const element = document.createElement("input");
  element.type = type;
  element.value = value ?? "";
  for (const [key, option] of Object.entries(options)) element[key] = option;
  element.addEventListener("input", () => onInput(type === "number" ? Number(element.value) : element.value, element));
  return element;
}

async function api(path, body) {
  const response = await fetch(path, body == null ? undefined : {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(body)
  });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(data.error || `请求失败：${response.status}`);
  return data;
}

let toastTimer;
function toast(message, isError = false) {
  clearTimeout(toastTimer);
  $("toast").textContent = message;
  $("toast").className = `toast show${isError ? " error" : ""}`;
  toastTimer = setTimeout(() => $("toast").className = "toast", 2400);
}

function markDirty() {
  state.dirty = true;
  $("saveState").textContent = "有未保存更改";
  $("saveState").classList.add("dirty");
}

function markClean() {
  state.dirty = false;
  $("saveState").textContent = "已同步";
  $("saveState").classList.remove("dirty");
}

function itemInfo(id) {
  return state.itemMap.get(id) || { id, name: id || "未选择物品", icon: null };
}

function iconElement(id, className) {
  const info = itemInfo(id);
  const holder = node("span", className, info.icon ? "" : "·");
  if (info.icon) {
    const image = document.createElement("img");
    image.src = info.icon;
    image.alt = "";
    holder.append(image);
  }
  return holder;
}

function weight(entry) {
  const value = Number(entry?.weight ?? 1);
  return Number.isFinite(value) && value >= 0 ? value : 0;
}

function rollRange(pool) {
  const rolls = pool?.rolls;
  if (Number.isFinite(Number(rolls))) {
    const value = Math.max(0, Math.round(Number(rolls)));
    return { min: value, max: value, mean: value };
  }
  const min = Math.max(0, Math.round(Number(rolls?.min) || 0));
  const max = Math.max(min, Math.round(Number(rolls?.max) || min));
  return { min, max, mean: (min + max) / 2 };
}

function setCountFunction(entry) {
  return (entry.functions || []).find(fn => fn?.function === "minecraft:set_count") || null;
}

function countRange(entry) {
  const count = setCountFunction(entry)?.count;
  if (Number.isFinite(Number(count))) {
    const value = Math.max(0, Number(count));
    return { min: value, max: value, mean: value };
  }
  if (count && Number.isFinite(Number(count.min)) && Number.isFinite(Number(count.max))) {
    const min = Math.max(0, Number(count.min));
    const max = Math.max(min, Number(count.max));
    return { min, max, mean: (min + max) / 2 };
  }
  return { min: 1, max: 1, mean: 1 };
}

function setCount(entry, min, max) {
  const safeMin = Math.max(0, Number(min) || 0);
  const safeMax = Math.max(safeMin, Number(max) || safeMin);
  entry.functions = Array.isArray(entry.functions) ? entry.functions : [];
  const index = entry.functions.findIndex(fn => fn?.function === "minecraft:set_count");
  if (safeMin === 1 && safeMax === 1) {
    if (index >= 0) entry.functions.splice(index, 1);
  } else {
    const fn = index >= 0 ? entry.functions[index] : { function: "minecraft:set_count" };
    fn.count = safeMin === safeMax ? safeMin : { min: safeMin, max: safeMax };
    if (index < 0) entry.functions.push(fn);
  }
  if (entry.functions.length === 0) delete entry.functions;
}

function chanceAcrossRolls(perRoll, range) {
  if (perRoll <= 0) return 0;
  if (perRoll >= 1 && range.min > 0) return 1;
  let total = 0;
  for (let rolls = range.min; rolls <= range.max; rolls++) total += 1 - Math.pow(1 - perRoll, rolls);
  return total / Math.max(1, range.max - range.min + 1);
}

function poolStats(pool) {
  const entries = Array.isArray(pool.entries) ? pool.entries : [];
  const totalWeight = entries.reduce((sum, entry) => sum + weight(entry), 0);
  const rolls = rollRange(pool);
  return { entries, totalWeight, rolls };
}

function entryChance(pool, entry) {
  const stats = poolStats(pool);
  const perRoll = stats.totalWeight > 0 ? weight(entry) / stats.totalWeight : 0;
  return { perRoll, chest: chanceAcrossRolls(perRoll, stats.rolls) };
}

function aggregateProbabilities() {
  const result = new Map();
  for (const pool of state.current?.json?.pools || []) {
    const stats = poolStats(pool);
    if (stats.totalWeight <= 0) continue;
    const poolItems = new Map();
    for (const entry of stats.entries) {
      if (entry.type !== "minecraft:item" || !entry.name) continue;
      if (!poolItems.has(entry.name)) poolItems.set(entry.name, { probability: 0, expected: 0, weight: 0 });
      const current = poolItems.get(entry.name);
      const selection = weight(entry) / stats.totalWeight;
      current.probability += selection;
      current.expected += selection * stats.rolls.mean * countRange(entry).mean;
      current.weight += weight(entry);
    }
    for (const [id, poolValue] of poolItems) {
      if (!result.has(id)) result.set(id, { id, chance: 0, expected: 0, weight: 0, noChance: 1 });
      const aggregate = result.get(id);
      const poolChance = chanceAcrossRolls(Math.min(1, poolValue.probability), stats.rolls);
      aggregate.noChance *= 1 - poolChance;
      aggregate.expected += poolValue.expected;
      aggregate.weight += poolValue.weight;
    }
  }
  for (const value of result.values()) value.chance = 1 - value.noChance;
  return [...result.values()].sort((a, b) => b.chance - a.chance || b.expected - a.expected || a.id.localeCompare(b.id));
}

function percent(value) {
  if (!Number.isFinite(value) || value <= 0) return "0%";
  const amount = value * 100;
  if (amount < .01) return "<0.01%";
  if (amount < 1) return `${amount.toFixed(2)}%`;
  return `${amount.toFixed(1)}%`;
}

function decimal(value) {
  if (!Number.isFinite(value)) return "0";
  return value < 10 ? value.toFixed(2).replace(/\.00$/, "") : value.toFixed(1).replace(/\.0$/, "");
}

function currentPoolCount() {
  return state.current?.json?.pools?.length || 0;
}

function currentEntryCount() {
  return (state.current?.json?.pools || []).reduce((sum, pool) => sum + (pool.entries?.length || 0), 0);
}

function renderTableList() {
  const query = $("tableSearch").value.trim().toLowerCase();
  const list = $("tableList");
  list.replaceChildren();
  const available = state.previousId || !state.current
    ? state.tables
    : [{ id: state.current.id, json: state.current.json, file: null, unsaved: true }, ...state.tables];
  const tables = available.filter(table => {
    if (!query) return true;
    return table.id.toLowerCase().includes(query) || JSON.stringify(table.json || {}).toLowerCase().includes(query);
  });
  $("tableCount").textContent = state.tables.length;
  if (!tables.length) list.append(node("div", "empty-state", "没有匹配的战利品表"));
  for (const table of tables) {
    const active = table.id === (state.previousId || state.current?.id);
    const entry = node("button", `table-entry${active ? " active" : ""}`);
    entry.type = "button";
    entry.append(node("strong", "", table.id));
    const pools = table.json?.pools || [];
    const meta = node("span", "table-meta");
    meta.append(node("span", "", table.unsaved ? "未保存" : `${pools.length} 奖池`), node("span", "", `${pools.reduce((sum, pool) => sum + (pool.entries?.length || 0), 0)} 条目`));
    entry.append(meta);
    entry.addEventListener("click", () => selectTable(table));
    list.append(entry);
  }
}

function confirmDiscard() {
  return !state.dirty || confirm("当前更改尚未保存，确定放弃吗？");
}

function selectTable(table, force = false) {
  if (!force && !confirmDiscard()) return;
  state.current = { id: table.id, json: clone(table.json) };
  state.previousId = table.file ? table.id : null;
  $("tableId").value = table.id;
  $("jsonEditor").value = JSON.stringify(state.current.json, null, 2);
  markClean();
  renderAll();
}

function renderAll() {
  renderTableList();
  renderPools();
  renderProbabilityList();
  renderReferences();
  $("poolSummary").textContent = `${currentPoolCount()} 个奖池 · ${currentEntryCount()} 个条目`;
  $("deleteButton").disabled = !state.previousId;
  $("duplicateButton").disabled = !state.current;
  $("saveButton").disabled = !state.current;
}

function changeRollMode(pool, mode) {
  const range = rollRange(pool);
  pool.rolls = mode === "range" ? { min: range.min, max: Math.max(range.min, range.max) } : range.min;
  markDirty();
  renderPools();
  refreshCalculatedViews();
}

function renderPoolHead(pool, poolIndex) {
  const head = node("div", "pool-head");
  const identity = node("div", "pool-identity");
  identity.append(node("span", "pool-number", `奖池 ${poolIndex + 1}`));
  const controls = node("div", "roll-controls");
  controls.append(node("span", "", "抽取"));
  const ranged = typeof pool.rolls === "object" && pool.rolls !== null;
  const mode = document.createElement("select");
  [["fixed", "固定"], ["range", "区间"]].forEach(([value, label]) => {
    const option = node("option", "", label); option.value = value; option.selected = value === (ranged ? "range" : "fixed"); mode.append(option);
  });
  mode.addEventListener("change", () => changeRollMode(pool, mode.value));
  controls.append(mode);
  const range = rollRange(pool);
  if (ranged) {
    controls.append(field("number", range.min, value => {
      pool.rolls.min = Math.max(0, Math.round(value));
      if (pool.rolls.max < pool.rolls.min) pool.rolls.max = pool.rolls.min;
      markDirty(); refreshCalculatedViews();
    }, { min: 0, max: 128 }), node("span", "", "至"), field("number", range.max, value => {
      pool.rolls.max = Math.max(Number(pool.rolls.min) || 0, Math.round(value));
      markDirty(); refreshCalculatedViews();
    }, { min: 0, max: 128 }));
  } else {
    controls.append(field("number", range.min, value => {
      pool.rolls = Math.max(0, Math.round(value)); markDirty(); refreshCalculatedViews();
    }, { min: 0, max: 128 }));
  }
  controls.append(node("span", "", "次"));
  identity.append(controls);
  const actions = node("div", "pool-actions");
  actions.append(
    button("small-icon", "↑", "上移奖池", () => moveItem(state.current.json.pools, poolIndex, -1, true)),
    button("small-icon", "↓", "下移奖池", () => moveItem(state.current.json.pools, poolIndex, 1, true)),
    button("small-icon", "×", "删除奖池", () => {
      if (state.current.json.pools.length <= 1) return toast("至少保留一个奖池", true);
      state.current.json.pools.splice(poolIndex, 1); markDirty(); renderAll();
    })
  );
  head.append(identity, actions);
  return head;
}

function advancedLabel(entry) {
  const functionCount = (entry.functions || []).filter(fn => fn?.function !== "minecraft:set_count").length;
  const conditionCount = (entry.conditions || []).length;
  const parts = [];
  if (functionCount) parts.push(`${functionCount} 个其他函数`);
  if (conditionCount) parts.push(`${conditionCount} 个条件`);
  return parts.join(" · ");
}

function renderEntry(pool, poolIndex, entry, entryIndex) {
  const row = node("div", `loot-entry${entry.type === "minecraft:empty" ? " empty-entry" : ""}`);
  row.dataset.pool = poolIndex;
  row.dataset.entry = entryIndex;
  const item = node("div", "item-control");
  if (entry.type === "minecraft:item") {
    item.append(iconElement(entry.name, "item-icon"));
    const id = field("text", entry.name || "", value => { entry.name = value.trim(); markDirty(); refreshCalculatedViews(); }, { spellcheck: false });
    id.className = "item-id";
    id.addEventListener("change", () => { renderPools(); refreshCalculatedViews(); });
    item.append(id, button("picker-button", "⌄", "选择物品", () => openItemPicker(value => {
      entry.name = value; markDirty(); renderPools(); refreshCalculatedViews();
    })));
  } else {
    item.append(iconElement("", "item-icon"), node("span", "empty-label", "空条目（本次不生成物品）"));
  }

  const weightControl = node("div", "entry-weight");
  weightControl.append(field("number", entry.weight ?? 1, value => {
    entry.weight = Math.max(0, Math.round(value)); markDirty(); refreshCalculatedViews();
  }, { min: 0, max: 100000 }), node("span", "", "权重"));

  const counts = countRange(entry);
  const countControl = node("div", "count-controls");
  if (entry.type === "minecraft:item") {
    countControl.append(field("number", counts.min, value => {
      setCount(entry, value, countRange(entry).max); markDirty(); refreshCalculatedViews();
    }, { min: 0, max: 9999 }), node("span", "", "–"), field("number", counts.max, value => {
      setCount(entry, countRange(entry).min, value); markDirty(); refreshCalculatedViews();
    }, { min: 0, max: 9999 }));
  } else {
    countControl.append(node("span", "", "—"));
  }

  const probability = node("div", "entry-probability");
  const probabilityStrong = node("strong", "entry-chance");
  const probabilitySmall = node("span", "entry-per-roll");
  probability.append(probabilityStrong, probabilitySmall);

  const actions = node("div", "entry-actions");
  actions.append(
    button("small-icon", "↕", "切换物品/空条目", () => {
      if (entry.type === "minecraft:item") {
        entry.type = "minecraft:empty"; delete entry.name; delete entry.functions;
      } else {
        entry.type = "minecraft:item"; entry.name = "minecraft:sugar";
      }
      markDirty(); renderPools(); refreshCalculatedViews();
    }),
    button("small-icon", "↑", "上移条目", () => moveItem(pool.entries, entryIndex, -1, false)),
    button("small-icon", "↓", "下移条目", () => moveItem(pool.entries, entryIndex, 1, false)),
    button("small-icon", "×", "删除条目", () => {
      if (pool.entries.length <= 1) return toast("每个奖池至少保留一个条目", true);
      pool.entries.splice(entryIndex, 1); markDirty(); renderAll();
    })
  );
  row.append(item, weightControl, countControl, probability, actions);
  const advanced = advancedLabel(entry);
  if (advanced) row.append(node("div", "entry-advanced", `保留高级配置：${advanced}`));
  return row;
}

function renderPools() {
  const root = $("poolList");
  root.replaceChildren();
  if (!state.current) return root.append(node("div", "empty-state", "请选择一张战利品表"));
  const pools = state.current.json.pools || [];
  pools.forEach((pool, poolIndex) => {
    const section = node("section", "pool");
    section.append(renderPoolHead(pool, poolIndex));
    const header = node("div", "entry-header");
    ["物品 / 条目", "权重", "生成数量", "理论概率", "操作"].forEach(label => header.append(node("span", "", label)));
    section.append(header);
    (pool.entries || []).forEach((entry, entryIndex) => section.append(renderEntry(pool, poolIndex, entry, entryIndex)));
    section.append(button("add-entry", "+ 添加条目", "", () => {
      pool.entries = Array.isArray(pool.entries) ? pool.entries : [];
      pool.entries.push({ type: "minecraft:item", name: "minecraft:sugar", weight: 1 });
      markDirty(); renderAll();
    }));
    root.append(section);
  });
  updateEntryProbabilities();
  $("poolSummary").textContent = `${currentPoolCount()} 个奖池 · ${currentEntryCount()} 个条目`;
}

function updateEntryProbabilities() {
  document.querySelectorAll(".loot-entry[data-pool]").forEach(row => {
    const pool = state.current?.json?.pools?.[Number(row.dataset.pool)];
    const entry = pool?.entries?.[Number(row.dataset.entry)];
    if (!pool || !entry) return;
    const chance = entryChance(pool, entry);
    row.querySelector(".entry-chance").textContent = percent(chance.chest);
    row.querySelector(".entry-per-roll").textContent = `单次 ${percent(chance.perRoll)}`;
  });
}

function renderProbabilityList() {
  const root = $("probabilityList");
  root.replaceChildren();
  const values = aggregateProbabilities();
  if (!values.length) return root.append(node("div", "no-references", "当前没有可统计的物品条目"));
  for (const value of values) {
    const info = itemInfo(value.id);
    const row = node("div", "probability-row");
    row.append(iconElement(value.id, "probability-icon"));
    const name = node("div", "probability-name");
    name.append(node("strong", "", info.name), node("code", "", value.id));
    const numbers = node("div", "probability-values");
    numbers.append(node("strong", "", percent(value.chance)), node("span", "", `期望 ${decimal(value.expected)} 个 · 权重合计 ${value.weight}`));
    const bar = node("div", "probability-bar");
    const fill = node("i"); fill.style.width = `${Math.max(0, Math.min(100, value.chance * 100))}%`; bar.append(fill);
    row.append(name, numbers, bar);
    root.append(row);
  }
}

function renderReferences() {
  const root = $("referenceList");
  root.replaceChildren();
  const key = state.previousId || state.current?.id;
  const references = state.references[key] || [];
  $("referenceCount").textContent = `${references.length} 处`;
  if (!references.length) return root.append(node("div", "no-references", "尚未发现结构引用。新增表保存后可供结构使用。"));
  const kindNames = { template: "NBT 模板", code: "生成代码", worldgen: "结构注册" };
  for (const reference of references) {
    const row = node("div", "reference-row");
    row.append(node("strong", "", reference.label), node("span", "", `${kindNames[reference.kind] || reference.kind}${reference.detail ? ` · ${reference.detail}` : ""}`));
    row.append(node("code", "", `${reference.source}${reference.line ? `:${reference.line}` : ""}`));
    root.append(row);
  }
}

function refreshCalculatedViews() {
  updateEntryProbabilities();
  renderProbabilityList();
  $("poolSummary").textContent = `${currentPoolCount()} 个奖池 · ${currentEntryCount()} 个条目`;
}

function moveItem(array, index, direction, wholeView) {
  const target = index + direction;
  if (target < 0 || target >= array.length) return;
  [array[index], array[target]] = [array[target], array[index]];
  markDirty();
  if (wholeView) renderAll(); else { renderPools(); refreshCalculatedViews(); }
}

function openItemPicker(onSelect) {
  state.pickerTarget = onSelect;
  $("itemPicker").hidden = false;
  $("pickerBackdrop").hidden = false;
  $("itemSearch").value = "";
  renderItemOptions();
  setTimeout(() => $("itemSearch").focus(), 0);
}

function closeItemPicker() {
  $("itemPicker").hidden = true;
  $("pickerBackdrop").hidden = true;
  state.pickerTarget = null;
}

function renderItemOptions() {
  const query = $("itemSearch").value.trim().toLowerCase();
  const values = state.items.filter(item => !query || item.id.toLowerCase().includes(query) || item.name.toLowerCase().includes(query)).slice(0, 250);
  const root = $("itemOptions");
  root.replaceChildren();
  for (const item of values) {
    const option = node("button", "picker-option"); option.type = "button";
    option.append(iconElement(item.id, "picker-icon"));
    const labels = node("span", "picker-label"); labels.append(node("strong", "", item.name), node("code", "", item.id)); option.append(labels);
    option.addEventListener("click", () => { const callback = state.pickerTarget; closeItemPicker(); callback?.(item.id); });
    root.append(option);
  }
}

function newTable() {
  if (!confirmDiscard()) return;
  let id = "new_chest";
  let suffix = 2;
  const ids = new Set(state.tables.map(table => table.id));
  while (ids.has(id)) id = `new_chest_${suffix++}`;
  selectTable({ id, file: null, json: {
    type: "minecraft:chest",
    pools: [{ rolls: 1, entries: [{ type: "minecraft:item", name: "minecraft:sugar", weight: 1 }] }]
  } }, true);
  markDirty();
  $("tableId").focus();
  $("tableId").select();
}

function duplicateTable() {
  if (!state.current || !confirmDiscard()) return;
  const base = `${state.current.id}_copy`;
  let id = base;
  let suffix = 2;
  const ids = new Set(state.tables.map(table => table.id));
  while (ids.has(id)) id = `${base}_${suffix++}`;
  selectTable({ id, file: null, json: clone(state.current.json) }, true);
  markDirty();
}

function applyJson(showSuccess = true) {
  try {
    const json = JSON.parse($("jsonEditor").value);
    if (!json || typeof json !== "object" || Array.isArray(json)) throw new Error("根节点必须是对象");
    state.current.json = json;
    markDirty();
    renderPools(); refreshCalculatedViews();
    if (showSuccess) toast("JSON 已应用到可视化编辑器");
    return true;
  } catch (error) {
    toast(`JSON 无效：${error.message}`, true);
    return false;
  }
}

async function saveTable() {
  if (!state.current) return;
  if (state.view === "json" && !applyJson(false)) return;
  const id = $("tableId").value.trim();
  try {
    const result = await api("/api/save", { id, previousId: state.previousId, json: state.current.json });
    toast(`已保存 ${result.id}`);
    await reload(result.id);
  } catch (error) { toast(error.message, true); }
}

async function deleteTable() {
  if (!state.previousId) return;
  if (!confirm(`确定删除战利品表 candycraftmod:chests/${state.previousId} 吗？`)) return;
  try {
    await api("/api/delete", { id: state.previousId });
    toast(`已删除 ${state.previousId}`);
    await reload();
  } catch (error) { toast(error.message, true); }
}

function setView(view) {
  if (view === "visual" && state.view === "json" && state.dirty && !applyJson(false)) return;
  state.view = view;
  document.querySelectorAll(".view-tabs button").forEach(tab => tab.classList.toggle("active", tab.dataset.view === view));
  $("visualView").classList.toggle("active", view === "visual");
  $("jsonView").classList.toggle("active", view === "json");
  if (view === "json") $("jsonEditor").value = JSON.stringify(state.current?.json || {}, null, 2);
}

async function reload(selectedId = null) {
  const data = await api("/api/state");
  state.tables = data.tables.filter(table => table.json);
  state.items = data.items;
  state.itemMap = new Map(data.items.map(item => [item.id, item]));
  state.references = data.references || {};
  $("workspacePath").textContent = data.root;
  const selected = state.tables.find(table => table.id === selectedId) || state.tables[0];
  if (selected) selectTable(selected, true);
  else {
    state.current = null; state.previousId = null; markClean(); renderAll();
  }
}

$("newButton").addEventListener("click", newTable);
$("duplicateButton").addEventListener("click", duplicateTable);
$("deleteButton").addEventListener("click", deleteTable);
$("saveButton").addEventListener("click", saveTable);
$("addPoolButton").addEventListener("click", () => {
  if (!state.current) return;
  state.current.json.pools = Array.isArray(state.current.json.pools) ? state.current.json.pools : [];
  state.current.json.pools.push({ rolls: 1, entries: [{ type: "minecraft:item", name: "minecraft:sugar", weight: 1 }] });
  markDirty(); renderAll();
});
$("tableSearch").addEventListener("input", renderTableList);
$("tableId").addEventListener("input", event => { if (!state.current) return; state.current.id = event.target.value.trim(); markDirty(); });
$("jsonEditor").addEventListener("input", markDirty);
$("applyJsonButton").addEventListener("click", () => applyJson(true));
document.querySelectorAll(".view-tabs button").forEach(tab => tab.addEventListener("click", () => setView(tab.dataset.view)));
$("itemSearch").addEventListener("input", renderItemOptions);
$("closePicker").addEventListener("click", closeItemPicker);
$("pickerBackdrop").addEventListener("click", closeItemPicker);
document.addEventListener("keydown", event => {
  if (event.key === "Escape" && !$("itemPicker").hidden) closeItemPicker();
  if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === "s") { event.preventDefault(); saveTable(); }
});
window.addEventListener("beforeunload", event => { if (state.dirty) { event.preventDefault(); event.returnValue = ""; } });

reload().catch(error => toast(`加载失败：${error.message}`, true));
