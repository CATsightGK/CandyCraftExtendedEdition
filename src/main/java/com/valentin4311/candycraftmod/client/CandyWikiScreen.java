package com.valentin4311.candycraftmod.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.math.Axis;
import com.valentin4311.candycraftmod.CandyCraft;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

public class CandyWikiScreen extends Screen {
    private static final int BOOK_WIDTH = 300;
    private static final int BOOK_HEIGHT = 198;
    private static final int PAGE_BUTTON_SIZE = 20;
    private static final int PAGE_BUTTON_SAFE_EXTENT = PAGE_BUTTON_SIZE * 2;
    private static final float MAX_BOOK_SCALE = 1.15F;
    private static final int LEFT_PAGE_X = 18;
    private static final int PAGE_Y = 18;
    private static final int PAGE_WIDTH = 120;
    private static final int STICKER_TEXTURE_SIZE = 32;
    private static final int INK = 0xFF3F251F;
    private static final int MUTED = 0xFF74483D;
    private static final ResourceLocation PAGES_JSON = new ResourceLocation(CandyCraft.MODID, "wiki/pages.json");
    private static final ResourceLocation BOOK_BACKGROUND =
        new ResourceLocation(CandyCraft.MODID, "textures/gui/wiki_book.png");
    private static final ResourceLocation ITEM_SLOT =
        new ResourceLocation(CandyCraft.MODID, "textures/gui/wiki_item_slot.png");
    private static final ResourceLocation MARSHMALLOW_WORKBENCH =
        new ResourceLocation(CandyCraft.MODID, "textures/gui/container/marshmallow_workbench_normal.png");
    private static final ResourceLocation MARSHMALLOW_WORKBENCH_DARK =
        new ResourceLocation(CandyCraft.MODID, "textures/gui/container/marshmallow_workbench_dark.png");
    private static final ResourceLocation MARSHMALLOW_WORKBENCH_LIGHT =
        new ResourceLocation(CandyCraft.MODID, "textures/gui/container/marshmallow_workbench_light.png");
    private static final Map<String, ResourceLocation> STICKERS = Map.ofEntries(
        Map.entry("candy_arrow_right", sticker("candy_arrow_right")),
        Map.entry("candy_arrow_curved", sticker("candy_arrow_curved")),
        Map.entry("candy_exclamation", sticker("candy_exclamation")),
        Map.entry("candy_star", sticker("candy_star")),
        Map.entry("candy_star_cluster", sticker("candy_star_cluster")),
        Map.entry("candy_heart", sticker("candy_heart")),
        Map.entry("candy_double_heart", sticker("candy_double_heart")),
        Map.entry("pink_face_happy", sticker("pink_face_happy")),
        Map.entry("pink_face_surprised", sticker("pink_face_surprised")),
        Map.entry("journal_arrow_right", sticker("journal_arrow_right")),
        Map.entry("journal_arrow_curved", sticker("journal_arrow_curved")),
        Map.entry("journal_bow", sticker("journal_bow")),
        Map.entry("journal_washi_tape", sticker("journal_washi_tape")),
        Map.entry("journal_flower", sticker("journal_flower")),
        Map.entry("journal_sparkles", sticker("journal_sparkles")),
        Map.entry("journal_heart_stamp", sticker("journal_heart_stamp")),
        Map.entry("candy_arrow_ribbon_right", sticker("candy_arrow_ribbon_right")),
        Map.entry("candy_arrow_hook_up", sticker("candy_arrow_hook_up")),
        Map.entry("candy_arrow_double_right", sticker("candy_arrow_double_right")),
        Map.entry("candy_arrow_sprinkle_down", sticker("candy_arrow_sprinkle_down")),
        Map.entry("candy_question_gummy", sticker("candy_question_gummy")),
        Map.entry("candy_question_cookie", sticker("candy_question_cookie")),
        Map.entry("candy_question_cream", sticker("candy_question_cream")),
        Map.entry("journal_lollipop_pair", sticker("journal_lollipop_pair")),
        Map.entry("journal_cupcake", sticker("journal_cupcake")),
        Map.entry("journal_candy_cluster", sticker("journal_candy_cluster")),
        Map.entry("journal_strawberry", sticker("journal_strawberry")),
        Map.entry("journal_candy_crown", sticker("journal_candy_crown")),
        Map.entry("journal_music_note", sticker("journal_music_note")),
        Map.entry("journal_star_garland", sticker("journal_star_garland"))
    );

    private List<WikiPage> pages = List.of();
    private int pageIndex;
    private Button previousButton;
    private Button nextButton;
    private int previousButtonX = 16;
    private int previousButtonY = 168;
    private int nextButtonX = 264;
    private int nextButtonY = 168;
    private LivingEntity previewEntity;

    public CandyWikiScreen() {
        super(Component.translatable("screen.candycraftmod.wiki.title"));
    }

    @Override
    protected void init() {
        pages = loadPages();
        pageIndex = Math.min(pageIndex, Math.max(0, pages.size() - 1));
        float scale = bookScale();
        int x = bookX(scale);
        int y = bookY(scale);
        previousButton = addRenderableWidget(new WikiPageButton(x, y, false, button -> changePage(-1)));
        nextButton = addRenderableWidget(new WikiPageButton(x, y, true, button -> changePage(1)));
        updateButtons();
        updatePreviewEntity();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        float scale = bookScale();
        int x = bookX(scale);
        int y = bookY(scale);
        int localMouseX = Math.round((mouseX - x) / scale);
        int localMouseY = Math.round((mouseY - y) / scale);
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        drawBook(graphics, 0, 0);
        drawPage(graphics, 0, 0, localMouseX, localMouseY);
        graphics.pose().popPose();
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > 0.0D) {
            changePage(-1);
            return true;
        }
        if (delta < 0.0D) {
            changePage(1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 262) {
            changePage(1);
            return true;
        }
        if (keyCode == 263) {
            changePage(-1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void changePage(int delta) {
        int next = Math.max(0, Math.min(pages.size() - 1, pageIndex + delta));
        if (next != pageIndex) {
            pageIndex = next;
            updateButtons();
            updatePreviewEntity();
        }
    }

    private void updateButtons() {
        float scale = bookScale();
        int bookX = bookX(scale);
        int bookY = bookY(scale);
        if (previousButton != null) {
            previousButton.active = pageIndex > 0;
            previousButton.setX(bookX + Math.round(previousButtonX * scale));
            previousButton.setY(bookY + Math.round(previousButtonY * scale));
        }
        if (nextButton != null) {
            nextButton.active = pageIndex < pages.size() - 1;
            nextButton.setX(bookX + Math.round(nextButtonX * scale));
            nextButton.setY(bookY + Math.round(nextButtonY * scale));
        }
    }

    private float bookScale() {
        float availableWidthScale = (width - 24.0F) / canvasWidth();
        float availableHeightScale = (height - 40.0F) / canvasHeight();
        return Math.max(0.5F, Math.min(MAX_BOOK_SCALE, Math.min(availableWidthScale, availableHeightScale)));
    }

    private int bookX(float scale) {
        return (width - Math.round(canvasWidth() * scale)) / 2 - Math.round(canvasMinX() * scale);
    }

    private int bookY(float scale) {
        return (height - Math.round(canvasHeight() * scale)) / 2 - Math.round(canvasMinY() * scale);
    }

    private int canvasMinX() {
        return Math.min(0, Math.min(previousButtonX, nextButtonX));
    }

    private int canvasMinY() {
        return Math.min(0, Math.min(previousButtonY, nextButtonY));
    }

    private int canvasWidth() {
        int maxX = Math.max(BOOK_WIDTH, Math.max(previousButtonX, nextButtonX) + PAGE_BUTTON_SAFE_EXTENT);
        return maxX - canvasMinX();
    }

    private int canvasHeight() {
        int maxY = Math.max(BOOK_HEIGHT, Math.max(previousButtonY, nextButtonY) + PAGE_BUTTON_SAFE_EXTENT);
        return maxY - canvasMinY();
    }

    private void updatePreviewEntity() {
        previewEntity = null;
        if (minecraft == null || minecraft.level == null || pages.isEmpty()) {
            return;
        }
        for (WikiElement element : pages.get(pageIndex).elements()) {
            if ("entity".equals(element.type())) {
                EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(element.id());
                Entity entity = type == null ? null : type.create(minecraft.level);
                if (entity instanceof LivingEntity living) {
                    previewEntity = living;
                    previewEntity.setYRot(25.0F);
                    previewEntity.yRotO = 25.0F;
                }
                return;
            }
        }
    }

    private void drawBook(GuiGraphics graphics, int x, int y) {
        graphics.blit(BOOK_BACKGROUND, x, y, 0, 0, BOOK_WIDTH, BOOK_HEIGHT, BOOK_WIDTH, BOOK_HEIGHT);
    }

    private void drawPage(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        if (pages.isEmpty()) {
            return;
        }
        WikiPage page = pages.get(pageIndex);
        graphics.drawString(font, component(page.titleKey(), page.title()), x + LEFT_PAGE_X + page.titleX(), y + PAGE_Y + page.titleY(), INK, false);
        graphics.drawString(font, Component.translatable("screen.candycraftmod.wiki.page", pageIndex + 1, pages.size()), x + 216, y + 177, MUTED, false);
        for (WikiElement element : page.elements()) {
            renderElement(graphics, element, x + LEFT_PAGE_X, y + PAGE_Y, mouseX, mouseY);
        }
    }

    private void renderElement(GuiGraphics graphics, WikiElement element, int pageX, int pageY, int mouseX, int mouseY) {
        int x = pageX + element.x();
        int y = pageY + element.y();
        graphics.pose().pushPose();
        if (element.rotation() != 0.0F) {
            graphics.pose().translate(x + element.width() / 2.0F, y + element.height() / 2.0F, 0.0F);
            graphics.pose().mulPose(Axis.ZP.rotationDegrees(element.rotation()));
            graphics.pose().translate(-(x + element.width() / 2.0F), -(y + element.height() / 2.0F), 0.0F);
        }
        switch (element.type()) {
            case "text" -> renderText(graphics, element, x, y);
            case "item", "item_slot" -> renderItemBox(graphics, element, x, y);
            case "entity" -> renderEntityBox(graphics, element, x, y, mouseX, mouseY);
            case "workbench_gui" -> renderWorkbench(graphics, element, x, y);
            case "sticker" -> renderSticker(graphics, element, x, y);
            default -> {
            }
        }
        graphics.pose().popPose();
    }

    private void renderText(GuiGraphics graphics, WikiElement element, int x, int y) {
        Component text = component(element.key(), element.text());
        float scale = Math.max(0.5F, element.scale());
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 150.0F);
        graphics.pose().scale(scale, scale, scale);
        graphics.drawWordWrap(font, text, 0, 0, Math.max(12, (int) (element.width() / scale)), element.color());
        graphics.pose().popPose();
    }

    private void renderItemBox(GuiGraphics graphics, WikiElement element, int x, int y) {
        if (element.slot()) {
            graphics.blit(ITEM_SLOT, x, y, 0, 0, 38, 38, 38, 38);
        }
        Item item = ForgeRegistries.ITEMS.getValue(element.id());
        if (item == null || item == Items.AIR) {
            return;
        }
        float scaleX = element.slot() ? 2.0F : Math.max(1, element.width()) / 16.0F;
        float scaleY = element.slot() ? 2.0F : Math.max(1, element.height()) / 16.0F;
        int insetX = element.slot() ? 3 : 0;
        int insetY = element.slot() ? 3 : 0;
        graphics.pose().pushPose();
        graphics.pose().translate(x + insetX, y + insetY, 180.0F);
        graphics.pose().scale(scaleX, scaleY, Math.max(scaleX, scaleY));
        graphics.renderItem(new ItemStack(item), 0, 0);
        graphics.pose().popPose();
    }

    private void renderEntityBox(GuiGraphics graphics, WikiElement element, int x, int y, int mouseX, int mouseY) {
        int width = Math.max(24, element.width());
        int height = Math.max(24, element.height());
        drawPinkFrame(graphics, x, y, width, height);
        if (previewEntity != null) {
            float entityWidth = Math.max(0.1F, previewEntity.getBbWidth());
            float entityHeight = Math.max(0.1F, previewEntity.getBbHeight());
            int fittedScale = Math.max(1, Math.round(Math.min((width - 8) / entityWidth, (height - 8) / entityHeight)));
            float scale = bookScale();
            int screenX = bookX(scale);
            int screenY = bookY(scale);
            graphics.enableScissor(
                screenX + Math.round((x + 2) * scale),
                screenY + Math.round((y + 2) * scale),
                screenX + Math.round((x + width - 2) * scale),
                screenY + Math.round((y + height - 2) * scale)
            );
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                graphics,
                x + width / 2,
                y + height - 4,
                fittedScale,
                x + width / 2 - mouseX,
                y + height / 2 - mouseY,
                previewEntity
            );
            graphics.disableScissor();
        }
    }

    private void renderWorkbench(GuiGraphics graphics, WikiElement element, int x, int y) {
        ResourceLocation texture = switch (element.theme().toLowerCase(Locale.ROOT)) {
            case "dark" -> MARSHMALLOW_WORKBENCH_DARK;
            case "light" -> MARSHMALLOW_WORKBENCH_LIGHT;
            default -> MARSHMALLOW_WORKBENCH;
        };
        float scale = Math.max(0.25F, element.scale());
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 90.0F);
        graphics.pose().scale(scale, scale, scale);
        graphics.blit(texture, 0, 0, 0, 0, 176, 166, 176, 166);
        graphics.pose().popPose();
    }

    private void renderSticker(GuiGraphics graphics, WikiElement element, int x, int y) {
        ResourceLocation texture = STICKERS.get(element.asset());
        if (texture != null) {
            float scaleX = Math.max(1, element.width()) / (float) STICKER_TEXTURE_SIZE;
            float scaleY = Math.max(1, element.height()) / (float) STICKER_TEXTURE_SIZE;
            graphics.pose().pushPose();
            graphics.pose().translate(x, y, 140.0F);
            graphics.pose().scale(scaleX, scaleY, 1.0F);
            graphics.blit(texture, 0, 0, 0, 0, STICKER_TEXTURE_SIZE, STICKER_TEXTURE_SIZE, STICKER_TEXTURE_SIZE, STICKER_TEXTURE_SIZE);
            graphics.pose().popPose();
        }
    }

    private void drawPinkFrame(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xFF8B5A50);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFFF4BFD0);
        graphics.fill(x + 1, y + 1, x + width - 1, y + 2, 0xFFB86F88);
        graphics.fill(x + 1, y + 1, x + 2, y + height - 1, 0xFFB86F88);
        graphics.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, 0xFFFFF0F5);
        graphics.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, 0xFFFFF0F5);
    }

    private Component component(String key, String literal) {
        return key == null || key.isBlank() ? Component.literal(literal) : Component.translatable(key);
    }

    private List<WikiPage> loadPages() {
        if (minecraft != null) {
            try {
                Resource resource = minecraft.getResourceManager().getResource(PAGES_JSON).orElse(null);
                if (resource != null) {
                    try (Reader reader = resource.openAsReader()) {
                        List<WikiPage> loaded = parsePages(JsonParser.parseReader(reader).getAsJsonObject());
                        if (!loaded.isEmpty()) {
                            return loaded;
                        }
                    }
                }
            } catch (RuntimeException | java.io.IOException ignored) {
            }
        }
        return fallbackPages();
    }

    private List<WikiPage> parsePages(JsonObject root) {
        JsonArray pagesJson = GsonHelper.getAsJsonArray(root, "pages", new JsonArray());
        JsonObject buttonJson = root.has("pageButtons") && root.get("pageButtons").isJsonObject()
            ? root.getAsJsonObject("pageButtons")
            : new JsonObject();
        JsonObject legacyPage = pagesJson.isEmpty() ? new JsonObject() : pagesJson.get(0).getAsJsonObject();
        previousButtonX = GsonHelper.getAsInt(buttonJson, "previousX", GsonHelper.getAsInt(legacyPage, "previousButtonX", 16));
        previousButtonY = GsonHelper.getAsInt(buttonJson, "previousY", GsonHelper.getAsInt(legacyPage, "previousButtonY", 168));
        nextButtonX = GsonHelper.getAsInt(buttonJson, "nextX", GsonHelper.getAsInt(legacyPage, "nextButtonX", 264));
        nextButtonY = GsonHelper.getAsInt(buttonJson, "nextY", GsonHelper.getAsInt(legacyPage, "nextButtonY", 168));
        List<WikiPage> parsed = new ArrayList<>();
        for (JsonElement pageElement : pagesJson) {
            JsonObject pageJson = pageElement.getAsJsonObject();
            String key = GsonHelper.getAsString(pageJson, "key", "page_" + parsed.size());
            String titleKey = GsonHelper.getAsString(pageJson, "titleKey", "wiki.candycraftmod." + key + ".title");
            String title = GsonHelper.getAsString(pageJson, "title", key);
            int titleX = GsonHelper.getAsInt(pageJson, "titleX", 0);
            int titleY = GsonHelper.getAsInt(pageJson, "titleY", 0);
            List<WikiElement> elements = new ArrayList<>();
            JsonArray elementJson = GsonHelper.getAsJsonArray(pageJson, "elements", new JsonArray());
            for (JsonElement entry : elementJson) {
                elements.add(parseElement(entry.getAsJsonObject()));
            }
            parsed.add(new WikiPage(key, titleKey, title, titleX, titleY, elements));
        }
        return parsed;
    }

    private static WikiElement parseElement(JsonObject json) {
        String type = GsonHelper.getAsString(json, "type", "text");
        boolean itemElement = "item".equals(type) || "item_slot".equals(type);
        boolean slot = GsonHelper.getAsBoolean(json, "slot", itemElement);
        boolean fixedItemSlot = itemElement && slot;
        int size = fixedItemSlot ? 38 : GsonHelper.getAsInt(json, "size", "entity".equals(type) ? 38 : 32);
        int defaultWidth = "entity".equals(type) ? 78 : itemElement ? Math.max(12, size) : PAGE_WIDTH;
        int defaultHeight = "entity".equals(type) ? 96 : itemElement ? Math.max(12, size) : 18;
        int width = fixedItemSlot ? 38 : GsonHelper.getAsInt(json, "width", defaultWidth);
        int height = fixedItemSlot ? 38 : GsonHelper.getAsInt(json, "height", defaultHeight);
        return new WikiElement(
            type,
            GsonHelper.getAsInt(json, "x", 0),
            GsonHelper.getAsInt(json, "y", 0),
            width,
            height,
            size,
            fixedItemSlot ? 2.0F : GsonHelper.getAsFloat(json, "scale", "text".equals(type) ? 1.0F : 2.0F),
            parseColor(GsonHelper.getAsString(json, "color", "#3F251F")),
            GsonHelper.getAsString(json, "key", ""),
            GsonHelper.getAsString(json, "text", ""),
            new ResourceLocation(GsonHelper.getAsString(json, "id", "minecraft:air")),
            slot,
            GsonHelper.getAsString(json, "theme", "normal"),
            GsonHelper.getAsString(json, "asset", ""),
            GsonHelper.getAsFloat(json, "rotation", 0.0F)
        );
    }

    private static int parseColor(String color) {
        String clean = color.startsWith("#") ? color.substring(1) : color;
        try {
            return 0xFF000000 | Integer.parseUnsignedInt(clean, 16);
        } catch (NumberFormatException ignored) {
            return INK;
        }
    }

    private static List<WikiPage> fallbackPages() {
        return List.of(
            page("portal", item(0, 34, "candycraftmod:sugar_block"), item(46, 34, "minecraft:lava_bucket"),
                item(0, 82, "candycraftmod:caramel_bucket"), item(46, 82, "candycraftmod:liquid_candy_bucket"), body("portal")),
            page("fork", item(0, 42, "candycraftmod:fork"), item(46, 42, "candycraftmod:sugar_block"),
                item(0, 90, "candycraftmod:red_gummy_block"), body("fork")),
            page("alchemy", item(0, 34, "candycraftmod:alchemy_table"), item(46, 34, "candycraftmod:alchemy_mixer_blade"),
                item(0, 82, "candycraftmod:sugar_pill"), workbench(34, 96, 0.32F), body("alchemy")),
            page("dungeon_keys", item(0, 34, "candycraftmod:jelly_key"), item(46, 34, "candycraftmod:jelly_sentry_key"),
                item(0, 82, "candycraftmod:jelly_boss_key"), item(46, 82, "candycraftmod:jelly_boss_key_hole"), body("dungeon_keys")),
            page("jelly_queen", entity(22, 28, "candycraftmod:jelly_queen", 46), item(50, 130, "candycraftmod:jelly_crown", 1.2F), body("jelly_queen")),
            page("suguard_boss", entity(26, 34, "candycraftmod:boss_suguard", 40), item(50, 130, "candycraftmod:suguard_emblem", 1.2F), body("suguard_boss")),
            page("dragon_egg", item(0, 34, "candycraftmod:dragon_egg_block"), item(46, 34, "candycraftmod:sugar_essence_flower"),
                item(0, 82, "candycraftmod:fraise_tagada_flower"), item(46, 82, "candycraftmod:dragon_spawn_egg"), body("dragon_egg")),
            page("emblems", item(0, 34, "candycraftmod:chewing_gum_emblem"), item(46, 34, "candycraftmod:jelly_emblem"),
                item(0, 82, "candycraftmod:water_emblem"), item(46, 82, "candycraftmod:honey_emblem"), body("emblems"))
        );
    }

    private static WikiPage page(String key, WikiElement... elements) {
        return new WikiPage(key, "wiki.candycraftmod." + key + ".title", key, 0, 0, List.of(elements));
    }

    private static WikiElement body(String key) {
        return new WikiElement("text", 146, 18, 116, 120, 0, 1.0F, INK, "wiki.candycraftmod." + key + ".body", "", new ResourceLocation("minecraft:air"), false, "normal", "", 0.0F);
    }

    private static WikiElement item(int x, int y, String id) {
        return item(x, y, id, 2.0F);
    }

    private static WikiElement item(int x, int y, String id, float scale) {
        return new WikiElement("item_slot", x, y, 38, 38, 38, scale, INK, "", "", new ResourceLocation(id), true, "normal", "", 0.0F);
    }

    private static WikiElement entity(int x, int y, String id, int size) {
        return new WikiElement("entity", x, y, 78, 96, size, 1.0F, INK, "", "", new ResourceLocation(id), true, "normal", "", 0.0F);
    }

    private static WikiElement workbench(int x, int y, float scale) {
        return new WikiElement("workbench_gui", x, y, 176, 166, 0, scale, INK, "", "", new ResourceLocation("minecraft:air"), false, "normal", "", 0.0F);
    }

    private static ResourceLocation sticker(String name) {
        return new ResourceLocation(CandyCraft.MODID, "textures/gui/wiki_stickers/" + name + ".png");
    }

    private record WikiPage(String key, String titleKey, String title, int titleX, int titleY,
                            List<WikiElement> elements) {
    }

    private record WikiElement(String type, int x, int y, int width, int height, int size, float scale, int color,
                               String key, String text, ResourceLocation id, boolean slot, String theme, String asset,
                               float rotation) {
    }
}
