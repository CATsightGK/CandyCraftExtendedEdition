# CandyCraft translation guide

All player-facing translations are defined in one canonical directory:

- `src/main/resources/assets/candycraftmod/lang/en_us.json`
- `src/main/resources/assets/candycraftmod/lang/zh_cn.json`

Resource packs must inherit these files. Do not add duplicate language files below
`src/main/resources/resourcepacks/`; a resource pack should override only art and other
resources that actually differ.

## Category order

Both locale files use the same keys and the same section order. Blank lines separate
the sections because Minecraft language JSON does not support comments.

1. Creative tabs: `itemGroup.*`
2. Blocks: `block.*`
3. Items: `item.*`
4. Entities: `entity.*`
5. World content: `biome.*`, `dimension.*`, `fluid.*`
6. Interfaces and resource packs: `container.*`, `gui.*`, `screen.*`, `button.*`, `overlay.*`, `jei.*`, `resourcePack.*`
7. Player guidance: `tooltip.*`, `message.*`, `chat.*`, `key.*`, `curios.*`, `gingerbread.*`
8. Advancements: `advancements.*`
9. Wiki: `wiki.*`

## Editing rules

- Add every new key to both locale files in the matching section.
- Keep keys alphabetically ordered inside each section.
- Preserve formatting placeholders such as `%s`, `%1$s`, and line breaks exactly.
- Save files as UTF-8 JSON. Do not add comments or trailing commas.
- Keep translations out of Java code and resource-pack-specific language folders.

Run `gradlew.bat processResources` after editing. The build must parse both JSON files
and package them at `assets/candycraftmod/lang/`.
