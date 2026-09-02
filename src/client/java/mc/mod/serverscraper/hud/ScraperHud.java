package mc.mod.serverscraper.hud;

import java.util.ArrayList;
import java.util.List;

import mc.mod.serverscraper.config.ScraperConfig;
import mc.mod.serverscraper.data.ServerInfo;
import mc.mod.serverscraper.scraper.MasterScraper;
import mc.mod.serverscraper.scraper.NetworkScraper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/**
 * Renders the in-game HUD overlay.
 *
 * Positions: TOP_LEFT | TOP_RIGHT | BOTTOM_LEFT | BOTTOM_RIGHT
 * Supports compact mode (single dense line per category) and full mode.
 * Background, scale, colour, and which sections show are all configurable.
 */
@Environment(EnvType.CLIENT)
public class ScraperHud {

    // ── Entry point called by HudRenderCallback ───────────────────────────────
    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        ScraperConfig cfg = ScraperConfig.get();
        if (!cfg.hudEnabled) return;
        if (!MasterScraper.isConnected()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null) return;
        // Don't draw over F3 debug screen
        if (mc.getDebugHud().shouldShowDebugHud()) return;
        // Don't draw while a screen is open (chat, inventory, etc.)
        if (mc.currentScreen != null) return;

        ServerInfo info = MasterScraper.INFO;
        List<HudLine> lines = buildLines(cfg, info);
        if (lines.isEmpty()) return;

        TextRenderer tr    = mc.textRenderer;
        int lineH          = tr.fontHeight + 1;
        int screenW        = context.getScaledWindowWidth();
        int screenH        = context.getScaledWindowHeight();

        float scale = cfg.hudScale;
        int offsetX = cfg.hudOffsetX;
        int offsetY = cfg.hudOffsetY;

        // Measure panel in unscaled units, then multiply by scale for screen size
        int maxWidth = 0;
        for (HudLine l : lines) {
            int w = tr.getWidth(l.text);
            if (w > maxWidth) maxWidth = w;
        }
        int panelW = maxWidth + 4;
        int panelH = lines.size() * lineH + 2;

        int scaledW = (int)(panelW * scale);
        int scaledH = (int)(panelH * scale);

        // Origin in actual screen pixels
        int ox, oy;
        switch (cfg.hudPosition.toUpperCase()) {
            case "TOP_RIGHT"    -> { ox = screenW - scaledW - offsetX; oy = offsetY; }
            case "BOTTOM_LEFT"  -> { ox = offsetX;                     oy = screenH - scaledH - offsetY; }
            case "BOTTOM_RIGHT" -> { ox = screenW - scaledW - offsetX; oy = screenH - scaledH - offsetY; }
            default             -> { ox = offsetX;                     oy = offsetY; }
        }

        // ── Background ────────────────────────────────────────────────────────
        if (cfg.hudBackground) {
            context.fill(ox - 1, oy - 1, ox + scaledW + 1, oy + scaledH + 1, cfg.hudBackgroundColor);
        }

        // ── Text — use org.joml.Matrix3x2fStack (what getMatrices() returns) ─
        // Matrix3x2fStack uses pushMatrix() / popMatrix() and scale(sx, sy)
        org.joml.Matrix3x2fStack mat = context.getMatrices();
        mat.pushMatrix();
        mat.translate(ox, oy);
        mat.scale(scale, scale);

        int y = 1;
        for (HudLine line : lines) {
            if (line.isSeparator) {
                y += 2;
                continue;
            }
            int color = line.color != 0 ? line.color : cfg.hudTextColor;
            context.drawText(tr, line.text, 2, y, color, cfg.hudBackground);
            y += lineH;
        }

        mat.popMatrix();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Line building
    // ─────────────────────────────────────────────────────────────────────────

    private static List<HudLine> buildLines(ScraperConfig cfg, ServerInfo info) {
        List<HudLine> lines = new ArrayList<>();
        boolean compact = cfg.hudCompact;

        if (cfg.hudShowConnection) {
            if (compact) {
                lines.add(line(
                    String.format("§b%s §7| §fping §a%dms §7| §f%s",
                        info.serverAddress, info.localPlayerPing, info.serverBrand),
                    0xFFFFFF));
            } else {
                lines.add(header("§b§lConnection"));
                lines.add(kv("Address",  info.serverAddress));
                lines.add(kv("IP",       info.resolvedIp + ":" + info.serverPort));
                lines.add(kv("Brand",    info.serverBrand));
                lines.add(kv("Ping",     info.localPlayerPing + "ms"));
                lines.add(kv("Version",  info.serverVersion));
                if (!info.detectedServerSoftware.isEmpty())
                    lines.add(kv("SW", String.join(", ", info.detectedServerSoftware)));
                lines.add(SEPARATOR);
            }
        }

        if (cfg.hudShowWorld) {
            if (compact) {
                String weather = info.isThundering ? "⛈" : info.isRaining ? "🌧" : "☀";
                lines.add(line(
                    String.format("§d%s §7| §f%s §7| §f%s §7| %s",
                        info.dimensionType, info.worldTimeFormatted,
                        "Day " + info.dayNumber, weather),
                    0xFFFFFF));
            } else {
                lines.add(header("§d§lWorld"));
                lines.add(kv("Dim",      info.dimensionType));
                lines.add(kv("Time",     info.worldTimeFormatted));
                lines.add(kv("Day",      String.valueOf(info.dayNumber)));
                lines.add(kv("Age",      info.worldAgeFormatted));
                lines.add(kv("Weather",  info.isThundering ? "§cThunder"
                                       : info.isRaining    ? "§9Rain"
                                       : "§aClear"));
                lines.add(SEPARATOR);
            }
        }

        if (cfg.hudShowPlayer) {
            if (compact) {
                lines.add(line(
                    String.format("§aXYZ §f%.1f §7/ §f%.1f §7/ §f%.1f  §7[%s]",
                        info.localPlayerX, info.localPlayerY, info.localPlayerZ,
                        info.localPlayerGamemode),
                    0xFFFFFF));
                lines.add(line(
                    String.format("§c❤ §f%.1f  §6🍖 §f%.0f  §eXP §f%d (%d%%)",
                        info.localPlayerHealth, info.localPlayerFood,
                        info.localPlayerLevel, (int)(info.localPlayerXp * 100)),
                    0xFFFFFF));
            } else {
                lines.add(header("§a§lPlayer"));
                lines.add(kv("Name",     info.localPlayerName));
                lines.add(kv("Mode",     info.localPlayerGamemode));
                lines.add(kv("Health",   String.format("§c%.1f §7/ 20", info.localPlayerHealth)));
                lines.add(kv("Food",     String.format("§6%.0f §7/ 20", info.localPlayerFood)));
                lines.add(kv("Armor",    String.format("§7%.0f", info.localPlayerArmor)));
                lines.add(kv("XP",       info.localPlayerLevel + " §7(" +
                                         (int)(info.localPlayerXp*100) + "%)"));
                lines.add(kv("XYZ",      String.format("§f%.1f §7/ §f%.1f §7/ §f%.1f",
                                         info.localPlayerX, info.localPlayerY, info.localPlayerZ)));
                lines.add(kv("Chunk",    info.localPlayerChunkX + " / " + info.localPlayerChunkZ));
                lines.add(kv("Facing",   String.format("%.0f° / %.0f°",
                                         info.localPlayerYaw, info.localPlayerPitch)));
                lines.add(kv("Speed",    String.format("%.3f b/t", info.localPlayerSpeed)));
                lines.add(kv("Flying",   info.localPlayerFlying  ? "§ayes" : "§7no"));
                lines.add(kv("On gnd",   info.localPlayerOnGround ? "§ayes" : "§7no"));
                lines.add(SEPARATOR);
            }
        }

        if (cfg.hudShowPlayers) {
            if (compact) {
                lines.add(line(
                    String.format("§e👥 §f%d players online", info.playerCount),
                    0xFFFFFF));
            } else {
                lines.add(header("§e§lPlayers §7(" + info.playerCount + ")"));
                int shown = 0;
                for (ServerInfo.PlayerEntry p : info.onlinePlayers) {
                    if (shown++ >= 8) { lines.add(dim("  …and " +
                        (info.onlinePlayers.size() - 8) + " more")); break; }
                    lines.add(dim("  §f" + p.name +
                        " §7[" + p.latencyMs + "ms]"));
                }
                lines.add(SEPARATOR);
            }
        }

        if (cfg.hudShowEntities) {
            if (compact) {
                lines.add(line(
                    String.format("§cEntities §f%d  §cHostile §f%d  §aPassive §f%d",
                        info.totalEntityCount, info.hostileEntityCount, info.passiveEntityCount),
                    0xFFFFFF));
            } else {
                lines.add(header("§c§lEntities"));
                lines.add(kv("Total",   String.valueOf(info.totalEntityCount)));
                lines.add(kv("Players", String.valueOf(info.playerEntityCount)));
                lines.add(kv("Hostile", "§c" + info.hostileEntityCount));
                lines.add(kv("Passive", "§a" + info.passiveEntityCount));
                lines.add(kv("Items",   String.valueOf(info.itemEntityCount)));
                lines.add(SEPARATOR);
            }
        }

        if (cfg.hudShowPerf) {
            int tpsColor = info.tps >= 19.0 ? 0x55FF55
                         : info.tps >= 15.0 ? 0xFFFF55
                         : 0xFF5555;
            if (compact) {
                lines.add(new HudLine(
                    String.format("TPS %.1f  MSPT %.0fms  FPS %d  RAM %dMB",
                        info.tps, info.mspt, info.clientFps, info.usedMemoryMb),
                    tpsColor, false));
            } else {
                lines.add(header("§6§lPerformance"));
                lines.add(new HudLine(
                    String.format("  TPS: §f%.2f §7 MSPT: §f%.1fms",
                        info.tps, info.mspt),
                    tpsColor, false));
                lines.add(kv("FPS",    String.valueOf(info.clientFps)));
                lines.add(kv("RAM",    info.usedMemoryMb + " / " + info.maxMemoryMb + " MB"));
                lines.add(kv("Chunks", String.valueOf(info.loadedChunkCount)));
                lines.add(kv("Net ↑",  NetworkScraper.formatBytes(info.networkSentBytes)));
                lines.add(kv("Net ↓",  NetworkScraper.formatBytes(info.networkRecvBytes)));
                lines.add(SEPARATOR);
            }
        }

        if (cfg.hudShowScoreboard && !info.scoreboardLines.isEmpty()) {
            lines.add(header("§f§l" + info.scoreboardTitle));
            for (String l : info.scoreboardLines) {
                lines.add(dim("  " + l));
            }
            lines.add(SEPARATOR);
        }

        // Remove trailing separator
        while (!lines.isEmpty() && lines.get(lines.size() - 1).isSeparator) {
            lines.remove(lines.size() - 1);
        }

        return lines;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HudLine helpers
    // ─────────────────────────────────────────────────────────────────────────

    private static final HudLine SEPARATOR = new HudLine("", 0, true);

    private record HudLine(String text, int color, boolean isSeparator) {}

    private static HudLine line(String text, int color) {
        return new HudLine(text, color, false);
    }

    private static HudLine header(String text) {
        return new HudLine(text, 0xAAAAAA, false);
    }

    private static HudLine kv(String key, String value) {
        return new HudLine("§7" + key + ": §f" + value, 0xFFFFFF, false);
    }

    private static HudLine dim(String text) {
        return new HudLine(text, 0xAAAAAA, false);
    }
}
