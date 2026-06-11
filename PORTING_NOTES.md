# CandyCraft 1.20.1 Porting Notes

These rules are mandatory when adding or fixing migrated content.

## General

- Treat the target as Forge 1.20.1. Do not use old 1.8.9 or 1.12.2 JSON/code formats directly.
- If a block or item behaves like vanilla, copy the vanilla class/model/blockstate pattern first, then swap CandyCraft-specific textures or values.
- Keep all resource paths lowercase. `ResourceLocation` does not accept uppercase names.
- Do not duplicate 1.8.9 and 1.12.2 content. Use 1.12.2 only as a reference to identify the same item or block.
- After resource edits, run `gradlew build`, then check `run/logs/latest.log` for the exact registry name and for `missing model` or `Unknown blockstate property`.

## Models And Blockstates

- No-property blockstates use the empty variant key `""`, not the old `" "` key.
- Use the parent model's required texture keys exactly:
  - `minecraft:block/cube_all`: `all`
  - `minecraft:block/cube_column`: `end`, `side`
  - `minecraft:block/cross`: `cross`
  - `minecraft:item/generated`: `layer0`
  - wall templates: `wall`
  - fence templates: `texture`
  - glass pane templates: `pane`, `edge`
- Walls must use vanilla wall templates and complete `east/north/south/west/up/waterlogged` blockstate variants.
- Fences must use vanilla fence templates and complete `east/north/south/west/waterlogged` blockstate variants.
- Stairs must use vanilla `stairs`, `inner_stairs`, and `outer_stairs` models and complete `facing/half/shape/waterlogged` variants.
- Slabs must use vanilla `slab`, `slab_top`, and `cube_all` models and complete `type/waterlogged` variants.
- Glass blocks should use `cube_all` unless a custom cutout model is actually needed.
- Glass pane inventory models should use an item model or the correct vanilla pane inventory pattern, not a placed-pane state.

## Items

- Vanilla-like handheld items should use vanilla parent/display conventions unless the old mod had a real special model.
- Bow and crossbow dynamic states require 1.20.1 predicates:
  - Bow: `pulling`, `pull`
  - Crossbow: `pulling`, `pull`, `charged`
- Projectile items need both item texture and entity renderer texture. The fired entity and pickup stack must be the mod item.

## Armor

- Inventory item textures live in `textures/item`.
- Worn armor textures live in `textures/models/armor`.
- Vanilla humanoid armor uses layer 1 for helmet/chestplate/boots and layer 2 for leggings.
- If old texture names differ from material names, bind the worn texture explicitly in the armor item with `getArmorTexture`.
- Verify every armor set by wearing helmet, chestplate, leggings, and boots at the same time and one by one.
