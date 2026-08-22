import * as THREE from "./vendor/three.module.js";
import { OrbitControls } from "./vendor/OrbitControls.js";
import { animationsFor, applyEntityAnimation } from "./entity-animations.js";

const $ = selector => document.querySelector(selector);
const $$ = selector => [...document.querySelectorAll(selector)];
const state = {
  index: null, selected: null, filtered: [], images: {}, frame: 0, playing: true,
  speed: 1, viewMode: "side", split: 50, blinkEdition: "current", actualPixels: false,
  entityEdition: "current", lastFrameAt: performance.now(), nextFrameDelay: 50,
  entityAnimationId: null, entityAnimationPlaying: true, entityAnimationTime: 0,
  entityAnimationSpeed: 1, entityAnimationLastAt: performance.now(), turntableAngle: 0
};

function labelForKey(key) {
  return key.split("/").pop().replace(/\.png$/i, "").replace(/_/g, " ");
}

function statusText(status) {
  return ({ changed: "已变化", same: "相同", "current-only": "仅现有", "classic-only": "仅经典" })[status] || status;
}

function debounce(fn, wait = 120) {
  let timer;
  return (...args) => { clearTimeout(timer); timer = setTimeout(() => fn(...args), wait); };
}

async function loadIndex(refresh = false) {
  $("#loading").classList.remove("hidden");
  const response = await fetch("/api/index" + (refresh ? "?refresh=1" : ""));
  if (!response.ok) throw new Error("无法读取资源索引");
  state.index = await response.json();
  populateTextureFilters();
  populateEntities();
  $("#loading").classList.add("hidden");
}

function populateTextureFilters() {
  filterTextures();
  if (!state.selected) {
    const preferred = state.index.textures.find(item => item.key.includes("textures/entity/sugarde.png"))
      || state.index.textures.find(item => item.status === "changed") || state.index.textures[0];
    if (preferred) selectTexture(preferred.key);
  }
}

function filterTextures() {
  const query = $("#searchInput").value.trim().toLowerCase();
  const category = $("#categoryFilter").value;
  const status = $("#statusFilter").value;
  state.filtered = state.index.textures.filter(item => {
    if (category !== "all" && item.category !== category) return false;
    if (status === "animated" ? !item.animated : status !== "all" && item.status !== status) return false;
    return !query || item.key.toLowerCase().includes(query) || labelForKey(item.key).includes(query);
  });
  $("#resultCount").textContent = state.filtered.length.toLocaleString();
  renderTextureList();
}

function renderTextureList() {
  const selectedKey = state.selected?.key;
  $("#textureList").innerHTML = state.filtered.map(item => {
    const preview = item.current || item.classic;
    return `<button class="asset-row ${item.key === selectedKey ? "selected" : ""}" data-key="${escapeHtml(item.key)}">
      <span class="asset-thumb"><img loading="lazy" src="${preview.url}" alt=""></span>
      <span class="asset-label"><strong>${escapeHtml(labelForKey(item.key))}</strong><small>${escapeHtml(item.key.replace("textures/", ""))}${item.animated ? " · 动态" : ""}</small></span>
      <i class="state-dot ${item.status}"></i>
    </button>`;
  }).join("");
}

function escapeHtml(value) {
  return value.replace(/[&<>"']/g, char => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[char]);
}

function loadImage(source) {
  if (!source) return Promise.resolve(null);
  if (state.images[source.url]) return Promise.resolve(state.images[source.url]);
  return new Promise((resolve, reject) => {
    const image = new Image();
    image.onload = () => { state.images[source.url] = image; resolve(image); };
    image.onerror = reject;
    image.src = source.url;
  });
}

async function selectTexture(key) {
  const item = state.index.textures.find(texture => texture.key === key);
  if (!item) return;
  state.selected = item;
  state.frame = 0;
  state.playing = true;
  state.lastFrameAt = performance.now();
  $$(".asset-row").forEach(row => row.classList.toggle("selected", row.dataset.key === key));
  try {
    const [classic, current] = await Promise.all([loadImage(item.classic), loadImage(item.current)]);
    state.textureImages = { classic, current };
    updateTextureInspector();
    updateAnimationControls();
    drawTexturePreviews();
  } catch (error) {
    console.error(error);
  }
}

function frameInfo(source, image, requestedFrame) {
  if (!source || !image) return null;
  const animation = source.animation;
  if (!animation) return { index: 0, duration: Infinity, x: 0, y: 0, width: image.width, height: image.height, count: 1 };
  const width = animation.width || image.width;
  const height = animation.height || Math.min(width, image.height);
  const columns = Math.max(1, Math.floor(image.width / width));
  const naturalCount = Math.max(1, columns * Math.floor(image.height / height));
  const frames = animation.frames?.length ? animation.frames : Array.from({ length: naturalCount }, (_, index) => index);
  const entry = frames[((requestedFrame % frames.length) + frames.length) % frames.length];
  const index = typeof entry === "number" ? entry : Number(entry.index) || 0;
  const ticks = typeof entry === "object" && entry.time ? Number(entry.time) : animation.frametime;
  return {
    index, duration: Math.max(1, ticks) * 50,
    x: (index % columns) * width, y: Math.floor(index / columns) * height,
    width, height, count: frames.length
  };
}

function masterFrameCount() {
  const item = state.selected;
  if (!item) return 1;
  return Math.max(...["classic", "current"].map(edition => frameInfo(item[edition], state.textureImages?.[edition], 0)?.count || 1));
}

function drawEdition(canvas, edition) {
  const source = state.selected?.[edition];
  const image = state.textureImages?.[edition];
  const info = frameInfo(source, image, state.frame);
  const figure = canvas.closest("figure");
  if (!info) {
    if (figure) figure.classList.add("empty");
    canvas.width = canvas.height = 1;
    return;
  }
  if (figure) figure.classList.remove("empty");
  canvas.width = info.width;
  canvas.height = info.height;
  canvas.getContext("2d").drawImage(image, info.x, info.y, info.width, info.height, 0, 0, info.width, info.height);
  if (state.actualPixels) {
    canvas.style.width = info.width + "px";
    canvas.style.height = info.height + "px";
  } else {
    canvas.style.width = canvas.style.height = "";
  }
}

function drawCompare() {
  const canvas = $("#compareCanvas");
  const editions = state.viewMode === "blink" ? [state.blinkEdition] : ["classic", "current"];
  const infos = {};
  for (const edition of editions) infos[edition] = frameInfo(state.selected?.[edition], state.textureImages?.[edition], state.frame);
  const available = Object.values(infos).filter(Boolean);
  if (!available.length) { canvas.width = canvas.height = 1; return; }
  const width = Math.max(...available.map(info => info.width));
  const height = Math.max(...available.map(info => info.height));
  canvas.width = width; canvas.height = height;
  const context = canvas.getContext("2d");
  context.imageSmoothingEnabled = false;
  const paint = (edition, left, right) => {
    const info = infos[edition];
    const image = state.textureImages?.[edition];
    if (!info || !image || right <= left) return;
    context.save();
    context.beginPath(); context.rect(left, 0, right - left, height); context.clip();
    context.drawImage(image, info.x, info.y, info.width, info.height, 0, 0, width, height);
    context.restore();
  };
  if (state.viewMode === "blink") {
    paint(state.blinkEdition, 0, width);
    $(".label-a").textContent = state.blinkEdition === "classic" ? "经典" : "现有";
    $(".label-b").classList.add("hidden");
  } else {
    const split = width * state.split / 100;
    paint("classic", 0, split);
    paint("current", split, width);
    $(".label-a").textContent = "经典";
    $(".label-b").classList.remove("hidden");
  }
  if (state.actualPixels) { canvas.style.width = width + "px"; canvas.style.height = height + "px"; }
  else { canvas.style.width = canvas.style.height = ""; }
}

function drawTexturePreviews() {
  if (!state.selected || !state.textureImages) return;
  drawEdition($("#classicCanvas"), "classic");
  drawEdition($("#currentCanvas"), "current");
  drawCompare();
  const count = masterFrameCount();
  $("#frameRange").max = Math.max(0, count - 1);
  $("#frameRange").value = state.frame % count;
  $("#frameText").textContent = count > 1 ? `帧 ${(state.frame % count) + 1} / ${count}` : "静态材质";
}

function updateTextureInspector() {
  const item = state.selected;
  $("#assetName").textContent = labelForKey(item.key);
  $("#assetPath").textContent = item.key;
  $("#statusBadge").textContent = statusText(item.status);
  $("#statusBadge").className = "badge " + item.status;
  const dimension = source => source ? `${source.width} × ${source.height}` : "不存在";
  const animation = source => source?.animation ? `是 · ${source.animation.frames?.length || "自动"} 帧序列` : "否";
  $("#textureDetails").innerHTML = [
    ["状态", statusText(item.status)], ["分类", item.category], ["经典尺寸", dimension(item.classic)],
    ["现有尺寸", dimension(item.current)], ["经典动态", animation(item.classic)], ["现有动态", animation(item.current)],
    ["经典路径", item.classic?.relative || "—"], ["现有路径", item.current?.relative || "—"]
  ].map(([term, value]) => `<dt>${term}</dt><dd>${escapeHtml(String(value))}</dd>`).join("");
}

function updateAnimationControls() {
  const animated = masterFrameCount() > 1;
  $("#playPause").disabled = !animated;
  $("#prevFrame").disabled = !animated;
  $("#nextFrame").disabled = !animated;
  $("#frameRange").disabled = !animated;
  $("#playPause").textContent = state.playing ? "Ⅱ" : "▶";
}

function stepFrame(direction) {
  const count = masterFrameCount();
  state.frame = (state.frame + direction + count) % count;
  state.lastFrameAt = performance.now();
  drawTexturePreviews();
}

let textureBlinkAt = performance.now();
function animationLoop(now) {
  if (state.playing && masterFrameCount() > 1) {
    const infos = ["current", "classic"].map(edition => frameInfo(state.selected?.[edition], state.textureImages?.[edition], state.frame)).filter(Boolean);
    const delay = Math.min(...infos.map(info => info.duration)) / state.speed;
    if (now - state.lastFrameAt >= delay) {
      state.lastFrameAt = now;
      state.frame = (state.frame + 1) % masterFrameCount();
      drawTexturePreviews();
    }
  }
  if (now - textureBlinkAt > 850 && (state.viewMode === "blink" || state.entityEdition === "blink")) {
    textureBlinkAt = now;
    state.blinkEdition = state.blinkEdition === "classic" ? "current" : "classic";
    if (state.viewMode === "blink") drawCompare();
    if (state.entityEdition === "blink") updateEntityMaterial();
  }
  requestAnimationFrame(animationLoop);
}

function setViewMode(mode) {
  state.viewMode = mode;
  $$("#viewMode button").forEach(button => button.classList.toggle("active", button.dataset.mode === mode));
  $("#sidePreview").classList.toggle("hidden", mode !== "side");
  $("#splitPreview").classList.toggle("hidden", mode === "side");
  $("#splitRange").classList.toggle("hidden", mode === "blink");
  drawTexturePreviews();
}

// Entity stage
let renderer, scene, camera, controls, modelRoot3d, requiredLayerRoot3d, glowRoot3d, grid, floor;
const textureLoader = new THREE.TextureLoader();

function initEntityStage() {
  const canvas = $("#entityCanvas");
  renderer = new THREE.WebGLRenderer({ canvas, antialias: true, alpha: false, preserveDrawingBuffer: true });
  renderer.setPixelRatio(Math.min(devicePixelRatio, 2));
  renderer.shadowMap.enabled = true;
  renderer.shadowMap.type = THREE.PCFSoftShadowMap;
  scene = new THREE.Scene();
  scene.background = new THREE.Color("#181a1c");
  camera = new THREE.PerspectiveCamera(35, 1, .01, 100);
  camera.position.set(3.8, 2.7, -5.2);
  controls = new OrbitControls(camera, canvas);
  controls.enableDamping = true;
  controls.target.set(0, .9, 0);
  controls.update();
  scene.add(new THREE.HemisphereLight(0xfff1d2, 0x37484b, 2.2));
  const key = new THREE.DirectionalLight(0xffffff, 3.1);
  key.position.set(4, 7, -5); key.castShadow = true; key.shadow.mapSize.set(1024, 1024);
  scene.add(key);
  const rim = new THREE.DirectionalLight(0x76c5bd, 1.6); rim.position.set(-5, 2, 4); scene.add(rim);
  grid = new THREE.GridHelper(12, 24, 0x6f7775, 0x343a3a); grid.position.y = 0; scene.add(grid);
  floor = new THREE.Mesh(new THREE.PlaneGeometry(20, 20), new THREE.ShadowMaterial({ color: 0x000000, opacity: .26 }));
  floor.rotation.x = -Math.PI / 2; floor.receiveShadow = true; floor.position.y = -.005; scene.add(floor);
  new ResizeObserver(resizeEntityStage).observe($("#entityStage"));
  animateEntity();
}

function resizeEntityStage() {
  if (!renderer) return;
  const box = $("#entityStage").getBoundingClientRect();
  if (!box.width || !box.height) return;
  renderer.setSize(box.width, box.height, false);
  camera.aspect = box.width / box.height;
  camera.updateProjectionMatrix();
}

function boxUvFaces(element) {
  const size = element.from.map((value, index) => Math.abs(element.to[index] - value));
  const [width, height, depth] = size;
  const [u, v] = element.uv_offset || [0, 0];
  const faces = [
    { name: "east", from: [0, depth], size: [depth, height] },
    { name: "west", from: [depth + width, depth], size: [depth, height] },
    { name: "up", from: [depth + width, depth], size: [-width, -depth] },
    { name: "down", from: [depth + width * 2, 0], size: [-width, depth] },
    { name: "south", from: [depth * 2 + width, depth], size: [width, height] },
    { name: "north", from: [depth, depth], size: [width, height] }
  ];
  if (element.mirror_uv) {
    for (const face of faces) {
      face.from[0] += face.size[0];
      face.size[0] *= -1;
    }
    [faces[0].from, faces[1].from] = [faces[1].from, faces[0].from];
    [faces[0].size, faces[1].size] = [faces[1].size, faces[0].size];
  }
  return Object.fromEntries(faces.map(face => [face.name, [
    u + face.from[0], v + face.from[1],
    u + face.from[0] + face.size[0], v + face.from[1] + face.size[1]
  ]]));
}

function createBbCubeGeometry(element, resolution, relativeOrigin) {
  const inflate = Number(element.inflate) || 0;
  const stretch = Array.isArray(element.stretch) ? element.stretch : [1, 1, 1];
  const from = [];
  const to = [];
  for (let axis = 0; axis < 3; axis++) {
    const low = Math.min(element.from[axis], element.to[axis]);
    const high = Math.max(element.from[axis], element.to[axis]);
    const center = (low + high) / 2;
    const half = ((high - low) / 2 + inflate) * (Number(stretch[axis]) || 1);
    from[axis] = center - half;
    to[axis] = center + half;
    if (from[axis] === to[axis]) to[axis] += .001;
  }

  const width = to[0] - from[0];
  const height = to[1] - from[1];
  const depth = to[2] - from[2];
  const origin = relativeOrigin || [0, 0, 0];
  const geometry = new THREE.BoxGeometry(width, height, depth);
  geometry.translate(
    (from[0] + to[0]) / 2 - origin[0],
    (from[1] + to[1]) / 2 - origin[1],
    (from[2] + to[2]) / 2 - origin[2]
  );

  const faceOrder = ["east", "west", "up", "down", "south", "north"];
  const generatedUvs = element.box_uv ? boxUvFaces(element) : null;
  const vertexUvs = geometry.attributes.uv;
  const indices = [];
  faceOrder.forEach((name, index) => {
    const face = element.faces?.[name];
    if (!face || face.texture === null) return;
    let uv = generatedUvs?.[name] || face.uv;
    if (!Array.isArray(uv)) return;
    uv = uv.slice();
    if (element.box_uv) {
      for (let axis = 0; axis < 2; axis++) {
        const margin = uv[axis] > uv[axis + 2] ? -1 / 64 : 1 / 64;
        uv[axis] += margin;
        uv[axis + 2] -= margin;
      }
    }
    const arr = [
      [uv[0] / resolution.width, 1 - uv[1] / resolution.height],
      [uv[2] / resolution.width, 1 - uv[1] / resolution.height],
      [uv[0] / resolution.width, 1 - uv[3] / resolution.height],
      [uv[2] / resolution.width, 1 - uv[3] / resolution.height]
    ];
    let rotation = ((Number(face.rotation) || 0) % 360 + 360) % 360;
    while (rotation > 0) {
      const first = arr[0];
      arr[0] = arr[2];
      arr[2] = arr[3];
      arr[3] = arr[1];
      arr[1] = first;
      rotation -= 90;
    }
    arr.forEach((pair, vertex) => vertexUvs.array.set(pair, index * 8 + vertex * 2));
    indices.push(index * 4, index * 4 + 2, index * 4 + 1, index * 4 + 2, index * 4 + 3, index * 4 + 1);
  });
  geometry.setIndex(indices);
  vertexUvs.needsUpdate = true;
  return geometry;
}

function createModel(model, material, isGlow = false) {
  const bbmodel = model.bbmodel;
  const root = new THREE.Group();
  const elements = new Map(bbmodel.elements.map(element => [element.uuid, element]));
  const rendered = new Set();
  const bones = new Map();
  const javaParts = new Map(model.parts.map(part => [part.name, part]));
  const radians = value => (Number(value) || 0) * Math.PI / 180;

  function addElement(element, parent, groupOrigin) {
    rendered.add(element.uuid);
    const elementOrigin = element.origin || groupOrigin;
    const hasRotation = Array.isArray(element.rotation) && element.rotation.some(value => Number(value));
    const holder = hasRotation ? new THREE.Group() : parent;
    if (hasRotation) {
      holder.position.set(
        elementOrigin[0] - groupOrigin[0],
        elementOrigin[1] - groupOrigin[1],
        elementOrigin[2] - groupOrigin[2]
      );
      holder.rotation.order = "ZYX";
      holder.rotation.set(...element.rotation.map(radians));
      parent.add(holder);
    }
    const geometryOrigin = hasRotation ? elementOrigin : groupOrigin;
    const geometry = createBbCubeGeometry(element, bbmodel.resolution, geometryOrigin);
    const mesh = new THREE.Mesh(geometry, material);
    mesh.visible = element.visibility !== false;
    mesh.castShadow = !isGlow;
    mesh.receiveShadow = !isGlow;
    if (isGlow) mesh.scale.multiplyScalar(1.006);
    holder.add(mesh);
  }

  function addGroup(definition, parent, parentOrigin) {
    const origin = definition.origin || parentOrigin;
    const group = new THREE.Group();
    group.name = definition.name || "group";
    group.visible = definition.visibility !== false;
    group.position.set(
      origin[0] - parentOrigin[0],
      origin[1] - parentOrigin[1],
      origin[2] - parentOrigin[2]
    );
    group.rotation.order = "ZYX";
    group.rotation.set(...(definition.rotation || [0, 0, 0]).map(radians));
    group.userData.basePosition = group.position.clone();
    group.userData.baseRotation = group.rotation.clone();
    group.userData.baseVisibility = group.visible;
    group.userData.javaBasePosition = javaParts.get(group.name)?.position?.slice() || [0, 0, 0];
    bones.set(group.name, group);
    parent.add(group);
    for (const child of definition.children || []) {
      if (typeof child === "string") {
        const element = elements.get(child);
        if (element) addElement(element, group, origin);
      } else if (child && typeof child === "object") {
        addGroup(child, group, origin);
      }
    }
  }

  for (const child of bbmodel.outliner || []) {
    if (typeof child === "string") {
      const element = elements.get(child);
      if (element) addElement(element, root, [0, 0, 0]);
    } else if (child && typeof child === "object") {
      addGroup(child, root, [0, 0, 0]);
    }
  }
  for (const element of elements.values()) {
    if (!rendered.has(element.uuid)) addElement(element, root, [0, 0, 0]);
  }
  if (Array.isArray(model.previewRotation)) {
    root.rotation.order = "ZYX";
    root.rotation.set(...model.previewRotation.map(radians));
  }
  root.userData.bones = bones;
  root.userData.basePosition = root.position.clone();
  root.userData.baseRotation = root.rotation.clone();
  root.scale.setScalar(.095);
  return root;
}

function captureRootBase(root) {
  if (!root) return;
  root.userData.basePosition = root.position.clone();
  root.userData.baseRotation = root.rotation.clone();
}

function loadThreeTexture(url) {
  return new Promise((resolve, reject) => textureLoader.load(url, texture => {
    texture.magFilter = THREE.NearestFilter;
    texture.minFilter = THREE.NearestFilter;
    texture.colorSpace = THREE.SRGBColorSpace;
    resolve(texture);
  }, undefined, reject));
}

async function rebuildEntity() {
  const model = state.index.models.find(item => item.id === $("#modelSelect").value);
  const record = state.index.textures.find(item => item.key === $("#entityTextureSelect").value);
  if (!model || !record) return;
  if (modelRoot3d) scene.remove(modelRoot3d);
  if (requiredLayerRoot3d) scene.remove(requiredLayerRoot3d);
  if (glowRoot3d) scene.remove(glowRoot3d);
  modelRoot3d = null; requiredLayerRoot3d = null; glowRoot3d = null;
  $("#entityError").classList.add("hidden");
  const edition = effectiveEntityEdition();
  const source = record[edition] || record.current || record.classic;
  try {
    const texture = await loadThreeTexture(source.url);
    const material = new THREE.MeshStandardMaterial({ map: texture, transparent: true, alphaTest: .05, roughness: .78, metalness: 0, side: THREE.DoubleSide });
    modelRoot3d = createModel(model, material);
    scene.add(modelRoot3d);
    centerModel(modelRoot3d);
    captureRootBase(modelRoot3d);
    await rebuildRequiredLayers(model);
    await rebuildGlow(model);
    applyCurrentEntityAnimation();
    updateEntityInspector(model, record, edition);
  } catch (error) {
    $("#entityError").textContent = "这张贴图无法载入实体舞台";
    $("#entityError").classList.remove("hidden");
    console.error(error);
  }
}

async function rebuildRequiredLayers(model) {
  if (requiredLayerRoot3d) { scene.remove(requiredLayerRoot3d); requiredLayerRoot3d = null; }
  if (model.id !== "GummyBunnyModel") return;
  const record = state.index.textures.find(item => item.key === "textures/entity/whitebunny.png");
  if (!record) return;
  const edition = effectiveEntityEdition();
  const source = record[edition] || record.current || record.classic;
  if (!source) return;
  const texture = await loadThreeTexture(source.url);
  const material = new THREE.MeshStandardMaterial({
    map: texture, transparent: true, alphaTest: .02, opacity: .82,
    roughness: .8, depthWrite: false, side: THREE.DoubleSide
  });
  requiredLayerRoot3d = createModel(model, material, true);
  requiredLayerRoot3d.position.copy(modelRoot3d.position);
  requiredLayerRoot3d.rotation.copy(modelRoot3d.rotation);
  captureRootBase(requiredLayerRoot3d);
  scene.add(requiredLayerRoot3d);
}

async function rebuildGlow(model) {
  if (glowRoot3d) { scene.remove(glowRoot3d); glowRoot3d = null; }
  if (!$("#glowToggle").checked || !$("#glowTextureSelect").value) return;
  const record = state.index.textures.find(item => item.key === $("#glowTextureSelect").value);
  if (!record) return;
  const edition = effectiveEntityEdition();
  const source = record[edition] || record.current || record.classic;
  if (!source) return;
  const texture = await loadThreeTexture(source.url);
  const emissive = /(?:glow|eyes|_eye)/i.test(record.key);
  const material = emissive
    ? new THREE.MeshBasicMaterial({
        map: texture, transparent: true, alphaTest: .03, opacity: .92,
        blending: THREE.AdditiveBlending, depthWrite: false, side: THREE.DoubleSide
      })
    : new THREE.MeshStandardMaterial({
        map: texture, transparent: true, alphaTest: .03, opacity: .72,
        roughness: .82, depthWrite: false, side: THREE.DoubleSide
      });
  glowRoot3d = createModel(model, material, true);
  glowRoot3d.position.copy(modelRoot3d.position);
  glowRoot3d.rotation.copy(modelRoot3d.rotation);
  captureRootBase(glowRoot3d);
  scene.add(glowRoot3d);
}

function currentEntityAnimation() {
  const modelId = $("#modelSelect").value;
  const animations = animationsFor(modelId);
  return animations.find(animation => animation.id === state.entityAnimationId) || animations[0] || null;
}

function populateEntityAnimations() {
  const animations = animationsFor($("#modelSelect").value);
  const previous = animations.find(animation => animation.id === state.entityAnimationId);
  state.entityAnimationId = previous?.id || animations[0]?.id || null;
  state.entityAnimationTime = 0;
  state.entityAnimationPlaying = Boolean(animations.length);
  state.entityAnimationLastAt = performance.now();
  state.turntableAngle = 0;
  $("#entityAnimationList").innerHTML = animations.map(animation =>
    `<button data-animation="${escapeHtml(animation.id)}" class="${animation.id === state.entityAnimationId ? "active" : ""}">${escapeHtml(animation.name)}</button>`
  ).join("") || `<span class="empty-animation">没有独立动画</span>`;
  updateEntityAnimationControls();
}

function selectEntityAnimation(id) {
  if (!animationsFor($("#modelSelect").value).some(animation => animation.id === id)) return;
  state.entityAnimationId = id;
  state.entityAnimationTime = 0;
  state.entityAnimationPlaying = true;
  state.entityAnimationLastAt = performance.now();
  $$("#entityAnimationList button").forEach(button => button.classList.toggle("active", button.dataset.animation === id));
  updateEntityAnimationControls();
  applyCurrentEntityAnimation();
}

function updateEntityAnimationControls() {
  const animation = currentEntityAnimation();
  const disabled = !animation;
  $("#entityAnimationRestart").disabled = disabled;
  $("#entityAnimationPlay").disabled = disabled;
  $("#entityAnimationRange").disabled = disabled;
  $("#entityAnimationPlay").textContent = state.entityAnimationPlaying ? "Ⅱ" : "▶";
  $("#entityAnimationPlay").title = state.entityAnimationPlaying ? "暂停动画" : "播放动画";
  $("#entityAnimationRange").max = animation?.duration || 0;
  $("#entityAnimationRange").value = Math.min(state.entityAnimationTime, animation?.duration || 0);
  $("#entityAnimationTime").textContent = animation
    ? `${state.entityAnimationTime.toFixed(2)} / ${animation.duration.toFixed(2)} 秒`
    : "无动画";
}

function applyCurrentEntityAnimation() {
  const animation = currentEntityAnimation();
  if (!animation) return;
  applyEntityAnimation(modelRoot3d, animation, state.entityAnimationTime);
  applyEntityAnimation(requiredLayerRoot3d, animation, state.entityAnimationTime);
  applyEntityAnimation(glowRoot3d, animation, state.entityAnimationTime);
}

function effectiveEntityEdition() {
  return state.entityEdition === "blink" ? state.blinkEdition : state.entityEdition;
}

function centerModel(root) {
  root.position.set(0, 0, 0);
  const box = new THREE.Box3().setFromObject(root);
  const center = box.getCenter(new THREE.Vector3());
  root.position.x -= center.x;
  root.position.z -= center.z;
  root.position.y -= box.min.y;
  const adjusted = new THREE.Box3().setFromObject(root);
  const size = adjusted.getSize(new THREE.Vector3());
  controls.target.set(0, Math.max(.45, size.y * .45), 0);
  const distance = Math.max(3.2, Math.max(size.x, size.y, size.z) * 2.15);
  camera.position.set(distance * .65, Math.max(1.8, size.y * .7), -distance);
  camera.near = .01; camera.far = 100; camera.updateProjectionMatrix(); controls.update();
}

function updateEntityInspector(model, record, edition) {
  $("#entityTitle").textContent = model.name;
  $("#entityEditionLabel").textContent = edition === "classic" ? "经典材质" : "现有材质";
  const source = record[edition] || record.current || record.classic;
  const cubes = model.parts.reduce((sum, part) => sum + part.cubes.length, 0);
  const catalog = selectedEntityCatalog();
  $("#entityDetails").innerHTML = [
    ["模型类", model.id], ["骨骼", model.parts.length], ["立方体", cubes],
    ["贴图画布", `${model.textureWidth} × ${model.textureHeight}`], ["材质", labelForKey(record.key)],
    ["显示版本", edition === "classic" ? "经典" : "现有"],
    ["渲染器映射", catalog?.renderers.join(", ") || "模型名匹配"],
    ["实际文件", source?.relative || "—"]
  ].map(([term, value]) => `<dt>${term}</dt><dd>${escapeHtml(String(value))}</dd>`).join("");
}

async function updateEntityMaterial() {
  await rebuildEntity();
}

function selectedEntityCatalog() {
  return state.index.entities.find(entity => entity.modelId === $("#modelSelect").value);
}

function populateEntityTextureOptions(preferredKey = "") {
  const entity = selectedEntityCatalog();
  if (!entity) return;
  let records = entity.textures.map(key => state.index.textures.find(item => item.key === key)).filter(Boolean);
  if (entity.modelId === "GummyBunnyModel") {
    records = records.filter(record => record.key === "textures/entity/bunny.png");
  }
  $("#entityTextureSelect").innerHTML = records.map(record => {
    const editions = record.classic && record.current ? "经典 + 现有" : record.current ? "仅现有" : "仅经典";
    return `<option value="${escapeHtml(record.key)}">${escapeHtml(labelForKey(record.key))} · ${editions}</option>`;
  }).join("");
  if (preferredKey && records.some(record => record.key === preferredKey)) $("#entityTextureSelect").value = preferredKey;
  let layers = entity.layers.map(key => state.index.textures.find(item => item.key === key)).filter(Boolean);
  if (entity.modelId === "GummyBunnyModel") {
    layers = layers.filter(record => record.key !== "textures/entity/whitebunny.png");
  }
  $("#glowTextureSelect").innerHTML = `<option value="">关闭</option>` + layers.map(record =>
    `<option value="${escapeHtml(record.key)}">${escapeHtml(labelForKey(record.key))}</option>`
  ).join("");
  $("#glowToggle").checked = false;
}

function populateEntities() {
  $("#modelCount").textContent = state.index.entities.length;
  $("#modelSelect").innerHTML = state.index.entities.map(entity =>
    `<option value="${entity.modelId}">${escapeHtml(entity.name)}</option>`
  ).join("");
  const preferred = state.index.entities.find(entity => entity.modelId === "SuguardModel") || state.index.entities[0];
  if (preferred) {
    $("#modelSelect").value = preferred.modelId;
    populateEntityTextureOptions(preferred.textures[0]);
    populateEntityAnimations();
  }
}

function animateEntity(now = performance.now()) {
  requestAnimationFrame(animateEntity);
  if (!renderer) return;
  const animation = currentEntityAnimation();
  const elapsed = Math.min(.1, Math.max(0, (now - state.entityAnimationLastAt) / 1000));
  state.entityAnimationLastAt = now;
  if (animation && state.entityAnimationPlaying) {
    state.entityAnimationTime += elapsed * state.entityAnimationSpeed;
    if (state.entityAnimationTime >= animation.duration) {
      if (animation.loop) state.entityAnimationTime %= animation.duration;
      else { state.entityAnimationTime = animation.duration; state.entityAnimationPlaying = false; }
    }
    updateEntityAnimationControls();
  }
  applyCurrentEntityAnimation();
  const speed = Number($("#turntableSpeed")?.value || 0);
  if ($("#turntableToggle")?.checked && modelRoot3d) {
    state.turntableAngle += speed * .006;
    [modelRoot3d, requiredLayerRoot3d, glowRoot3d].filter(Boolean).forEach(root => root.rotation.y += state.turntableAngle);
  }
  controls.update();
  renderer.render(scene, camera);
}

function setCameraView(view) {
  if (!modelRoot3d) return;
  const box = new THREE.Box3().setFromObject(modelRoot3d);
  const size = box.getSize(new THREE.Vector3());
  const distance = Math.max(3, Math.max(size.x, size.y, size.z) * 2.2);
  const target = controls.target.clone();
  const positions = {
    front: [0, target.y, -distance], side: [distance, target.y, 0],
    back: [0, target.y, distance], perspective: [distance * .7, target.y + distance * .32, -distance]
  };
  camera.position.set(...positions[view]); controls.update();
}

function bindEvents() {
  $("#textureList").addEventListener("click", event => {
    const row = event.target.closest(".asset-row");
    if (row) selectTexture(row.dataset.key);
  });
  $("#searchInput").addEventListener("input", debounce(filterTextures));
  $("#categoryFilter").addEventListener("change", filterTextures);
  $("#statusFilter").addEventListener("change", filterTextures);
  $$(".tab").forEach(tab => tab.addEventListener("click", async () => {
    $$(".tab").forEach(item => item.classList.toggle("active", item === tab));
    const entity = tab.dataset.workspace === "entities";
    $("#textureWorkspace").classList.toggle("hidden", entity);
    $("#entityWorkspace").classList.toggle("hidden", !entity);
    if (entity) {
      if (!renderer) initEntityStage();
      resizeEntityStage();
      if (!modelRoot3d) await rebuildEntity();
    }
  }));
  $$("#viewMode button").forEach(button => button.addEventListener("click", () => setViewMode(button.dataset.mode)));
  $("#splitRange").addEventListener("input", event => { state.split = Number(event.target.value); drawCompare(); });
  $("#playPause").addEventListener("click", () => { state.playing = !state.playing; state.lastFrameAt = performance.now(); updateAnimationControls(); });
  $("#prevFrame").addEventListener("click", () => { state.playing = false; updateAnimationControls(); stepFrame(-1); });
  $("#nextFrame").addEventListener("click", () => { state.playing = false; updateAnimationControls(); stepFrame(1); });
  $("#frameRange").addEventListener("input", event => { state.playing = false; state.frame = Number(event.target.value); updateAnimationControls(); drawTexturePreviews(); });
  $("#speedSelect").addEventListener("change", event => { state.speed = Number(event.target.value); });
  $("#checkerBtn").addEventListener("click", event => {
    $("#textureStage").classList.toggle("checker");
    event.currentTarget.classList.toggle("active");
  });
  $("#backgroundColor").addEventListener("input", event => $("#textureStage").style.backgroundColor = event.target.value);
  $("#actualBtn").addEventListener("click", event => { state.actualPixels = !state.actualPixels; event.currentTarget.classList.toggle("active"); drawTexturePreviews(); });
  $("#fitBtn").addEventListener("click", () => { state.actualPixels = false; $("#actualBtn").classList.remove("active"); drawTexturePreviews(); });
  $("#refreshBtn").addEventListener("click", () => loadIndex(true).then(() => { if (renderer) rebuildEntity(); }));
  $("#recordBtn").addEventListener("click", toggleRecordMode);
  $("#fullscreenBtn").addEventListener("click", () => document.fullscreenElement ? document.exitFullscreen() : document.documentElement.requestFullscreen());
  document.addEventListener("keydown", event => {
    if (event.key === "Escape" && document.body.classList.contains("record-mode")) toggleRecordMode();
    if (event.code === "Space" && !/INPUT|SELECT|TEXTAREA/.test(document.activeElement.tagName)) {
      event.preventDefault(); state.playing = !state.playing; updateAnimationControls();
    }
  });
  $("#modelSelect").addEventListener("change", () => {
    populateEntityTextureOptions();
    populateEntityAnimations();
    rebuildEntity();
  });
  $("#entityAnimationList").addEventListener("click", event => {
    const button = event.target.closest("button[data-animation]");
    if (button) selectEntityAnimation(button.dataset.animation);
  });
  $("#entityAnimationPlay").addEventListener("click", () => {
    state.entityAnimationPlaying = !state.entityAnimationPlaying;
    state.entityAnimationLastAt = performance.now();
    updateEntityAnimationControls();
  });
  $("#entityAnimationRestart").addEventListener("click", () => {
    state.entityAnimationTime = 0;
    state.entityAnimationPlaying = true;
    state.entityAnimationLastAt = performance.now();
    updateEntityAnimationControls();
    applyCurrentEntityAnimation();
  });
  $("#entityAnimationRange").addEventListener("input", event => {
    state.entityAnimationTime = Number(event.target.value);
    state.entityAnimationPlaying = false;
    updateEntityAnimationControls();
    applyCurrentEntityAnimation();
  });
  $("#entityAnimationSpeed").addEventListener("change", event => {
    state.entityAnimationSpeed = Number(event.target.value);
  });
  $("#entityTextureSelect").addEventListener("change", rebuildEntity);
  $$("#entityEdition button").forEach(button => button.addEventListener("click", () => {
    state.entityEdition = button.dataset.edition;
    $$("#entityEdition button").forEach(item => item.classList.toggle("active", item === button));
    updateEntityMaterial();
  }));
  $("#glowToggle").addEventListener("change", rebuildEntity);
  $("#glowTextureSelect").addEventListener("change", () => {
    $("#glowToggle").checked = Boolean($("#glowTextureSelect").value);
    rebuildEntity();
  });
  $$(".view-buttons button").forEach(button => button.addEventListener("click", () => setCameraView(button.dataset.view)));
  $("#resetCamera").addEventListener("click", rebuildEntity);
  $$("#stageSwatches button").forEach(button => button.addEventListener("click", () => {
    scene.background = new THREE.Color(button.dataset.color);
    $$("#stageSwatches button").forEach(item => item.classList.toggle("active", item === button));
  }));
  $("#gridToggle").addEventListener("change", event => grid.visible = event.target.checked);
  $("#shadowToggle").addEventListener("change", event => floor.visible = event.target.checked);
}

function toggleRecordMode() {
  document.body.classList.toggle("record-mode");
  setTimeout(() => { drawTexturePreviews(); resizeEntityStage(); }, 80);
}

async function boot() {
  bindEvents();
  await loadIndex();
  requestAnimationFrame(animationLoop);
}

boot().catch(error => {
  $("#loading").innerHTML = `<strong>启动失败</strong><span>${escapeHtml(error.message)}</span>`;
  console.error(error);
});
