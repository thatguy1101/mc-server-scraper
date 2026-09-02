package mc.mod.serverscraper.scraper;

import mc.mod.serverscraper.data.ServerInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

/**
 * Scrapes client-side performance metrics: FPS, memory, and estimates TPS/MSPT
 * from the world-time packet cadence tracked by the TpsTracker.
 */
@Environment(EnvType.CLIENT)
public class PerformanceScraper {

    public static void scrape(ServerInfo info) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;

        // ── FPS ───────────────────────────────────────────────────────────────
        info.clientFps = MinecraftClient.getInstance().getCurrentFps();

        // ── Memory ────────────────────────────────────────────────────────────
        Runtime rt          = Runtime.getRuntime();
        info.maxMemoryMb    = rt.maxMemory()   / (1024 * 1024);
        info.freeMemoryMb   = rt.freeMemory()  / (1024 * 1024);
        info.usedMemoryMb   = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);

        // TPS / MSPT are updated externally by TpsTracker; we just read them here
        // (no-op — values already set on info by TpsTracker)
    }
}
