const PI = Math.PI;
const sin = Math.sin;
const cos = Math.cos;

function clip(id, name, duration, apply, loop = true) {
  return { id, name, duration, apply, loop };
}

function headTurn(pose, names, time, amount = 0.24) {
  const yaw = sin(time * 0.9) * amount;
  const pitch = sin(time * 0.55 + 1.2) * amount * 0.3;
  names.forEach(name => pose.addRot(name, pitch, yaw, 0));
}

function quadrupedWalk(pose, time, names, amount = 1.0) {
  const swing = cos(time * 5.4) * amount;
  pose.setRot(names[0], swing, null, null);
  pose.setRot(names[1], -swing, null, null);
  pose.setRot(names[2], -swing, null, null);
  pose.setRot(names[3], swing, null, null);
}

function suguardHead(pose, x, y, z = 0) {
  ["head", "nose", "hat_brim", "hat_top", "ear_right", "ear_left"]
    .forEach(name => pose.addRot(name, x, y, z));
}

const suguard = [
  clip("dormant", "首领休眠", 4, pose => {
    pose.setRot("right_arm", -1.050296, 0, 0);
    pose.setRot("left_arm", -1.570796, 0, 0);
  }),
  clip("idle", "待机呼吸", 6, (pose, time) => {
    const breath = sin(time * 1.8);
    pose.offsetPos("body", 0, -0.055 * (breath + 1), 0);
    pose.addRot("body", breath * 0.012, 0, 0);
    pose.addRot("right_arm", 0, 0, -0.025 - breath * 0.018);
    pose.addRot("left_arm", 0, 0, 0.025 + breath * 0.018);
    headTurn(pose, ["head", "nose", "hat_brim", "hat_top", "ear_right", "ear_left"], time, 0.14);
  }),
  clip("lookout", "观察四周", 5, (pose, time) => {
    const look = sin(time * 0.52) * 0.28;
    suguardHead(pose, sin(time * 1.06) * 0.035, look);
    pose.addRot("body", 0, -look * 0.12, 0);
  }),
  clip("weapon_check", "检查武器", 4, (pose, time) => {
    const inspect = (1 - cos(time * PI / 2)) * 0.5;
    pose.setRot("right_arm", -1.050296 - inspect * 0.48, inspect * 0.22, -inspect * 0.12);
    pose.setRot("left_arm", -1.570796 + inspect * 0.16, 0, 0);
    suguardHead(pose, inspect * 0.12, -inspect * 0.18);
  }),
  clip("weight_shift", "重心摇摆", 5, (pose, time) => {
    const sway = sin(time * 1.25);
    pose.addRot("body", 0, sway * 0.07, sway * 0.035);
    pose.addRot("leg1", 0, 0, 0.025 + sway * 0.018);
    pose.addRot("leg2", 0, 0, -0.025 + sway * 0.018);
    suguardHead(pose, 0, -sway * 0.0385, -sway * 0.014);
  }),
  clip("dance", "武器舞蹈", 7.5, (pose, time) => {
    const phase = time / 7.5;
    const flourish = Math.sin(Math.min(1, phase * 3) * PI);
    const sweep = sin(time * 8) * flourish;
    const jump = phase > 0.48 && phase < 0.62 ? sin((phase - 0.48) / 0.14 * PI) : 0;
    pose.setRot("right_arm", -1.050296 - flourish * 0.42, sweep * 0.78, -flourish * 0.18 - sweep * 0.3);
    pose.setRot("left_arm", -1.570796 + flourish * 0.18, -sweep * 0.16, flourish * 0.12);
    pose.addRot("body", 0, -sweep * 0.11, sin(phase * PI * 4) * jump * 0.055);
    pose.root.rotation.y += jump * phase * PI * 2;
    pose.root.position.y += jump * 0.34;
  }, false),
  clip("walk", "行走", 1.2, (pose, time) => {
    const step = cos(time * 5.23);
    pose.setRot("leg1", step * 1.15, 0, -step * 0.035);
    pose.setRot("leg2", -step * 1.15, 0, step * 0.035);
    pose.setRot("right_arm", -1.050296 + step * 0.62, 0, step * 0.055);
    pose.setRot("left_arm", -1.570796 - step * 0.62, 0, step * 0.055);
    pose.addRot("body", 0, sin(time * 5.23) * 0.075, step * 0.025);
  }),
  clip("melee", "近战攻击", 1, (pose, time) => {
    const progress = Math.min(1, time);
    const strike = sin(progress * PI);
    const turn = sin(Math.sqrt(progress) * PI * 2) * 0.18;
    pose.addRot("body", 0, turn, 0);
    pose.setRot("right_arm", -1.050296 - strike * 1.43, turn * 2.1, -sin(progress * PI) * 0.34);
    pose.setRot("left_arm", -1.570796 + strike * 0.42, turn * 1.35, sin(progress * PI) * 0.16);
    suguardHead(pose, -sin(progress * PI) * 0.07, -turn * 0.7);
  }, false),
  clip("bow", "拉弓射击", 1.6, (pose, time) => {
    const draw = Math.min(1, time / 1.15);
    const tension = sin(draw * PI) * 0.025;
    pose.setRot("body", null, -0.08, null);
    pose.setRot("right_arm", -1.48 + tension, -0.18, -0.07);
    pose.setRot("left_arm", -1.42 - draw * 0.14 - tension, 0.28 + draw * 0.42, 0.08 + draw * 0.08);
    suguardHead(pose, -0.035, 0.06 + draw * 0.06);
  }, false),
  clip("hurt", "受伤转头", 0.8, (pose, time) => {
    suguardHead(pose, 0, Math.min(1, time / 0.8) * PI * 2);
  }, false)
];

export const ENTITY_ANIMATIONS = {
  BeeModel: [
    clip("fly", "飞行振翅", 1.4, (pose, time) => {
      const flap = sin(time * 18);
      pose.setRot("wing_right", null, null, -PI / 2 + flap);
      pose.setRot("wing_left", null, null, PI / 2 - flap);
    })
  ],
  BeetleModel: [
    clip("dormant", "首领休眠", 5, (pose, time) => {
      const wave = sin(time * PI * 0.4);
      ["belly", "body", "rim", "shell"].forEach(name => pose.offsetPos(name, 0, wave * 0.035, 0));
      pose.offsetPos("head", 0, wave * 0.012, 0);
    }),
    clip("idle", "待机", 3, (pose, time) => {
      const wave = sin(time * PI * 2 / 3);
      ["leg1", "leg3", "leg4"].forEach(name => pose.addRot(name, 0, 0, wave * 0.026));
      ["leg2", "leg5", "leg6"].forEach(name => pose.addRot(name, 0, 0, -wave * 0.026));
      pose.offsetPos("body", 0, wave * 0.08, 0);
      pose.offsetPos("belly", 0, wave * 0.044, 0);
    }),
    clip("walk", "行走", 1.5, (pose, time) => {
      const walk = cos(time * 5.2);
      ["leg1", "leg3", "leg4"].forEach(name => pose.setRot(name, null, null, walk));
      ["leg2", "leg5", "leg6"].forEach(name => pose.setRot(name, null, null, -walk));
    }),
    clip("single_shot", "单发射击", 1.25, (pose, time) => {
      const recoil = sin(Math.min(1, time / 1.25) * PI);
      pose.addRot("body", recoil * 0.12, 0, 0);
      pose.addRot("head", -recoil * 0.28, 0, 0);
      pose.offsetPos("body", 0, 0, recoil * 0.3);
    }, false),
    clip("volley_charge", "齐射蓄力", 2, (pose, time) => {
      const charge = Math.min(1, time / 2);
      pose.addRot("body", charge * 0.22, 0, 0);
      pose.addRot("head", -charge * 0.42, 0, 0);
      ["leg1", "leg3", "leg5"].forEach(name => pose.addRot(name, 0, 0, charge * 0.25));
      ["leg2", "leg4", "leg6"].forEach(name => pose.addRot(name, 0, 0, -charge * 0.25));
    }, false),
    clip("volley_fire", "连续齐射", 2.2, (pose, time) => {
      const recoil = Math.max(0, sin(time * 16));
      pose.addRot("body", recoil * 0.12, 0, 0);
      pose.addRot("head", -recoil * 0.24, 0, 0);
    }),
    clip("spin_fire", "旋转射击", 2.4, (pose, time) => {
      pose.root.rotation.y += time * PI * 2;
      const pulse = sin(time * 8) * 0.08;
      pose.addRot("body", pulse, 0, 0);
    }),
    clip("melee", "近战撞击", 1.4, (pose, time) => {
      const hit = sin(Math.min(1, time / 1.4) * PI);
      pose.addRot("body", -hit * 0.42, 0, 0);
      pose.addRot("head", hit * 0.25, 0, 0);
      pose.offsetPos("body", 0, -hit * 0.4, -hit * 0.7);
    }, false)
  ],
  CandyFishModel: [
    clip("swim", "游动", 2.2, (pose, time) => {
      const wave = cos(time * 7.1);
      pose.setRot("tail_top", null, wave, null);
      pose.setRot("tail_bottom", null, wave, null);
    })
  ],
  DragonModel: [
    clip("walk", "地面行走", 1.6, (pose, time) => {
      ["left_wing", "right_wing", "left_scale_wing", "right_scale_wing"].forEach(name => pose.show(name, false));
      const step = sin(time * 4.0);
      pose.setRot("left_front_leg_top", 0.4059698 - (step + 1) * 0.65, null, null);
      pose.setRot("right_back_leg_top", 0.4059698 - (step + 1) * 0.65, null, null);
      pose.setRot("right_front_leg_top", 0.4059698 - (cos(time * 4) + 1) * 0.65, null, null);
      pose.setRot("left_back_leg_top", 0.4059698 - (cos(time * 4) + 1) * 0.65, null, null);
    }),
    clip("fly", "飞行振翅", 1.5, (pose, time) => {
      ["left_wing", "right_wing", "left_scale_wing", "right_scale_wing"].forEach(name => pose.show(name, true));
      const flap = (sin(time * 10.7) + 1) * 0.68;
      pose.setRot("right_wing", null, null, flap - PI / 2);
      pose.setRot("left_wing", null, null, -flap + PI / 2);
      pose.setRot("right_scale_wing", null, null, flap);
      pose.setRot("left_scale_wing", null, null, -flap);
      ["left_front_leg_top", "right_front_leg_top", "left_back_leg_top", "right_back_leg_top"].forEach(name => pose.setRot(name, PI / 4, null, null));
    }),
    clip("glide", "滑翔下落", 3, (pose, time) => {
      ["left_wing", "right_wing", "left_scale_wing", "right_scale_wing"].forEach(name => pose.show(name, true));
      const sway = sin(time * 3.2) * 0.04;
      pose.setRot("right_wing", null, null, -0.35 + sway);
      pose.setRot("left_wing", null, null, 0.35 - sway);
      pose.setRot("right_scale_wing", null, null, 1.15 + sway);
      pose.setRot("left_scale_wing", null, null, -1.15 - sway);
    })
  ],
  GingerbreadManModel: [
    clip("idle", "待机", 5, (pose, time) => headTurn(pose, ["head", "hat"], time, 0.18)),
    clip("walk", "行走", 1.2, (pose, time) => {
      const step = cos(time * 5.23) * 1.15;
      pose.setRot("right_leg", step, 0, 0); pose.setRot("left_leg", -step, 0, 0);
      pose.setRot("right_arm", -step, 0, 0); pose.setRot("left_arm", step, 0, 0);
    }),
    clip("attack", "挥手攻击", 0.8, (pose, time) => {
      const strike = sin(Math.min(1, time / 0.8) * PI);
      pose.setRot("right_arm", -strike * 2.1, 0, -strike * 0.2);
    }, false)
  ],
  GummyBearModel: [
    clip("idle", "待机呼吸", 5, (pose, time) => {
      pose.setRot("body", PI / 2, null, null);
      pose.offsetPos("head", 0, sin(time * 0.8) * 0.3, 0);
      pose.offsetPos("body", 0, sin(time * 0.8 + PI / 2) * 0.3, 0);
    }),
    clip("walk", "四足行走", 1.2, (pose, time) => {
      pose.setRot("body", PI / 2, null, null);
      quadrupedWalk(pose, time, ["leg1", "leg0", "leg3", "leg2"], 1.4);
      quadrupedWalk(pose, time, ["leg1_outer", "leg2_outer", "leg4_outer", "leg3_outer"], 1.4);
    }),
    clip("stand", "站立", 2.4, (pose, time) => {
      const stand = (sin(time * PI / 2 - PI / 2) + 1) * 0.5;
      pose.setRot("body", PI / 2 - stand * PI * 0.35, null, null);
      pose.setPos("body", null, 9 + 2 * stand, null);
      pose.addRot("leg2", -stand * PI * 0.45, 0, 0);
      pose.addRot("leg3", -stand * PI * 0.45, 0, 0);
    })
  ],
  GummyBunnyModel: [
    clip("idle", "待机", 5, (pose, time) => headTurn(pose, ["head", "whiskers", "ear_left", "ear_right"], time, 0.18)),
    clip("hop", "跳跃奔跑", 1.1, (pose, time) => {
      const phase = (sin(time * PI * 2 / 1.1 - PI / 2) + 1) * 0.5;
      const swing = cos(time * 5.71) * 0.9;
      pose.setRot("front_leg_right", swing, null, null);
      pose.setRot("front_leg_left", -swing, null, null);
      pose.setRot("back_leg_right", phase * 0.4, null, null);
      pose.setRot("back_leg_left", phase * 0.4, null, null);
      pose.root.position.y += sin(phase * PI) * 0.16;
    }),
    clip("airborne", "空中姿势", 2, (pose, time) => {
      const bob = sin(time * PI) * 0.08;
      pose.setRot("back_leg_right", 0.4, null, null);
      pose.setRot("back_leg_left", 0.4, null, null);
      pose.root.position.y += bob;
    })
  ],
  GummyMouseModel: [
    clip("idle", "待机摆尾", 3, (pose, time) => {
      pose.offsetPos("body", sin(time * 12 + 0.3) * 0.004, sin(time) * 0.04, 0);
      pose.setRot("tail", null, sin(time * 12 + 0.3) * PI * 0.04, null);
    })
  ],
  GummyMouseOuterModel: [
    clip("idle", "外层摆尾", 3, (pose, time) => pose.setRot("tail", null, sin(time * 12) * PI * 0.04, null))
  ],
  MermaidModel: [
    clip("idle", "水中待机", 4, (pose, time) => {
      const sway = sin(time * 3.2) * 0.08;
      [["shape21", .35], ["shape22", .55], ["shape23", .75], ["shape24", 1], ["shape25", 1.25], ["shape5", 1.55]]
        .forEach(([name, scale]) => pose.setRot(name, null, sway * scale, null));
    }),
    clip("swim", "游动", 2, (pose, time) => {
      const sway = sin(time * 6) * 0.2;
      pose.setRot("shape4", -PI / 2 + cos(time * 5.3) * 0.22, null, null);
      pose.setRot("shape41", -PI / 2 - cos(time * 5.3) * 0.22, null, null);
      [["shape21", .35], ["shape22", .55], ["shape23", .75], ["shape24", 1], ["shape25", 1.25], ["shape5", 1.55]]
        .forEach(([name, scale]) => pose.setRot(name, null, sway * scale, null));
    })
  ],
  NessieModel: [
    clip("swim", "划水", 3, (pose, time) => {
      const rear = cos(time * 3.2);
      const front = cos((time * 4 + 4) * 0.8);
      pose.setRot("leg7", null, rear, null); pose.setRot("leg1", null, rear, null);
      pose.setRot("leg8", null, front, null); pose.setRot("leg2", null, front, null);
    })
  ],
  NougatGolemModel: [
    clip("stack_spin", "堆叠旋转", 4, (pose, time) => pose.setRot("cube", 0, time * 2.7, 0)),
    clip("move", "堆叠移动", 2, (pose, time) => {
      const phase = time * 6.8;
      pose.setRot("cube", sin(phase) * 0.075, 0, cos(phase * 0.85) * 0.09);
      pose.offsetPos("cube", 0, -Math.abs(sin(phase)) * 0.65, 0);
    }),
    clip("attack", "砸击", 1.2, (pose, time) => {
      const impact = sin(Math.min(1, time / 1.2) * PI);
      pose.addRot("cube", -0.34 * impact, 0, sin(time * 18) * 0.13 * impact);
      pose.offsetPos("cube", 0, -1.8 * impact, 0);
    }, false)
  ],
  PingouinModel: [
    clip("idle", "待机扇翅", 5, (pose, time) => {
      const flap = (sin(time * 2.4) + 1) * 0.045;
      pose.setRot("wing_left", null, null, 0.2230717 + flap);
      pose.setRot("wing_right", null, null, -0.2230717 - flap);
      pose.setRot("crest", 0.2230717 + sin(time * 2.4) * 0.08, null, null);
    }),
    clip("walk", "摇摆行走", 1.3, (pose, time) => {
      const step = time * 4.83;
      const waddle = sin(step) * 0.18;
      const bounce = Math.abs(cos(step)) * 0.45;
      ["body", "head", "beak", "crest", "wing_left", "wing_right"].forEach(name => pose.offsetPos(name, 0, -bounce, 0));
      pose.addRot("body", 0.06, 0, waddle);
      pose.setRot("foot_left", cos(step) * 0.85, 0.3490659 - waddle * 0.8, -waddle * 0.35);
      pose.setRot("foot_right", -cos(step) * 0.85, -0.3490659 - waddle * 0.8, -waddle * 0.35);
      pose.setRot("wing_left", -waddle * 0.8, null, 0.2230717 + Math.abs(sin(step)) * 0.3);
      pose.setRot("wing_right", waddle * 0.8, null, -0.2230717 - Math.abs(sin(step)) * 0.3);
    }),
    clip("fly", "空中振翅", 1.4, (pose, time) => {
      const flap = (sin(time * 15) + 1) * 0.55;
      pose.setRot("wing_left", 0, null, 0.2230717 + flap);
      pose.setRot("wing_right", 0, null, -0.2230717 - flap);
    }),
    clip("super", "超级企鹅冠饰", 5, (pose, time) => {
      pose.show("crest", true);
      pose.setRot("crest", 0.2230717 + sin(time * 2.4) * 0.08, null, null);
      headTurn(pose, ["head", "beak"], time, 0.18);
    })
  ],
  SuguardModel: suguard,
  WaffleSheepModel: [
    clip("idle", "待机", 5, (pose, time) => headTurn(pose, ["head"], time, 0.2)),
    clip("walk", "四足行走", 1.2, (pose, time) => {
      quadrupedWalk(pose, time, ["right_hind_leg", "left_hind_leg", "right_front_leg", "left_front_leg"], 1.4);
    })
  ]
};

export function animationsFor(modelId) {
  return ENTITY_ANIMATIONS[modelId] || [];
}

export function applyEntityAnimation(root, animation, time) {
  if (!root?.userData?.bones || !animation) return;
  const bones = root.userData.bones;
  const reset = () => {
    for (const bone of bones.values()) {
      bone.position.copy(bone.userData.basePosition);
      bone.rotation.copy(bone.userData.baseRotation);
      bone.visible = bone.userData.baseVisibility;
    }
    root.position.copy(root.userData.basePosition);
    root.rotation.copy(root.userData.baseRotation);
  };
  const bone = name => bones.get(name);
  const pose = {
    root,
    setRot(name, x, y, z) {
      const part = bone(name); if (!part) return;
      if (x != null) part.rotation.x = -x;
      if (y != null) part.rotation.y = -y;
      if (z != null) part.rotation.z = z;
    },
    addRot(name, x = 0, y = 0, z = 0) {
      const part = bone(name); if (!part) return;
      part.rotation.x -= x; part.rotation.y -= y; part.rotation.z += z;
    },
    setPos(name, x, y, z) {
      const part = bone(name); if (!part) return;
      const java = part.userData.javaBasePosition;
      const base = part.userData.basePosition;
      if (x != null) part.position.x = base.x - (x - java[0]);
      if (y != null) part.position.y = base.y - (y - java[1]);
      if (z != null) part.position.z = base.z + (z - java[2]);
    },
    offsetPos(name, x = 0, y = 0, z = 0) {
      const part = bone(name); if (!part) return;
      part.position.x -= x; part.position.y -= y; part.position.z += z;
    },
    show(name, visible) { const part = bone(name); if (part) part.visible = visible; }
  };
  reset();
  animation.apply(pose, time);
}
