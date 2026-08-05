import * as THREE from "three";
import { OrbitControls } from "./vendor/OrbitControls.js";
import { TransformControls } from "./vendor/TransformControls.js";

const $ = id => document.getElementById(id);
const canvas = $("sceneCanvas");
const viewport = $("viewport");
const entitySelect = $("entitySelect");
const itemSelect = $("itemSelect");
const transformInputs = ["posX", "posY", "posZ", "rotX", "rotY", "rotZ", "scaleX", "scaleY", "scaleZ"].map($);

const entityLabels = {
  suguard: "糖卫",
  boss_suguard: "糖卫图腾",
  gingerbread: "姜饼人"
};
const itemLabels = {
  caramel_bow: "焦糖弓",
  licorice_spear: "甘草长矛",
  jump_wand: "跳跃法杖",
  honey_sword: "蜂蜜剑",
  honey_pickaxe: "蜂蜜镐",
  honey_hoe: "蜂蜜锄",
  candy_cane: "拐杖糖",
  dynamite: "炸药",
  glue_dynamite: "黏胶炸药"
};
const transformGroupByItem = {
  caramel_bow: "bow",
  licorice_spear: "tool",
  jump_wand: "tool",
  honey_sword: "tool",
  honey_pickaxe: "tool",
  honey_hoe: "tool",
  candy_cane: "regular",
  dynamite: "regular",
  glue_dynamite: "regular"
};
const transformGroupLabels = { bow: "弓箭", tool: "工具 / 武器", regular: "普通物品" };
const transformGroupDefaults = {
  suguard: {
    bow: { position: [-0.0385, 0.1329, 0.1183], rotation: [21.6, 12.16, 0.44], scale: [0.625, 0.625, 0.625] },
    tool: { position: [-0.0038, 0.0331, 0.1978], rotation: [-105.86, 0, 180], scale: [0.825, 0.825, 0.825] },
    regular: { position: [-0.0822, 0.1672, -0.0052], rotation: [-86.33, 5.08, 177.81], scale: [0.825, 0.825, 0.825] }
  },
  gingerbread: {
    bow: { position: [0.0333, 0.0221, -0.0395], rotation: [-85.84, -9.16, 179.46], scale: [0.625, 0.625, 0.625] },
    tool: { position: [0, -0.3468, 0.204], rotation: [10.86, 0, 0], scale: [1, 1, 1] },
    regular: { position: [-0.2101, -0.0478, 0.1159], rotation: [-85.11, -1.98, -88.03], scale: [0.825, 0.825, 0.825] }
  }
};
const storeKey = "candycraft-held-item-transform-v6";
const itemModelTransforms = {
  caramel_bow: { position: [0.75 / 16, 0, 0.25 / 16], rotation: [5, 80, -45], scale: 1 },
  licorice_spear: { position: [0, 4 / 16, 0.5 / 16], rotation: [0, -90, 55], scale: 0.85 },
  jump_wand: { position: [0, 4 / 16, 0.5 / 16], rotation: [0, -90, 55], scale: 0.85 },
  honey_sword: { position: [0, 4 / 16, 0.5 / 16], rotation: [0, -90, 55], scale: 0.85 },
  honey_pickaxe: { position: [0, 4 / 16, 0.5 / 16], rotation: [0, -90, 55], scale: 0.85 },
  honey_hoe: { position: [0, 4 / 16, 0.5 / 16], rotation: [0, -90, 55], scale: 0.85 },
  candy_cane: { position: [0, 3 / 16, 1 / 16], rotation: [0, 0, 0], scale: 0.55 },
  dynamite: { position: [0, 3 / 16, 1 / 16], rotation: [0, 0, 0], scale: 0.55 },
  glue_dynamite: { position: [0, 3 / 16, 1 / 16], rotation: [0, 0, 0], scale: 0.55 }
};

const renderer = new THREE.WebGLRenderer({ canvas, antialias: true, alpha: false, powerPreference: "high-performance" });
renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
renderer.setClearColor(0x1e2024, 1);
renderer.outputColorSpace = THREE.SRGBColorSpace;

const scene = new THREE.Scene();
scene.fog = new THREE.Fog(0x1e2024, 6, 11);
const camera = new THREE.OrthographicCamera(-2, 2, 2, -2, 0.01, 100);
camera.position.set(3.3, 2.35, 4.6);
camera.lookAt(0, 0.85, 0);

const orbit = new OrbitControls(camera, canvas);
orbit.target.set(0, 0.82, 0);
orbit.enableDamping = true;
orbit.dampingFactor = 0.09;
orbit.minZoom = 0.55;
orbit.maxZoom = 4.5;
orbit.mouseButtons.LEFT = null;
orbit.mouseButtons.RIGHT = THREE.MOUSE.ROTATE;
orbit.mouseButtons.MIDDLE = THREE.MOUSE.DOLLY;

scene.add(new THREE.HemisphereLight(0xeef2ff, 0x5a3a38, 2.15));
const keyLight = new THREE.DirectionalLight(0xffffff, 2.2);
keyLight.position.set(3, 5, 4);
scene.add(keyLight);
const rimLight = new THREE.DirectionalLight(0xff9aba, 1.25);
rimLight.position.set(-4, 2, -3);
scene.add(rimLight);

const grid = new THREE.GridHelper(12, 48, 0x6c7078, 0x35383e);
grid.position.y = -0.005;
scene.add(grid);

const transformControl = new TransformControls(camera, canvas);
transformControl.setSize(0.72);
transformControl.setSpace("local");
scene.add(transformControl);
transformControl.addEventListener("dragging-changed", event => { orbit.enabled = !event.value; });
transformControl.addEventListener("objectChange", () => {
  readObjectIntoInputs();
  updateOutput();
  saveCurrent();
});

const textureLoader = new THREE.TextureLoader();
const textureCache = new Map();
let entityRoot = null;
let handAnchor = null;
let weaponTransform = null;
let weaponVisual = null;
let armAxes = null;
let entityMaterials = [];
let pointerStart = null;
let toastTimer = null;
let currentMode = "translate";
const raycaster = new THREE.Raycaster();
const pointer = new THREE.Vector2();

function radians(value) { return value * Math.PI / 180; }
function degrees(value) { return value * 180 / Math.PI; }
function entityFamily() { return entitySelect.value === "gingerbread" ? "gingerbread" : "suguard"; }
function minecraftPositionToThree(position) { return [position[0], -position[1], position[2]]; }
function minecraftRotationToThree(rotation) { return [-rotation[0], rotation[1], -rotation[2]]; }
function threePositionToMinecraft(position) { return [position.x, -position.y, position.z]; }
function threeRotationToMinecraft(rotation) { return [-degrees(rotation.x), degrees(rotation.y), -degrees(rotation.z)]; }
function positionToPreview(position) { return entityFamily() === "suguard" ? position : minecraftPositionToThree(position); }
function rotationToPreview(rotation) { return entityFamily() === "suguard" ? rotation : minecraftRotationToThree(rotation); }
function positionFromPreview(position) {
  return entityFamily() === "suguard" ? [position.x, position.y, position.z] : threePositionToMinecraft(position);
}
function rotationFromPreview(rotation) {
  return entityFamily() === "suguard"
    ? [degrees(rotation.x), degrees(rotation.y), degrees(rotation.z)]
    : threeRotationToMinecraft(rotation);
}
function round(value, digits = 4) {
  const scale = 10 ** digits;
  return Math.round((Number(value) + Number.EPSILON) * scale) / scale;
}
function format(value) {
  const result = round(value, 4);
  return Number.isInteger(result) ? `${result}.0F` : `${result}F`;
}

async function loadTexture(name) {
  if (!textureCache.has(name)) {
    textureCache.set(name, textureLoader.loadAsync(`./assets/${name}`).then(texture => {
      texture.colorSpace = THREE.SRGBColorSpace;
      texture.magFilter = THREE.NearestFilter;
      texture.minFilter = THREE.NearestFilter;
      texture.generateMipmaps = false;
      return texture;
    }));
  }
  return textureCache.get(name);
}

function atlasMaterial(texture, u, v, width, height) {
  const map = texture.clone();
  map.needsUpdate = true;
  map.magFilter = THREE.NearestFilter;
  map.minFilter = THREE.NearestFilter;
  map.generateMipmaps = false;
  const atlasWidth = texture.image.width;
  const atlasHeight = texture.image.height;
  map.repeat.set(width / atlasWidth, height / atlasHeight);
  map.offset.set(u / atlasWidth, 1 - (v + height) / atlasHeight);
  const material = new THREE.MeshLambertMaterial({ map, transparent: true, alphaTest: 0.08, side: THREE.FrontSide });
  entityMaterials.push(material);
  return material;
}

function minecraftMaterials(texture, u, v, width, height, depth, correctNorthSouth = false) {
  const materials = [
    atlasMaterial(texture, u + depth + width, v + depth, depth, height),
    atlasMaterial(texture, u, v + depth, depth, height),
    atlasMaterial(texture, u + depth, v, width, depth),
    atlasMaterial(texture, u + depth + width, v, width, depth),
    atlasMaterial(texture, u + depth, v + depth, width, height),
    atlasMaterial(texture, u + depth + width + depth, v + depth, width, height)
  ];
  if (correctNorthSouth) {
    [materials[4], materials[5]] = [materials[5], materials[4]];
  }
  return materials;
}

function createPart(parent, x, y, z, xRot = 0, yRot = 0, zRot = 0, groundY = 24) {
  const part = new THREE.Group();
  part.position.set(x / 16, (groundY - y) / 16, z / 16);
  part.rotation.set(-xRot, yRot, -zRot);
  parent.add(part);
  return part;
}

function addCuboid(part, texture, offset, size, uv, correctNorthSouth = false) {
  const [x, y, z] = offset;
  const [width, height, depth] = size;
  const geometry = new THREE.BoxGeometry(width / 16, height / 16, depth / 16);
  const mesh = new THREE.Mesh(geometry, minecraftMaterials(texture, uv[0], uv[1], width, height, depth, correctNorthSouth));
  mesh.position.set((x + width / 2) / 16, -(y + height / 2) / 16, (z + depth / 2) / 16);
  part.add(mesh);
  return mesh;
}

function createMinecraftPart(parent, x, y, z, xRot = 0, yRot = 0, zRot = 0) {
  const part = new THREE.Group();
  part.position.set(x / 16, y / 16, z / 16);
  part.rotation.order = "ZYX";
  part.rotation.set(xRot, yRot, zRot);
  parent.add(part);
  return part;
}

function addMinecraftCuboid(part, texture, offset, size, uv) {
  const [x, y, z] = offset;
  const [width, height, depth] = size;
  const geometry = new THREE.BoxGeometry(width / 16, height / 16, depth / 16);
  const mesh = new THREE.Mesh(geometry, minecraftMaterials(texture, uv[0], uv[1], width, height, depth));
  mesh.position.set((x + width / 2) / 16, (y + height / 2) / 16, (z + depth / 2) / 16);
  part.add(mesh);
  return mesh;
}

async function buildSuguard(boss) {
  const texture = await loadTexture(boss ? "sugardeboss.png" : "sugarde.png");
  const root = new THREE.Group();
  root.rotation.y = Math.PI;
  const scaledRoot = new THREE.Group();
  const size = boss ? 2 : 1;
  scaledRoot.scale.set(-size, -size, size);
  root.add(scaledRoot);
  const modelRoot = new THREE.Group();
  modelRoot.position.set(0, -1.501, 0);
  scaledRoot.add(modelRoot);

  const leg1 = createMinecraftPart(modelRoot, 1, 20, -1);
  addMinecraftCuboid(leg1, texture, [0, 0, 0], [2, 4, 2], [0, 16]);
  const leg2 = createMinecraftPart(modelRoot, -3, 20, -1);
  addMinecraftCuboid(leg2, texture, [0, 0, 0], [2, 4, 2], [0, 16]);
  const body = createMinecraftPart(modelRoot, -3, 14, -2);
  addMinecraftCuboid(body, texture, [0, 0, 0], [6, 6, 4], [0, 6]);
  const head = createMinecraftPart(modelRoot, 0, 11, 0);
  addMinecraftCuboid(head, texture, [-1.5, 0, -1.5], [3, 3, 3], [0, 0]);
  const nose = createMinecraftPart(modelRoot, 0, 12, 0);
  addMinecraftCuboid(nose, texture, [-0.5, 0, -2], [1, 1, 1], [0, 22]);
  const brim = createMinecraftPart(modelRoot, 0, 11, 0);
  addMinecraftCuboid(brim, texture, [-2, 0, -2], [4, 1, 4], [12, 0]);
  const earRight = createMinecraftPart(modelRoot, 0, 12, 0);
  addMinecraftCuboid(earRight, texture, [1, 0, -0.5], [1, 2, 1], [4, 22]);
  const earLeft = createMinecraftPart(modelRoot, 0, 10, 0);
  addMinecraftCuboid(earLeft, texture, [-2, 2, -0.5], [1, 2, 1], [4, 22]);
  const leftArm = createMinecraftPart(modelRoot, 3, 15, 0, -1.570796);
  addMinecraftCuboid(leftArm, texture, [0, 0, 0], [1, 4, 2], [20, 6]);
  const rightArm = createMinecraftPart(modelRoot, -4, 15, 0, -1.050296);
  addMinecraftCuboid(rightArm, texture, [0, 0, 0], [1, 5, 2], [20, 6]);
  const shield = createMinecraftPart(modelRoot, 1, 13.5, -5);
  addMinecraftCuboid(shield, texture, [0, 0, 0], [5, 5, 1], [8, 16]);
  const hat = createMinecraftPart(modelRoot, 0, 10, 0);
  addMinecraftCuboid(hat, texture, [-1.5, 0, -1.5], [3, 1, 3], [28, 0]);

  const anchor = new THREE.Group();
  anchor.position.set(0.0275, 0.1225, 0.1425);
  anchor.rotation.y = Math.PI;
  rightArm.add(anchor);
  return { root, anchor, rightArm };
}

async function buildGingerbread(index) {
  const texture = await loadTexture(`gingerbread${index}.png`);
  const root = new THREE.Group();
  const head = createPart(root, 0, 0, 0);
  addCuboid(head, texture, [-4, -8, -4], [8, 8, 8], [0, 0]);
  const hat = createPart(root, 0, 0, 0);
  addCuboid(hat, texture, [-4.25, -8.25, -4.25], [8.5, 8.5, 8.5], [32, 0]);
  const body = createPart(root, 0, 0, 0);
  addCuboid(body, texture, [-4, 0, -2], [8, 12, 4], [16, 16]);
  const rightArm = createPart(root, -5, 2, 0);
  addCuboid(rightArm, texture, [-3, -2, -2], [4, 12, 4], [40, 16]);
  const leftArm = createPart(root, 5, 2, 0);
  addCuboid(leftArm, texture, [-1, -2, -2], [4, 12, 4], [40, 16]);
  const rightLeg = createPart(root, -1.9, 12, 0);
  addCuboid(rightLeg, texture, [-2, 0, -2], [4, 12, 4], [0, 16]);
  const leftLeg = createPart(root, 1.9, 12, 0);
  addCuboid(leftLeg, texture, [-2, 0, -2], [4, 12, 4], [0, 16]);
  root.scale.setScalar(0.5);

  const anchor = new THREE.Group();
  anchor.position.set(-0.06, -0.62, 0.05);
  rightArm.add(anchor);
  return { root, anchor, rightArm };
}

function disposeObject(object) {
  object?.traverse(child => {
    child.geometry?.dispose();
    const materials = Array.isArray(child.material) ? child.material : child.material ? [child.material] : [];
    materials.forEach(material => { material.map?.dispose(); material.dispose(); });
  });
  object?.removeFromParent();
}

async function rebuildEntity() {
  transformControl.detach();
  disposeObject(entityRoot);
  entityMaterials = [];
  const id = entitySelect.value;
  const built = id === "suguard" || id === "boss_suguard"
    ? await buildSuguard(id === "boss_suguard")
    : await buildGingerbread(2);
  entityRoot = built.root;
  handAnchor = built.anchor;
  scene.add(entityRoot);
  armAxes = new THREE.AxesHelper(0.42);
  armAxes.visible = $("showBones").checked;
  built.rightArm.add(armAxes);
  await rebuildWeapon();
  updateEntityOpacity();
  $("entityName").textContent = entityLabels[id];
  $("handAnchorValue").textContent = entityFamily() === "gingerbread"
    ? "-0.06 / 0.62 / 0.05"
    : "0.0275 / 0.1225 / 0.1425";
}

async function rebuildWeapon() {
  if (!handAnchor) return;
  transformControl.detach();
  disposeObject(weaponTransform);
  const item = itemSelect.value;
  const texture = await loadTexture(`${item}.png`);
  weaponTransform = new THREE.Group();
  weaponTransform.name = "weapon_transform";
  const modelTransform = new THREE.Group();
  const model = itemModelTransforms[item];
  modelTransform.position.set(...positionToPreview(model.position));
  modelTransform.rotation.set(...rotationToPreview(model.rotation).map(radians));
  modelTransform.scale.setScalar(model.scale);
  weaponTransform.add(modelTransform);

  weaponVisual = createExtrudedItem(texture.image);
  weaponVisual.name = item;
  modelTransform.add(weaponVisual);
  handAnchor.add(weaponTransform);
  applySavedOrDefault();
  selectWeapon();
  $("weaponName").textContent = itemLabels[item];
  $("selectionLabel").textContent = `已选择：${itemLabels[item]}`;
  $("transformGroupName").textContent = transformGroupLabels[currentTransformGroup()];
}

function createExtrudedItem(image) {
  const sampleCanvas = document.createElement("canvas");
  sampleCanvas.width = image.naturalWidth || image.width;
  sampleCanvas.height = image.naturalHeight || image.height;
  const context = sampleCanvas.getContext("2d", { willReadFrequently: true });
  context.imageSmoothingEnabled = false;
  context.drawImage(image, 0, 0);
  const pixels = context.getImageData(0, 0, sampleCanvas.width, sampleCanvas.height).data;
  const visible = [];
  for (let y = 0; y < sampleCanvas.height; y++) {
    for (let x = 0; x < sampleCanvas.width; x++) {
      const offset = (y * sampleCanvas.width + x) * 4;
      if (pixels[offset + 3] > 24) visible.push({ x, y, offset });
    }
  }

  const maxDimension = Math.max(sampleCanvas.width, sampleCanvas.height);
  const pixelSize = 1 / maxDimension;
  const thickness = 1 / 16;
  const geometry = new THREE.BoxGeometry(pixelSize, pixelSize, thickness);
  const material = new THREE.MeshStandardMaterial({ vertexColors: true, roughness: 0.82, metalness: 0.02 });
  const mesh = new THREE.InstancedMesh(geometry, material, Math.max(1, visible.length));
  const matrix = new THREE.Matrix4();
  const color = new THREE.Color();
  visible.forEach((pixel, index) => {
    const x = (pixel.x + 0.5 - sampleCanvas.width / 2) / maxDimension;
    const y = (sampleCanvas.height / 2 - pixel.y - 0.5) / maxDimension;
    matrix.makeTranslation(x, y, 0);
    mesh.setMatrixAt(index, matrix);
    color.setRGB(pixels[pixel.offset] / 255, pixels[pixel.offset + 1] / 255, pixels[pixel.offset + 2] / 255, THREE.SRGBColorSpace);
    mesh.setColorAt(index, color);
  });
  mesh.instanceMatrix.needsUpdate = true;
  if (mesh.instanceColor) mesh.instanceColor.needsUpdate = true;
  mesh.count = visible.length;
  mesh.renderOrder = 3;
  return mesh;
}

function readStore() {
  try { return JSON.parse(localStorage.getItem(storeKey) || "{}"); }
  catch { return {}; }
}
function currentTransformGroup() { return transformGroupByItem[itemSelect.value]; }
function currentTransformKey() { return `${entityFamily()}:${currentTransformGroup()}`; }
function currentDefault() { return transformGroupDefaults[entityFamily()][currentTransformGroup()]; }
function currentData() {
  return {
    position: [Number($("posX").value), Number($("posY").value), Number($("posZ").value)],
    rotation: [Number($("rotX").value), Number($("rotY").value), Number($("rotZ").value)],
    scale: [Number($("scaleX").value), Number($("scaleY").value), Number($("scaleZ").value)]
  };
}
function applySavedOrDefault() {
  const data = readStore()[currentTransformKey()] || currentDefault();
  writeDataToInputs(data);
  applyInputsToObject(false);
}
function saveCurrent() {
  if (!weaponTransform) return;
  const store = readStore();
  store[currentTransformKey()] = currentData();
  localStorage.setItem(storeKey, JSON.stringify(store));
}
function writeDataToInputs(data) {
  [$("posX").value, $("posY").value, $("posZ").value] = data.position.map(value => round(value, 4));
  [$("rotX").value, $("rotY").value, $("rotZ").value] = data.rotation.map(value => round(value, 2));
  [$("scaleX").value, $("scaleY").value, $("scaleZ").value] = data.scale.map(value => round(value, 4));
}
function applyInputsToObject(save = true) {
  if (!weaponTransform) return;
  const data = currentData();
  weaponTransform.position.set(...positionToPreview(data.position));
  weaponTransform.rotation.set(...rotationToPreview(data.rotation).map(radians));
  weaponTransform.scale.set(...data.scale);
  updateOutput();
  if (save) saveCurrent();
}
function readObjectIntoInputs() {
  if (!weaponTransform) return;
  const position = positionFromPreview(weaponTransform.position);
  const rotation = rotationFromPreview(weaponTransform.rotation);
  $("posX").value = round(position[0], 4);
  $("posY").value = round(position[1], 4);
  $("posZ").value = round(position[2], 4);
  $("rotX").value = round(rotation[0], 2);
  $("rotY").value = round(rotation[1], 2);
  $("rotZ").value = round(rotation[2], 2);
  $("scaleX").value = round(weaponTransform.scale.x, 4);
  $("scaleY").value = round(weaponTransform.scale.y, 4);
  $("scaleZ").value = round(weaponTransform.scale.z, 4);
}

function updateOutput() {
  const data = currentData();
  const rotations = ["X", "Y", "Z"].map((axis, index) => Math.abs(data.rotation[index]) > 0.0001
    ? `poseStack.mulPose(Axis.${axis}P.rotationDegrees(${format(data.rotation[index])}));`
    : null).filter(Boolean);
  $("codeOutput").textContent = [
    `poseStack.translate(${format(data.position[0])}, ${format(data.position[1])}, ${format(data.position[2])});`,
    ...rotations,
    `poseStack.scale(${format(data.scale[0])}, ${format(data.scale[1])}, ${format(data.scale[2])});`
  ].join("\n");
}

function selectWeapon() {
  if (!weaponTransform) return;
  transformControl.attach(weaponTransform);
  transformControl.enabled = true;
  transformControl.visible = true;
  $("weaponRow").classList.add("selected");
  $("entityRow").classList.remove("selected");
  $("viewportHint").textContent = `拖动彩色轴调整${itemLabels[itemSelect.value]}`;
}
function selectEntity() {
  transformControl.detach();
  $("entityRow").classList.add("selected");
  $("weaponRow").classList.remove("selected");
  $("selectionLabel").textContent = `已选择：${entityLabels[entitySelect.value]}`;
  $("viewportHint").textContent = "点击武器以编辑手持位置";
}
function updateEntityOpacity() {
  const transparent = $("transparentEntity").checked;
  entityMaterials.forEach(material => {
    material.transparent = true;
    material.opacity = transparent ? 0.34 : 1;
    material.depthWrite = !transparent;
    material.needsUpdate = true;
  });
}

function setMode(mode) {
  currentMode = mode;
  transformControl.enabled = true;
  transformControl.visible = Boolean(transformControl.object);
  transformControl.setMode(mode);
  $("viewportHint").textContent = `拖动彩色轴调整${itemLabels[itemSelect.value]}，拖动空白处旋转视角`;
  document.querySelectorAll("[data-mode]").forEach(button => button.classList.toggle("active", button.dataset.mode === mode));
}
function setCamera(position, target = new THREE.Vector3(0, 0.82, 0)) {
  camera.position.copy(position);
  orbit.target.copy(target);
  camera.zoom = 1;
  camera.updateProjectionMatrix();
  orbit.update();
}
function resize() {
  const width = Math.max(1, viewport.clientWidth);
  const height = Math.max(1, viewport.clientHeight);
  renderer.setSize(width, height, false);
  const aspect = width / height;
  const size = 3.1;
  camera.left = -size * aspect / 2;
  camera.right = size * aspect / 2;
  camera.top = size / 2;
  camera.bottom = -size / 2;
  camera.updateProjectionMatrix();
}
function showToast(message) {
  clearTimeout(toastTimer);
  $("toast").textContent = message;
  $("toast").classList.add("show");
  toastTimer = setTimeout(() => $("toast").classList.remove("show"), 1600);
}

document.querySelectorAll("[data-mode]").forEach(button => button.addEventListener("click", () => setMode(button.dataset.mode)));
$("frontView").addEventListener("click", () => setCamera(new THREE.Vector3(0, 0.82, 5)));
$("sideView").addEventListener("click", () => setCamera(new THREE.Vector3(5, 0.82, 0)));
$("resetCamera").addEventListener("click", () => setCamera(new THREE.Vector3(3.3, 2.35, 4.6)));
$("resetTransform").addEventListener("click", () => {
  const group = currentTransformGroup();
  writeDataToInputs(currentDefault());
  applyInputsToObject();
  showToast(`已恢复${transformGroupLabels[group]}的默认变换`);
});
$("entityRow").addEventListener("click", selectEntity);
$("weaponRow").addEventListener("click", selectWeapon);
$("showGrid").addEventListener("change", event => { grid.visible = event.target.checked; });
$("showBones").addEventListener("change", event => { if (armAxes) armAxes.visible = event.target.checked; });
$("transparentEntity").addEventListener("change", updateEntityOpacity);
entitySelect.addEventListener("change", rebuildEntity);
itemSelect.addEventListener("change", rebuildWeapon);

transformInputs.forEach((input, index) => input.addEventListener("input", () => {
  if ($("uniformScale").checked && index >= 6) {
    const value = input.value;
    [$("scaleX"), $("scaleY"), $("scaleZ")].forEach(field => { field.value = value; });
  }
  applyInputsToObject();
}));

$("copyCode").addEventListener("click", async () => {
  await navigator.clipboard.writeText($("codeOutput").textContent);
  showToast("PoseStack 代码已复制");
});
$("exportJson").addEventListener("click", () => {
  const family = entityFamily();
  const store = readStore();
  const transforms = Object.fromEntries(Object.keys(transformGroupLabels).map(group => [group, {
    label: transformGroupLabels[group],
    ...(store[`${family}:${group}`] || transformGroupDefaults[family][group])
  }]));
  const payload = {
    version: 2,
    entityFamily: family,
    previewEntity: entitySelect.value,
    handAnchor: family === "gingerbread" ? [-0.06, 0.62, 0.05] : [0.0275, 0.1225, 0.1425],
    displayContext: "THIRD_PERSON_RIGHT_HAND",
    transforms
  };
  const blob = new Blob([JSON.stringify(payload, null, 2)], { type: "application/json" });
  const link = document.createElement("a");
  link.href = URL.createObjectURL(blob);
  link.download = `${entitySelect.value}-held-item-transforms.json`;
  document.body.appendChild(link);
  link.click();
  link.remove();
  setTimeout(() => URL.revokeObjectURL(link.href), 1000);
  showToast("该实体的全部变换 JSON 已导出");
});

canvas.addEventListener("pointerdown", event => {
  pointerStart = { x: event.clientX, y: event.clientY };
  const rect = canvas.getBoundingClientRect();
  pointer.set(((event.clientX - rect.left) / rect.width) * 2 - 1, -((event.clientY - rect.top) / rect.height) * 2 + 1);
  raycaster.setFromCamera(pointer, camera);
  const overWeapon = weaponVisual && raycaster.intersectObject(weaponVisual, true).length > 0;
  orbit.mouseButtons.LEFT = !overWeapon && !transformControl.axis ? THREE.MOUSE.ROTATE : null;
}, true);
canvas.addEventListener("pointerup", event => {
  orbit.mouseButtons.LEFT = null;
  if (!pointerStart || Math.hypot(event.clientX - pointerStart.x, event.clientY - pointerStart.y) > 4 || !weaponVisual) return;
  const rect = canvas.getBoundingClientRect();
  pointer.set(((event.clientX - rect.left) / rect.width) * 2 - 1, -((event.clientY - rect.top) / rect.height) * 2 + 1);
  raycaster.setFromCamera(pointer, camera);
  const hits = raycaster.intersectObject(weaponVisual, true);
  if (hits.length) selectWeapon();
  else if (!transformControl.dragging) selectEntity();
});
window.addEventListener("keydown", event => {
  if (/INPUT|SELECT|TEXTAREA/.test(document.activeElement.tagName)) return;
  if (event.key.toLowerCase() === "g") setMode("translate");
  if (event.key.toLowerCase() === "r") setMode("rotate");
  if (event.key.toLowerCase() === "s") setMode("scale");
});
new ResizeObserver(resize).observe(viewport);

let frames = 0;
let fpsStart = performance.now();
function animate(now) {
  requestAnimationFrame(animate);
  orbit.update();
  renderer.render(scene, camera);
  frames++;
  if (now - fpsStart >= 1000) {
    $("fpsLabel").textContent = `${Math.round(frames * 1000 / (now - fpsStart))} FPS`;
    frames = 0;
    fpsStart = now;
  }
}

resize();
rebuildEntity().then(() => {
  $("renderDot").classList.add("ready");
  $("renderStatus").textContent = "3D 视口就绪";
}).catch(error => {
  console.error(error);
  $("renderStatus").textContent = "3D 视口加载失败";
  showToast(`加载失败：${error.message}`);
});
requestAnimationFrame(animate);
