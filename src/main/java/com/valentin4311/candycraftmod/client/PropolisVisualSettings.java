package com.valentin4311.candycraftmod.client;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class PropolisVisualSettings {
    private static final int DEFAULT_SIZE_PERCENT = 115;
    private static final Path SETTINGS_FILE = FMLPaths.CONFIGDIR.get().resolve("candycraft-client.properties");
    private static int sizePercent = loadSizePercent();

    private static final OptionInstance<Integer> SIZE = new OptionInstance<>(
        "options.candycraftmod.propolis_glint_size",
        OptionInstance.noTooltip(),
        (caption, value) -> Options.genericValueLabel(caption, Component.literal(value + "%")),
        new OptionInstance.IntRange(50, 200),
        sizePercent,
        PropolisVisualSettings::setSizePercent
    );

    private PropolisVisualSettings() {
    }

    public static float textureScale() {
        return 0.16F * 100.0F / sizePercent;
    }

    public static OptionInstance<Integer> option() {
        return SIZE;
    }

    private static void setSizePercent(int value) {
        sizePercent = Math.max(50, Math.min(200, value));
        Properties properties = new Properties();
        properties.setProperty("propolisGlintSizePercent", Integer.toString(sizePercent));
        try {
            Files.createDirectories(SETTINGS_FILE.getParent());
            try (OutputStream output = Files.newOutputStream(SETTINGS_FILE)) {
                properties.store(output, "CandyCraft client settings");
            }
        } catch (IOException ignored) {
            // The selected value remains active for this session if the config directory is read-only.
        }
    }

    private static int loadSizePercent() {
        Properties properties = new Properties();
        if (Files.isRegularFile(SETTINGS_FILE)) {
            try (InputStream input = Files.newInputStream(SETTINGS_FILE)) {
                properties.load(input);
                return Math.max(50, Math.min(200,
                    Integer.parseInt(properties.getProperty("propolisGlintSizePercent"))));
            } catch (IOException | NumberFormatException | NullPointerException ignored) {
                // Fall back to the stable default below.
            }
        }
        return DEFAULT_SIZE_PERCENT;
    }
}
