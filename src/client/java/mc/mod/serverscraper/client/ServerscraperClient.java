package mc.mod.serverscraper.client;

import mc.mod.serverscraper.command.ScraperCommand;
import mc.mod.serverscraper.config.ScraperConfig;
import mc.mod.serverscraper.export.DataExporter;
import mc.mod.serverscraper.hud.ScraperHud;
import mc.mod.serverscraper.scraper.MasterScraper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client entry point — registers all Fabric API events and hooks.
 */
@Environment(EnvType.CLIENT)
public class ServerscraperClient implements ClientModInitializer {

    public static final String MOD_ID = "serverscraper";
    public static final Logger LOGGER  = LoggerFactory.getLogger("ServerScraper");

    @Override
    public void onInitializeClient() {
        LOGGER.info("ServerScraper initialising…");

        // Load config early so all systems pick up saved values
        ScraperConfig.get();

        // ── Commands ──────────────────────────────────────────────────────────
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            ScraperCommand.register(dispatcher)
        );

        // ── Join / leave lifecycle ────────────────────────────────────────────
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            // MasterScraper.onConnect() is called by MixinGameJoinS2CPacket once the
            // GameJoin packet arrives (after world is ready). Here we just do early setup.
            LOGGER.info("ServerScraper: joined server.");

            if (ScraperConfig.get().autoExportOnJoin) {
                // Delay one second to let the initial scrape complete
                scheduleExport(client, 20);
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            // Auto-export on leave (also handled by disconnect mixin as fallback)
            ScraperConfig cfg = ScraperConfig.get();
            if (cfg.autoExportOnLeave && MasterScraper.isConnected()) {
                try {
                    DataExporter.export(MasterScraper.INFO, cfg.exportFormat);
                } catch (Exception e) {
                    LOGGER.warn("Auto-export on leave failed: {}", e.getMessage());
                }
            }
            MasterScraper.onDisconnect();
        });

        // ── Client tick ───────────────────────────────────────────────────────
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            MasterScraper.tick();
            checkAlerts(client);
        });

        // ── HUD rendering ─────────────────────────────────────────────────────
        HudRenderCallback.EVENT.register(ScraperHud::render);

        LOGGER.info("ServerScraper ready. Use /scraper or /ss in-game.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Schedule a one-shot export after {@code delayTicks} ticks. */
    private static void scheduleExport(MinecraftClient client, int delayTicks) {
        final int[] countdown = {delayTicks};
        ClientTickEvents.END_CLIENT_TICK.register(new ClientTickEvents.EndTick() {
            @Override
            public void onEndTick(MinecraftClient c) {
                if (--countdown[0] <= 0) {
                    try {
                        MasterScraper.refresh();
                        DataExporter.export(MasterScraper.INFO,
                                ScraperConfig.get().exportFormat);
                    } catch (Exception e) {
                        LOGGER.warn("Scheduled auto-export failed: {}", e.getMessage());
                    }
                    // Unregister by not re-subscribing — Fabric lets us do this
                    // by replacing with a no-op; we use a flag instead
                    countdown[0] = Integer.MIN_VALUE; // prevent re-fire
                }
            }
        });
    }

    // ── Alert state ───────────────────────────────────────────────────────────
    private static int    prevPlayerCount  = 0;
    private static boolean tpsAlertActive = false;
    private static int     alertCheckDelay = 0;

    private static void checkAlerts(MinecraftClient client) {
        if (client.player == null || !MasterScraper.isConnected()) return;

        alertCheckDelay++;
        // Check alerts every 40 ticks (2 s) to avoid spam
        if (alertCheckDelay < 40) return;
        alertCheckDelay = 0;

        ScraperConfig cfg = ScraperConfig.get();
        mc.mod.serverscraper.data.ServerInfo info = MasterScraper.INFO;

        // ── Player join / leave alerts ────────────────────────────────────────
        int current = info.playerCount;
        if (current != prevPlayerCount) {
            int diff = current - prevPlayerCount;
            if (diff > 0 && cfg.alertOnPlayerJoin) {
                sendOverlay(client, "§a+" + diff + " player" + (diff > 1 ? "s" : "") + " joined");
            } else if (diff < 0 && cfg.alertOnPlayerLeave) {
                sendOverlay(client, "§c" + Math.abs(diff) + " player" +
                        (Math.abs(diff) > 1 ? "s" : "") + " left");
            }
            prevPlayerCount = current;
        }

        // ── Low TPS alert ─────────────────────────────────────────────────────
        if (cfg.alertLowTps) {
            if (info.tps < cfg.alertLowTpsThreshold && !tpsAlertActive) {
                sendOverlay(client, String.format("§c[ServerScraper] Low TPS: %.1f", info.tps));
                tpsAlertActive = true;
            } else if (info.tps >= cfg.alertLowTpsThreshold) {
                tpsAlertActive = false;
            }
        }
    }

    private static void sendOverlay(MinecraftClient client, String message) {
        if (client.player != null) {
            client.player.sendMessage(
                net.minecraft.text.Text.literal(message), false);
        }
    }
}
