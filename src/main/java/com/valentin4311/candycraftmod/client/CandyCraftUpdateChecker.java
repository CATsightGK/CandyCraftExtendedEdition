package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;

public final class CandyCraftUpdateChecker {
    private static final URI CURSEFORGE_FILES = URI.create("https://api.curse.tools/v1/cf/mods/1620428/files");
    private static final Pattern VERSION_PATTERN = Pattern.compile("(?i)candy\\s*craft(?:ee|\\s*extended\\s*edition)?[^0-9]{0,24}([0-9]+(?:\\.[0-9]+){1,3})");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicBoolean CHECK_IN_PROGRESS = new AtomicBoolean(false);
    private static final HttpClient CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(4))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();
    private static String pendingVersion;
    private static String notifiedVersion;
    private static boolean wasInWorld;

    private CandyCraftUpdateChecker() {
    }

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            wasInWorld = false;
            return;
        }

        if (!wasInWorld) {
            wasInWorld = true;
            if (CHECK_IN_PROGRESS.compareAndSet(false, true)) {
                checkForUpdates(minecraft);
            }
        }
        showPendingUpdate(minecraft);
    }

    private static void checkForUpdates(Minecraft minecraft) {
        HttpRequest request = HttpRequest.newBuilder(CURSEFORGE_FILES)
            .timeout(Duration.ofSeconds(8))
            .header("User-Agent", "CandyCraft/" + currentVersion() + " Minecraft update checker")
            .GET()
            .build();

        CompletableFuture.supplyAsync(() -> fetchLatestVersion(request)).whenComplete((latest, error) ->
            minecraft.execute(() -> {
                CHECK_IN_PROGRESS.set(false);
                if (error != null) {
                    LOGGER.warn("CandyCraft update check failed", error);
                    return;
                }
                latest.ifPresent(version -> {
                    String current = currentVersion();
                    LOGGER.debug("CandyCraft update check: current={}, latest={}", current, version);
                    if (isNewer(version, current) && !version.equals(notifiedVersion)) {
                        pendingVersion = version;
                        showPendingUpdate(minecraft);
                    }
                });
            })
        );
    }

    private static void showPendingUpdate(Minecraft minecraft) {
        if (pendingVersion == null || minecraft.player == null) {
            return;
        }
        if (!isNewer(pendingVersion, currentVersion())) {
            pendingVersion = null;
            return;
        }
        minecraft.player.displayClientMessage(
            Component.translatable("chat.candycraftmod.update_available", pendingVersion),
            false
        );
        notifiedVersion = pendingVersion;
        pendingVersion = null;
    }

    private static Optional<String> fetchLatestVersion(HttpRequest request) {
        try {
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                LOGGER.warn("CandyCraft update check returned HTTP {}", response.statusCode());
                return Optional.empty();
            }
            return latestVersionIn(response.body());
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.warn("CandyCraft update check request failed", exception);
            return Optional.empty();
        } catch (RuntimeException exception) {
            LOGGER.warn("CandyCraft update response could not be parsed", exception);
            return Optional.empty();
        }
    }

    private static Optional<String> latestVersionIn(String page) {
        Matcher matcher = VERSION_PATTERN.matcher(page);
        return matcher.results()
            .map(result -> result.group(1))
            .max(Comparator.comparing(CandyCraftUpdateChecker::versionParts, CandyCraftUpdateChecker::compareParts));
    }

    private static String currentVersion() {
        return ModList.get()
            .getModContainerById(CandyCraft.MODID)
            .map(container -> container.getModInfo().getVersion().toString())
            .orElse("0.0.0");
    }

    private static boolean isNewer(String candidate, String current) {
        return compareParts(versionParts(candidate), versionParts(current)) > 0;
    }

    private static int[] versionParts(String version) {
        String[] raw = version.toLowerCase(Locale.ROOT).replaceAll("[^0-9.]", "").split("\\.");
        int[] parts = new int[Math.max(4, raw.length)];
        for (int i = 0; i < raw.length; i++) {
            try {
                parts[i] = raw[i].isEmpty() ? 0 : Integer.parseInt(raw[i]);
            } catch (NumberFormatException ignored) {
                parts[i] = 0;
            }
        }
        return parts;
    }

    private static int compareParts(int[] left, int[] right) {
        int length = Math.max(left.length, right.length);
        for (int i = 0; i < length; i++) {
            int a = i < left.length ? left[i] : 0;
            int b = i < right.length ? right[i] : 0;
            if (a != b) {
                return Integer.compare(a, b);
            }
        }
        return 0;
    }
}
