const http = require("http");
const fs = require("fs");
const path = require("path");
const crypto = require("crypto");
const { spawn } = require("child_process");

const toolRoot = __dirname;
const configuredProject = process.env.CANDYCRAFT_PROJECT_ROOT ||
  (fs.existsSync(path.join(toolRoot, "project_path.txt")) ? fs.readFileSync(path.join(toolRoot, "project_path.txt"), "utf8").trim() : "");
const projectRoot = configuredProject ? path.resolve(configuredProject) : path.resolve(toolRoot, "..", "..");
const roots = {
  current: path.join(projectRoot, "src", "main", "resources", "assets", "candycraftmod"),
  classic: path.join(projectRoot, "src", "main", "resources", "resourcepacks", "candycraft_classic", "assets", "candycraftmod")
};
const clientRoot = path.join(projectRoot, "src", "main", "java", "com", "valentin4311", "candycraftmod", "client");
const blockbenchRoot = path.join(projectRoot, "output", "blockbench");
const port = Number(process.env.PORT || 4321);
const mime = {
  ".html": "text/html; charset=utf-8", ".css": "text/css; charset=utf-8",
  ".js": "text/javascript; charset=utf-8", ".json": "application/json; charset=utf-8",
  ".png": "image/png", ".txt": "text/plain; charset=utf-8"
};

function walk(directory, predicate, output = []) {
  if (!fs.existsSync(directory)) return output;
  for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
    const full = path.join(directory, entry.name);
    if (entry.isDirectory()) walk(full, predicate, output);
    else if (predicate(full)) output.push(full);
  }
  return output;
}

function canonicalTexture(relative) {
  return relative.replace(/\\/g, "/")
    .replace(/^textures\/(blocks|items)\//, (_, type) => `textures/${type === "blocks" ? "block" : "item"}/`);
}

function pngSize(file) {
  const data = fs.readFileSync(file);
  if (data.length < 24 || data.toString("ascii", 1, 4) !== "PNG") return { width: 0, height: 0 };
  return { width: data.readUInt32BE(16), height: data.readUInt32BE(20) };
}

function readAnimation(file) {
  const metaFile = file + ".mcmeta";
  if (!fs.existsSync(metaFile)) return null;
  try {
    const value = JSON.parse(fs.readFileSync(metaFile, "utf8")).animation || {};
    return {
      frametime: Math.max(1, Number(value.frametime) || 1),
      interpolate: Boolean(value.interpolate),
      width: Number(value.width) || null,
      height: Number(value.height) || null,
      frames: Array.isArray(value.frames) ? value.frames : null
    };
  } catch {
    return { frametime: 1, interpolate: false, frames: null, invalid: true };
  }
}

function scanTextures() {
  const scanned = {};
  for (const [edition, root] of Object.entries(roots)) {
    const textureRoot = path.join(root, "textures");
    scanned[edition] = walk(textureRoot, value => value.toLowerCase().endsWith(".png")).map(file => {
      const relative = path.relative(root, file).replace(/\\/g, "/");
      const data = fs.readFileSync(file);
      return {
        url: `/asset/${edition}/${relative.split("/").map(encodeURIComponent).join("/")}`,
        relative, ...pngSize(file), animation: readAnimation(file),
        hash: crypto.createHash("sha1").update(data).digest("hex")
      };
    });
  }

  const currentByPath = new Map(scanned.current.map(source => [source.relative, source]));
  const usedCurrent = new Set();
  const records = scanned.classic.map(classic => {
    const current = currentByPath.get(classic.relative) || currentByPath.get(canonicalTexture(classic.relative));
    if (current) usedCurrent.add(current.relative);
    return { key: classic.relative, classic, current };
  });
  for (const current of scanned.current) {
    if (!usedCurrent.has(current.relative)) records.push({ key: current.relative, current });
  }

  const normalizedCategory = { blocks: "block", items: "item", mob_effect: "effect", misc: "other", slot: "gui" };
  return records.map(record => {
    const rawSegment = record.key.split("/")[1] || "other";
    const segment = normalizedCategory[rawSegment] || rawSegment;
    record.category = ["block", "item", "entity", "gui", "particle", "effect", "environment", "fluid", "models"].includes(segment) ? segment : "other";
    record.status = !record.classic ? "current-only" : !record.current ? "classic-only" :
      record.classic.hash === record.current.hash ? "same" : "changed";
    record.animated = Boolean(record.current?.animation || record.classic?.animation);
    return record;
  }).sort((a, b) => a.key.localeCompare(b.key));
}

function numbers(value) {
  return value.split(",").map(part => {
    const expression = part.trim()
      .replace(/\(\s*float\s*\)/g, "")
      .replace(/Math\.PI/g, String(Math.PI))
      .replace(/([0-9.])[fFdD]\b/g, "$1");
    if (!/^[0-9eE+\-*/().\s]+$/.test(expression)) return Number.NaN;
    try {
      return Number(Function(`"use strict"; return (${expression});`)());
    } catch {
      return Number.NaN;
    }
  });
}

function parseModels() {
  const result = [];
  for (const file of walk(clientRoot, value => value.endsWith("Model.java"))) {
    const source = fs.readFileSync(file, "utf8");
    const className = path.basename(file, ".java");
    const bodyStart = source.indexOf("createBodyLayer()");
    if (bodyStart < 0) continue;
    const bodyEnd = source.indexOf("return LayerDefinition.create", bodyStart);
    if (bodyEnd < 0) continue;
    const bodySource = source.slice(bodyStart, source.indexOf(";", bodyEnd) + 1)
      .replace(/\(\(float\)\s*Math\.PI\s*\/\s*([0-9.]+)F?\)/g, (_, divisor) => String(Math.PI / Number(divisor)) + "F");
    const layer = bodySource.match(/LayerDefinition\.create\(\s*mesh\s*,\s*(\d+)\s*,\s*(\d+)\s*\)/);
    if (!layer) continue;
    const parts = [];
    const variableParents = { root: null };
    const callPattern = /(?:PartDefinition\s+(\w+)\s*=\s*)?([\w.()]+)\.addOrReplaceChild\(\s*"([^"]+)"\s*,\s*CubeListBuilder\.create\(\)([\s\S]*?),\s*PartPose\.(offsetAndRotation|offset)\(([^)]*)\)\s*\)/g;
    let match;
    while ((match = callPattern.exec(bodySource))) {
      const [, assigned, receiver, name, builder, poseType, poseRaw] = match;
      const cubes = [];
      const cubePattern = /(?:texOffs\(\s*(-?\d+)\s*,\s*(-?\d+)\s*\)\s*)?\.?(?:(mirror)\(\)\s*\.\s*)?addBox\(([^)]*)\)/g;
      let cube;
      let lastU = 0, lastV = 0;
      while ((cube = cubePattern.exec(builder))) {
        if (cube[1] != null) { lastU = Number(cube[1]); lastV = Number(cube[2]); }
        const values = numbers(cube[4]);
        if (values.length >= 6) cubes.push({
          u: lastU, v: lastV, mirrored: Boolean(cube[3]),
          from: values.slice(0, 3), size: values.slice(3, 6)
        });
      }
      const pose = numbers(poseRaw);
      const parent = receiver.includes("getRoot()") ? null : variableParents[receiver] ?? null;
      parts.push({ name, parent, cubes, position: pose.slice(0, 3), rotation: poseType === "offsetAndRotation" ? pose.slice(3, 6) : [0, 0, 0] });
      if (assigned) variableParents[assigned] = name;
    }
    const helperPattern = /add\(\s*root\s*,\s*"([^"]+)"\s*,\s*([^;]+?)\);/g;
    while ((match = helperPattern.exec(bodySource))) {
      const values = numbers(match[2]);
      if (values.length >= 14) {
        parts.push({
          name: match[1], parent: null,
          cubes: [{ u: values[0], v: values[1], mirrored: false, from: values.slice(2, 5), size: values.slice(5, 8) }],
          position: values.slice(8, 11), rotation: values.slice(11, 14)
        });
      }
    }
    if (parts.some(part => part.cubes.length)) {
      const stem = className.replace(/Model$/, "");
      const model = { id: className, name: stem.replace(/([a-z])([A-Z])/g, "$1 $2"), textureWidth: Number(layer[1]), textureHeight: Number(layer[2]), parts };
      model.bbmodel = loadOrConvertBbmodel(model);
      applyNeutralPreviewPose(model);
      result.push(model);
    }
  }
  return result.sort((a, b) => a.name.localeCompare(b.name));
}

function bbmodelGroups(outliner, output = []) {
  for (const child of outliner || []) {
    if (!child || typeof child !== "object") continue;
    output.push(child);
    bbmodelGroups(child.children, output);
  }
  return output;
}

function applyNeutralPreviewPose(model) {
  const groups = new Map(bbmodelGroups(model.bbmodel.outliner).map(group => [group.name, group]));
  if (model.id === "GummyBearModel") {
    for (const name of ["body", "body_outer"]) {
      const group = groups.get(name);
      if (group) group.rotation = [-90, 0, 0];
    }
  }
  if (model.id === "DragonModel") {
    for (const name of ["left_wing", "right_wing", "left_scale_wing", "right_scale_wing"]) {
      const group = groups.get(name);
      if (group) group.visibility = false;
    }
  }
  if (model.id === "PingouinModel") {
    const crest = groups.get("crest");
    if (crest) crest.visibility = false;
  }
  if (model.id === "BeetleModel") {
    model.previewRotation = [0, 90, 0];
  }
}

function boxUvFaces(cube) {
  const width = Math.abs(cube.size[0]);
  const height = Math.abs(cube.size[1]);
  const depth = Math.abs(cube.size[2]);
  const u = cube.u;
  const v = cube.v;
  const faces = [
    { name: "east", from: [0, depth], size: [depth, height] },
    { name: "west", from: [depth + width, depth], size: [depth, height] },
    { name: "up", from: [depth + width, depth], size: [-width, -depth] },
    { name: "down", from: [depth + width * 2, 0], size: [-width, depth] },
    { name: "south", from: [depth * 2 + width, depth], size: [width, height] },
    { name: "north", from: [depth, depth], size: [width, height] }
  ];
  if (cube.mirrored) {
    for (const face of faces) {
      face.from[0] += face.size[0];
      face.size[0] *= -1;
    }
    [faces[0].from, faces[1].from] = [faces[1].from, faces[0].from];
    [faces[0].size, faces[1].size] = [faces[1].size, faces[0].size];
  }
  return Object.fromEntries(faces.map(face => [face.name, {
    uv: [
      u + face.from[0], v + face.from[1],
      u + face.from[0] + face.size[0], v + face.from[1] + face.size[1]
    ],
    texture: 0
  }]));
}

function cleanBbmodel(value) {
  return {
    meta: value.meta,
    name: value.name,
    modded_entity_flip_y: value.modded_entity_flip_y,
    resolution: value.resolution,
    elements: value.elements,
    outliner: value.outliner
  };
}

function loadOrConvertBbmodel(model) {
  if (model.id === "SuguardModel") {
    const source = path.join(blockbenchRoot, "suguard", "suguard.bbmodel");
    if (fs.existsSync(source)) return cleanBbmodel(JSON.parse(fs.readFileSync(source, "utf8")));
  }

  const byName = new Map(model.parts.map(part => [part.name, part]));
  const absolutePivots = new Map();
  function absolutePivot(part) {
    if (absolutePivots.has(part.name)) return absolutePivots.get(part.name);
    const parent = part.parent ? byName.get(part.parent) : null;
    const parentPivot = parent ? absolutePivot(parent) : [0, 0, 0];
    const pivot = part.position.map((value, index) => value + parentPivot[index]);
    absolutePivots.set(part.name, pivot);
    return pivot;
  }

  const elements = [];
  const groups = new Map();
  for (const part of model.parts) {
    const pivot = absolutePivot(part);
    const origin = [-pivot[0], 24 - pivot[1], pivot[2]];
    const group = {
      name: part.name,
      origin,
      rotation: [
        -part.rotation[0] * 180 / Math.PI,
        -part.rotation[1] * 180 / Math.PI,
        part.rotation[2] * 180 / Math.PI
      ],
      uuid: `group:${model.id}:${part.name}`,
      visibility: true,
      children: []
    };
    part.cubes.forEach((cube, cubeIndex) => {
      const x0 = origin[0] - cube.from[0] - cube.size[0];
      const y0 = origin[1] - cube.from[1] - cube.size[1];
      const z0 = origin[2] + cube.from[2];
      const x1 = x0 + cube.size[0];
      const y1 = y0 + cube.size[1];
      const z1 = z0 + cube.size[2];
      const element = {
        name: part.cubes.length > 1 ? `${part.name}_${cubeIndex + 1}` : part.name,
        type: "cube",
        uuid: `cube:${model.id}:${part.name}:${cubeIndex}`,
        from: [Math.min(x0, x1), Math.min(y0, y1), Math.min(z0, z1)],
        to: [Math.max(x0, x1), Math.max(y0, y1), Math.max(z0, z1)],
        origin,
        box_uv: true,
        uv_offset: [cube.u, cube.v],
        mirror_uv: cube.mirrored,
        faces: boxUvFaces(cube)
      };
      elements.push(element);
      group.children.push(element.uuid);
    });
    groups.set(part.name, group);
  }
  const outliner = [];
  for (const part of model.parts) {
    const group = groups.get(part.name);
    if (part.parent && groups.has(part.parent)) groups.get(part.parent).children.push(group);
    else outliner.push(group);
  }
  return {
    meta: { format_version: "4.10", model_format: "modded_entity", box_uv: true },
    name: model.id.replace(/Model$/, ""),
    modded_entity_flip_y: true,
    resolution: { width: model.textureWidth, height: model.textureHeight },
    elements,
    outliner
  };
}

function parseEntityCatalog(models, textures) {
  const catalog = new Map(models.map(model => [model.id, {
    modelId: model.id, name: model.name, textures: new Set(), layers: new Set(), renderers: new Set()
  }]));
  for (const file of walk(clientRoot, value => value.endsWith("Renderer.java"))) {
    const source = fs.readFileSync(file, "utf8");
    const modelIds = new Set([...source.matchAll(/new\s+(\w+Model)\s*(?:<[^>]*>)?\s*\(/g)].map(match => match[1]));
    const targets = [...modelIds].map(id => catalog.get(id)).filter(Boolean);
    if (!targets.length) continue;
    const texturePaths = new Set([...source.matchAll(/["'](textures\/entity\/[^"']+?\.png)["']/g)].map(match => match[1]));
    const helper = source.match(/["']textures\/entity\/([^"']*)["']\s*\+\s*name(?:\s*\+\s*["']([^"']*)["'])?/);
    if (helper) {
      for (const call of source.matchAll(/\btexture\(\s*["']([^"']+)["']\s*\)/g)) {
        texturePaths.add(`textures/entity/${helper[1]}${call[1]}${helper[2] || ""}`);
      }
    }
    for (const target of targets) {
      target.renderers.add(path.basename(file, ".java"));
      for (const texturePath of texturePaths) {
        if (/(?:_glow|_eyes|_fur|saddle)\.png$/i.test(texturePath) || /\/[^/]*fur[^/]*\.png$/i.test(texturePath)) target.layers.add(texturePath);
        else target.textures.add(texturePath);
      }
    }
  }
  const available = new Set(textures.filter(item => item.category === "entity").map(item => item.key));
  for (const entry of catalog.values()) {
    const layerOverrides = {
      GummyBunnyModel: ["textures/entity/whitebunny.png"],
      WaffleSheepModel: ["textures/entity/sheepcandy.png"]
    };
    for (const key of layerOverrides[entry.modelId] || []) {
      entry.textures.delete(key);
      entry.layers.add(key);
    }
    if (!entry.textures.size) {
      const needle = entry.modelId.replace(/Model$/, "").toLowerCase();
      for (const key of available) {
        if (key.toLowerCase().replace(/[^a-z0-9]/g, "").includes(needle.replace(/[^a-z0-9]/g, ""))) entry.textures.add(key);
      }
    }
    const baseNames = [...entry.textures].map(key => path.posix.basename(key, ".png").replace(/[0-9]+$/, ""));
    for (const key of available) {
      const name = path.posix.basename(key, ".png");
      if (/(?:_glow|_eyes)$/.test(name) && baseNames.some(base => name.startsWith(base))) entry.layers.add(key);
    }
  }
  return [...catalog.values()].filter(entry => entry.textures.size).map(entry => ({
    modelId: entry.modelId, name: entry.name,
    textures: [...entry.textures].filter(key => available.has(key)).sort(),
    layers: [...entry.layers].filter(key => available.has(key)).sort(),
    renderers: [...entry.renderers].sort()
  })).sort((a, b) => a.name.localeCompare(b.name));
}

let cache = null;
function getIndex() {
  if (!cache) {
    const textures = scanTextures();
    const models = parseModels();
    cache = {
      generatedAt: new Date().toISOString(), projectRoot,
      textures, models, entities: parseEntityCatalog(models, textures),
      counts: {
        total: textures.length,
        current: walk(path.join(roots.current, "textures"), value => value.toLowerCase().endsWith(".png")).length,
        classic: walk(path.join(roots.classic, "textures"), value => value.toLowerCase().endsWith(".png")).length,
        changed: textures.filter(item => item.status === "changed").length,
        animated: textures.filter(item => item.animated).length
      }
    };
  }
  return cache;
}

function safeFile(root, relative) {
  const file = path.resolve(root, relative);
  return file === root || file.startsWith(root + path.sep) ? file : null;
}

function sendFile(file, res) {
  fs.readFile(file, (error, data) => {
    if (error) return res.writeHead(error.code === "ENOENT" ? 404 : 500).end("Not found");
    res.writeHead(200, {
      "Content-Type": mime[path.extname(file).toLowerCase()] || "application/octet-stream",
      "Cache-Control": "no-store", "Access-Control-Allow-Origin": "*"
    });
    res.end(data);
  });
}

function serve(req, res) {
  const url = new URL(req.url, `http://${req.headers.host}`);
  const pathname = decodeURIComponent(url.pathname);
  if (pathname === "/api/index") {
    if (url.searchParams.get("refresh") === "1") cache = null;
    res.writeHead(200, { "Content-Type": mime[".json"], "Cache-Control": "no-store" });
    return res.end(JSON.stringify(getIndex()));
  }
  const asset = pathname.match(/^\/asset\/(current|classic)\/(.+)$/);
  if (asset) {
    const file = safeFile(roots[asset[1]], asset[2]);
    return file ? sendFile(file, res) : res.writeHead(403).end("Forbidden");
  }
  const relative = pathname === "/" ? "index.html" : pathname.replace(/^\/+/, "");
  const file = safeFile(toolRoot, relative);
  return file ? sendFile(file, res) : res.writeHead(403).end("Forbidden");
}

if (require.main === module) {
  const server = http.createServer(serve);
  server.listen(port, "127.0.0.1", () => {
    const url = `http://127.0.0.1:${port}/`;
    console.log(`CandyCraft resource comparison studio: ${url}`);
    if (!process.env.CANDYCRAFT_NO_BROWSER && !process.argv.includes("--no-browser")) {
      spawn("cmd", ["/c", "start", "", url], { detached: true, stdio: "ignore", windowsHide: true }).unref();
    }
  });
}

module.exports = { boxUvFaces, parseModels, scanTextures, parseEntityCatalog, getIndex };
