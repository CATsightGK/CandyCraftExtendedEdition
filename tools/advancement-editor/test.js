const assert = require("assert");
const fs = require("fs");
const os = require("os");
const path = require("path");

const fixture = fs.mkdtempSync(path.join(os.tmpdir(), "candycraft-advancement-editor-"));
process.env.CANDYCRAFT_ROOT = fixture;

function writeJson(relative, value) {
  const file = path.join(fixture, relative);
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, JSON.stringify(value, null, 2) + "\n", "utf8");
}

try {
  writeJson("src/main/resources/assets/candycraftmod/lang/zh_cn.json", { "advancements.candycraft.root.title": "old" });
  writeJson("src/main/resources/assets/candycraftmod/lang/en_us.json", {});
  writeJson("src/main/resources/data/candycraftmod/advancements/root.json", {
    display: {
      icon: { item: "candycraftmod:sugar_block" },
      title: { translate: "advancements.candycraft.root.title" },
      description: { translate: "advancements.candycraft.root.desc" }
    },
    criteria: { complete: { trigger: "minecraft:impossible" } }
  });

  const service = require("./server");
  const state = service.buildState();
  assert.equal(state.advancements.length, 1);
  const root = state.advancements[0];
  root.titleZh = "new title";
  root.titleEn = "New title";
  root.descriptionZh = "new description";
  root.descriptionEn = "New description";
  root.position = { x: 123, y: 456 };
  const saved = service.saveState({ advancements: [root] });
  assert.deepEqual(saved, { ok: true, count: 1, advancementsRoot: path.join(fixture, "src", "main", "resources", "data", "candycraftmod", "advancements") });
  assert.equal(JSON.parse(fs.readFileSync(path.join(fixture, "src/main/resources/assets/candycraftmod/lang/zh_cn.json"), "utf8"))[root.titleKey], "new title");
  assert.deepEqual(JSON.parse(fs.readFileSync(path.join(fixture, "tools/advancement-editor/layout.json"), "utf8")).root, { x: 123, y: 456 });

  const child = JSON.parse(JSON.stringify(root));
  child.id = "child";
  root.data.parent = "candycraftmod:child";
  child.data.parent = "candycraftmod:root";
  assert.throws(() => service.saveState({ advancements: [root, child] }), /contains a cycle/);
  console.log("Advancement editor tests passed");
} finally {
  fs.rmSync(fixture, { recursive: true, force: true });
}
