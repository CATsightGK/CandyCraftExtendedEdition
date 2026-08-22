package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.world.CCDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = CandyCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class JellyQueenBossBarOverlay {
    private static final BossBarLayout JELLY_BOSS_BAR_LAYOUT = new BossBarLayout(
        new ResourceLocation(CandyCraft.MODID, "textures/gui/jelly_queen_boss_bar.png"),
        128, 5, 9, 116, 17, 8, 46, 18, 46, 4, 3, 9);
    private static final BossBarLayout LICORICE_BEETLE_LAYOUT = new BossBarLayout(
        new ResourceLocation(CandyCraft.MODID, "textures/gui/licorice_beetle_boss_bar.png"),
        128, 5, 8, 117, 17, 8, 46, 19, 46, 4, 3, 10);
    private static final BossBarLayout SUGUARD_TOTEM_LAYOUT = new BossBarLayout(
        new ResourceLocation(CandyCraft.MODID, "textures/gui/suguard_totem_boss_bar.png"),
        128, 0, 8, 128, 18, 3, 46, 30, 46, 4, 3, 10);
    private static final BossBarStyle JELLY_QUEEN_STYLE = new BossBarStyle(
        "entity.candycraftmod.jelly_queen", "gui.candycraftmod.jelly_queen.angry_subtitle",
        JELLY_BOSS_BAR_LAYOUT, -1, 0xFFD6E5);
    private static final BossBarStyle PEZ_JELLY_STYLE = new BossBarStyle(
        "entity.candycraftmod.pez_jelly", "gui.candycraftmod.pez_jelly.subtitle",
        JELLY_BOSS_BAR_LAYOUT, 42, 0xEEEEEE);
    private static final BossBarStyle JELLY_KING_STYLE = new BossBarStyle(
        "entity.candycraftmod.king_slime", "gui.candycraftmod.king_slime.subtitle",
        JELLY_BOSS_BAR_LAYOUT, 47, 0xFFB15A);
    private static final BossBarStyle LICORICE_BEETLE_STYLE = new BossBarStyle(
        "entity.candycraftmod.boss_beetle", "gui.candycraftmod.boss_beetle.subtitle",
        LICORICE_BEETLE_LAYOUT, 27, 0xFFFFFF);
    private static final BossBarStyle SUGUARD_TOTEM_STYLE = new BossBarStyle(
        "entity.candycraftmod.boss_suguard", "gui.candycraftmod.boss_suguard.subtitle",
        SUGUARD_TOTEM_LAYOUT, 27, 0xFFFFFF);
    private static final float BAR_SCALE = 1.5F;
    private static final float SUBTITLE_SCALE = 0.625F;
    private static final int PINK_FILL_V = 27;
    private static final int BLUE_FILL_V = 32;
    private static final int CARAMEL_FILL_V = 37;
    private static final float FADE_IN_PER_SECOND = 2.5F;
    private static final float FADE_OUT_PER_SECOND = 2.0F;
    private static final float SUBTITLE_VISIBLE_SECONDS = 10.0F;
    private static final float SUBTITLE_FADE_OUT_PER_SECOND = 2.0F;
    private static final float HIDDEN_SCALE_MULTIPLIER = 0.86F;
    private static final int HIDDEN_Y_OFFSET = 9;
    private static final Map<UUID, AnimatedBossBar> ANIMATED_BARS = new HashMap<>();
    private static final Set<UUID> VISIBLE_THIS_FRAME = new HashSet<>();
    private static final Map<BossBarStyle, List<AnimatedBossBar>> VISIBLE_GROUPS = new LinkedHashMap<>();
    private static int groupedBarsStartY = -1;

    private JellyQueenBossBarOverlay() {
    }

    @SubscribeEvent
    public static void beginBossBarFrame(RenderGuiOverlayEvent.Pre event) {
        if (isBossOverlay(event)) {
            VISIBLE_THIS_FRAME.clear();
            VISIBLE_GROUPS.clear();
            groupedBarsStartY = -1;
        }
    }

    @SubscribeEvent
    public static void renderJellyBossBar(CustomizeGuiOverlayEvent.BossEventProgress event) {
        LerpingBossEvent bossEvent = event.getBossEvent();
        BossBarStyle style = styleFor(bossEvent);
        if (style == null) {
            return;
        }

        UUID id = bossEvent.getId();
        long now = System.nanoTime();
        AnimatedBossBar animatedBar = ANIMATED_BARS.computeIfAbsent(id, ignored -> new AnimatedBossBar(now));
        animatedBar.capture(style, bossEvent.getName(), bossEvent.getColor(), bossEvent.getProgress());
        animatedBar.update(now, true);
        VISIBLE_THIS_FRAME.add(id);

        List<AnimatedBossBar> group = VISIBLE_GROUPS.computeIfAbsent(style, ignored -> new java.util.ArrayList<>());
        boolean firstOfType = group.isEmpty();
        group.add(animatedBar);
        if (groupedBarsStartY < 0) {
            groupedBarsStartY = event.getY();
        }

        event.setCanceled(true);
        event.setIncrement(firstOfType
            ? subtitleGap(animatedBar, style) + displayedFrameHeight(style) + 10
            : compactBarIncrement(style));
    }

    @SubscribeEvent
    public static void finishBossBarFrame(RenderGuiOverlayEvent.Post event) {
        if (!isBossOverlay(event)) {
            return;
        }
        long now = System.nanoTime();
        int screenWidth = event.getWindow().getGuiScaledWidth();
        int groupY = groupedBarsStartY;
        if (groupY >= 0) {
            for (Map.Entry<BossBarStyle, List<AnimatedBossBar>> groupEntry : VISIBLE_GROUPS.entrySet()) {
                BossBarStyle style = groupEntry.getKey();
                List<AnimatedBossBar> bars = groupEntry.getValue();
                boolean showSubtitle = shouldShowSubtitle(style);
                int firstFrameY = groupY + subtitleGap(bars.get(0), style);
                for (int index = 0; index < bars.size(); index++) {
                    AnimatedBossBar animatedBar = bars.get(index);
                    animatedBar.setLayout(firstFrameY + index * compactBarIncrement(style), groupY,
                        index == 0, index == 0 && showSubtitle);
                    renderAnimatedBossBar(event.getGuiGraphics(), screenWidth, animatedBar);
                }
                int lastFrameY = firstFrameY + (bars.size() - 1) * compactBarIncrement(style);
                groupY = lastFrameY + displayedFrameHeight(style) + 10;
            }
        }

        Iterator<Map.Entry<UUID, AnimatedBossBar>> iterator = ANIMATED_BARS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, AnimatedBossBar> entry = iterator.next();
            if (VISIBLE_THIS_FRAME.contains(entry.getKey())) {
                continue;
            }
            AnimatedBossBar animatedBar = entry.getValue();
            animatedBar.update(now, false);
            if (animatedBar.visibility <= 0.0F) {
                iterator.remove();
            } else {
                if (VISIBLE_GROUPS.containsKey(animatedBar.style)) {
                    animatedBar.showLabels = false;
                    animatedBar.showSubtitle = false;
                }
                renderAnimatedBossBar(event.getGuiGraphics(), screenWidth, animatedBar);
            }
        }
    }

    @SubscribeEvent
    public static void clearBossBarAnimations(ClientPlayerNetworkEvent.LoggingOut event) {
        ANIMATED_BARS.clear();
        VISIBLE_THIS_FRAME.clear();
        VISIBLE_GROUPS.clear();
        groupedBarsStartY = -1;
    }

    private static void renderAnimatedBossBar(GuiGraphics graphics, int screenWidth, AnimatedBossBar animatedBar) {
        float opacity = smoothStep(animatedBar.visibility);
        if (opacity <= 0.0F) {
            return;
        }
        BossBarLayout layout = animatedBar.style.layout();
        float transformProgress = opacity;
        float scaleProgress = HIDDEN_SCALE_MULTIPLIER + (1.0F - HIDDEN_SCALE_MULTIPLIER) * transformProgress;
        float animatedScale = barScale(animatedBar.style) * scaleProgress;
        int frameX = (screenWidth - Math.round(layout.frameWidth() * animatedScale)) / 2;
        int animationYOffset = Math.round((1.0F - transformProgress) * HIDDEN_Y_OFFSET);
        int frameY = animatedBar.frameY + animationYOffset;

        graphics.setColor(1.0F, 1.0F, 1.0F, opacity);
        graphics.pose().pushPose();
        graphics.pose().translate(frameX, frameY, 0.0F);
        graphics.pose().scale(animatedScale, animatedScale, 1.0F);
        renderSplitFill(graphics, animatedBar, layout);
        graphics.pose().translate(0.0F, 0.0F, 1.0F);
        graphics.blit(layout.texture(), 0, 0, layout.frameU(), layout.frameV(),
            layout.frameWidth(), layout.frameHeight(), layout.textureSize(), layout.textureSize());
        graphics.pose().popPose();
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (!animatedBar.showLabels) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        int nameY = animatedBar.labelY - 9 + animationYOffset;
        graphics.drawString(font, animatedBar.name, (screenWidth - font.width(animatedBar.name)) / 2,
            nameY, colorWithAlpha(0xFFFFFF, opacity), true);

        if (animatedBar.showSubtitle) {
            float subtitleOpacity = opacity * smoothStep(animatedBar.subtitleVisibility);
            if (subtitleOpacity > 0.0F) {
                Component subtitle = Component.translatable(animatedBar.style.subtitleTranslationKey());
                float subtitleWidth = font.width(subtitle) * SUBTITLE_SCALE;
                float subtitleY = animatedBar.labelY + 1.0F + animationYOffset;
                graphics.pose().pushPose();
                graphics.pose().translate((screenWidth - subtitleWidth) / 2.0F,
                    subtitleY, 0.0F);
                graphics.pose().scale(SUBTITLE_SCALE, SUBTITLE_SCALE, 1.0F);
                graphics.drawString(font, subtitle, 0, 0, colorWithAlpha(animatedBar.style.subtitleColor(), subtitleOpacity), true);
                graphics.pose().popPose();
            }
        }
    }

    private static void renderSplitFill(GuiGraphics graphics, AnimatedBossBar animatedBar, BossBarLayout layout) {
        int totalFillWidth = layout.leftFillWidth() + layout.rightFillWidth();
        int filledPixels = Math.min(totalFillWidth,
            Math.max(0, (int)Math.ceil(animatedBar.progress * totalFillWidth)));
        int leftFilled = Math.min(layout.leftFillWidth(), filledPixels);
        int rightFilled = Math.min(layout.rightFillWidth(),
            Math.max(0, filledPixels - layout.leftFillWidth()));
        int fillV = fillV(animatedBar.style, animatedBar.bossBarColor);

        if (leftFilled > 0) {
            graphics.blit(layout.texture(), layout.fillXOffset(), layout.fillYOffset(),
                layout.fillU(), fillV, leftFilled, layout.fillHeight(),
                layout.textureSize(), layout.textureSize());
        }
        if (rightFilled > 0) {
            int rightOffset = layout.leftFillWidth() + layout.centerGapWidth();
            graphics.blit(layout.texture(), layout.fillXOffset() + rightOffset, layout.fillYOffset(),
                layout.fillU() + rightOffset, fillV, rightFilled, layout.fillHeight(),
                layout.textureSize(), layout.textureSize());
        }
    }

    private static boolean isBossOverlay(RenderGuiOverlayEvent event) {
        return VanillaGuiOverlay.BOSS_EVENT_PROGRESS.id().equals(event.getOverlay().id());
    }

    private static BossBarStyle styleFor(LerpingBossEvent bossEvent) {
        if (!(bossEvent.getName().getContents() instanceof TranslatableContents contents)) {
            return null;
        }
        String key = contents.getKey();
        if (JELLY_QUEEN_STYLE.entityTranslationKey().equals(key)) {
            return JELLY_QUEEN_STYLE;
        }
        if (PEZ_JELLY_STYLE.entityTranslationKey().equals(key)) {
            return PEZ_JELLY_STYLE;
        }
        if (JELLY_KING_STYLE.entityTranslationKey().equals(key)) {
            return JELLY_KING_STYLE;
        }
        if (LICORICE_BEETLE_STYLE.entityTranslationKey().equals(key)) {
            return LICORICE_BEETLE_STYLE;
        }
        return SUGUARD_TOTEM_STYLE.entityTranslationKey().equals(key) ? SUGUARD_TOTEM_STYLE : null;
    }

    private static boolean shouldShowSubtitle(BossBarStyle style) {
        if (style.subtitleTranslationKey() == null) {
            return false;
        }
        if (style == JELLY_QUEEN_STYLE || style == LICORICE_BEETLE_STYLE || style == SUGUARD_TOTEM_STYLE) {
            return true;
        }
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level != null && CCDimensions.JELLY_DUNGEON.equals(minecraft.level.dimension());
    }

    private static int fillV(BossBarStyle style, BossEvent.BossBarColor color) {
        if (style.fixedFillV() >= 0) {
            return style.fixedFillV();
        }
        return switch (color) {
            case BLUE -> BLUE_FILL_V;
            case YELLOW -> CARAMEL_FILL_V;
            default -> PINK_FILL_V;
        };
    }

    private static int displayedFrameHeight(BossBarStyle style) {
        return (int)Math.ceil(style.layout().frameHeight() * barScale(style));
    }

    // The subtitle reserves 8px between the name and the bar; once it fades
    // out the bar slides up to the 1px no-subtitle gap.
    private static int subtitleGap(AnimatedBossBar animatedBar, BossBarStyle style) {
        if (!shouldShowSubtitle(style)) {
            return 1;
        }
        return 1 + Math.round(7.0F * smoothStep(animatedBar.subtitleVisibility));
    }

    private static int compactBarIncrement(BossBarStyle style) {
        return displayedFrameHeight(style) + 1;
    }

    private static float barScale(BossBarStyle style) {
        if (style == SUGUARD_TOTEM_STYLE) {
            return BAR_SCALE * LICORICE_BEETLE_LAYOUT.frameWidth() / SUGUARD_TOTEM_LAYOUT.frameWidth();
        }
        return BAR_SCALE;
    }

    private static float smoothStep(float value) {
        float clamped = Math.max(0.0F, Math.min(1.0F, value));
        return clamped * clamped * (3.0F - 2.0F * clamped);
    }

    private static int colorWithAlpha(int color, float opacity) {
        int alpha = opacity <= 0.0F ? 0 : Math.max(4, Math.min(255, Math.round(opacity * 255.0F)));
        return alpha << 24 | color & 0xFFFFFF;
    }

    private record BossBarStyle(String entityTranslationKey, String subtitleTranslationKey, BossBarLayout layout,
                                int fixedFillV, int subtitleColor) {
    }

    private record BossBarLayout(ResourceLocation texture, int textureSize, int frameU, int frameV,
                                 int frameWidth, int frameHeight, int fillU, int leftFillWidth,
                                 int centerGapWidth, int rightFillWidth, int fillHeight, int fillXOffset,
                                 int fillYOffset) {
    }

    private static final class AnimatedBossBar {
        private long lastUpdateNanos;
        private float visibility;
        private BossBarStyle style;
        private Component name = Component.empty();
        private BossEvent.BossBarColor bossBarColor = BossEvent.BossBarColor.PINK;
        private float progress;
        private int frameY;
        private int labelY;
        private boolean showLabels;
        private boolean showSubtitle;
        private boolean disappearing;
        private float subtitleVisibility = 1.0F;
        private float visibleSeconds;

        private AnimatedBossBar(long now) {
            lastUpdateNanos = now;
        }

        private void capture(BossBarStyle style, Component name, BossEvent.BossBarColor bossBarColor, float progress) {
            this.style = style;
            this.name = name;
            this.bossBarColor = bossBarColor;
            this.progress = progress;
        }

        private void setLayout(int frameY, int labelY, boolean showLabels, boolean showSubtitle) {
            this.frameY = frameY;
            this.labelY = labelY;
            this.showLabels = showLabels;
            this.showSubtitle = showSubtitle;
        }

        private void update(long now, boolean visible) {
            float elapsedSeconds = Math.min(0.1F, Math.max(0.0F, (now - lastUpdateNanos) / 1_000_000_000.0F));
            lastUpdateNanos = now;
            boolean wasDisappearing = disappearing;
            disappearing = !visible;
            if (visible && wasDisappearing) {
                // Re-awakened before the fade-out finished: restart the
                // subtitle timer like a fresh bar.
                visibleSeconds = 0.0F;
                subtitleVisibility = 1.0F;
            }
            float speed = visible ? FADE_IN_PER_SECOND : FADE_OUT_PER_SECOND;
            visibility = Math.max(0.0F, Math.min(1.0F, visibility + (visible ? 1.0F : -1.0F) * elapsedSeconds * speed));
            if (visible) {
                visibleSeconds += elapsedSeconds;
                // The subtitle hides after the bar has been up for a while; a
                // fresh bar (e.g. boss re-awakened from dormancy) shows it again.
                if (visibleSeconds >= SUBTITLE_VISIBLE_SECONDS) {
                    subtitleVisibility = Math.max(0.0F,
                        subtitleVisibility - elapsedSeconds * SUBTITLE_FADE_OUT_PER_SECOND);
                }
            }
        }
    }
}
