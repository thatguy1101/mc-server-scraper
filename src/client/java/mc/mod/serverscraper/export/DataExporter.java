package mc.mod.serverscraper.export;

import com.google.gson.*;
import mc.mod.serverscraper.config.ScraperConfig;
import mc.mod.serverscraper.data.ServerInfo;
import mc.mod.serverscraper.scraper.NetworkScraper;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Exports all scraped data to TXT, JSON, or CSV format.
 *
 * Output directory is resolved relative to the game directory
 * (i.e. .minecraft/scraper_exports/ by default).
 *
 * Returns the absolute path of the file written so the command can show it
 * as a clickable link.
 */
public class DataExporter {

    private static final Logger LOGGER = LoggerFactory.getLogger("ServerScraper/Exporter");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public static String export(ServerInfo info, String format) throws IOException {
        String dir   = ScraperConfig.get().exportDirectory;
        Path   outDir = FabricLoader.getInstance().getGameDir().resolve(dir);
        Files.createDirectories(outDir);

        String ts       = LocalDateTime.now().format(TS);
        String safeName = info.serverAddress.replaceAll("[^a-zA-Z0-9._-]", "_");
        String ext      = format.equalsIgnoreCase("JSON") ? "json"
                        : format.equalsIgnoreCase("CSV")  ? "csv"
                        : "txt";
        Path file = outDir.resolve(safeName + "_" + ts + "." + ext);

        switch (format.toUpperCase()) {
            case "JSON" -> writeJson(info, file);
            case "CSV"  -> writeCsv(info, file);
            default     -> writeTxt(info, file);
        }

        LOGGER.info("Exported scrape data to {}", file.toAbsolutePath());
        return file.toAbsolutePath().toString();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TXT
    // ─────────────────────────────────────────────────────────────────────────

    private static void writeTxt(ServerInfo info, Path file) throws IOException {
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(file))) {
            w.println("========================================");
            w.println("  ServerScraper Export");
            w.println("  Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            w.println("========================================");
            w.println();

            section(w, "CONNECTION");
            kv(w, "Address",            info.serverAddress);
            kv(w, "Resolved IP",        info.resolvedIp + ":" + info.serverPort);
            kv(w, "Brand",              info.serverBrand);
            kv(w, "Version",            info.serverVersion);
            kv(w, "Protocol",           String.valueOf(info.protocolVersion));
            kv(w, "Ping",               info.pingMs + " ms");
            kv(w, "Online mode",        String.valueOf(info.isOnlineMode));
            kv(w, "Singleplayer",       String.valueOf(info.isSinglePlayer));
            kv(w, "LAN",                String.valueOf(info.isLanServer));
            kv(w, "Detected software",  String.join(", ", info.detectedServerSoftware));
            kv(w, "Join time",          info.joinTime.toString());
            kv(w, "Last refreshed",     info.lastRefreshed.toString());
            w.println();

            section(w, "WORLD");
            kv(w, "Dimension",          info.dimensionType + " (" + info.dimensionId + ")");
            kv(w, "Time of day",        info.worldTimeFormatted);
            kv(w, "Day number",         String.valueOf(info.dayNumber));
            kv(w, "World age",          info.worldAgeFormatted);
            kv(w, "Weather",            info.isThundering ? "Thunder" : info.isRaining ? "Rain" : "Clear");
            kv(w, "Rain level",         String.format("%.3f", info.rainLevel));
            kv(w, "Thunder level",      String.format("%.3f", info.thunderLevel));
            kv(w, "Superflat",          String.valueOf(info.isSuperFlat));
            kv(w, "Debug world",        String.valueOf(info.isDebugWorld));
            kv(w, "Fixed time",         String.valueOf(info.hasFixedTime));
            kv(w, "Has sky light",      String.valueOf(info.hasSkyLight));
            kv(w, "Has ceiling",        String.valueOf(info.hasCeiling));
            kv(w, "Ultrawarm",          String.valueOf(info.isUltrawarm));
            kv(w, "Natural",            String.valueOf(info.isNatural));
            kv(w, "Min Y",              String.valueOf(info.minY));
            kv(w, "Height",             String.valueOf(info.height));
            kv(w, "Logical height",     String.valueOf(info.logicalHeight));
            kv(w, "Ambient light",      String.valueOf(info.ambientLight));
            kv(w, "Effects",            info.effectsId);
            kv(w, "Infiniburn",         info.infiniburnTag);
            w.println();

            section(w, "LOCAL PLAYER");
            kv(w, "Name",               info.localPlayerName);
            kv(w, "UUID",               info.localPlayerUuidStr);
            kv(w, "Gamemode",           info.localPlayerGamemode);
            kv(w, "Ping",               info.localPlayerPing + " ms");
            kv(w, "Health",             String.format("%.1f / 20", info.localPlayerHealth));
            kv(w, "Food",               String.format("%.0f / 20", info.localPlayerFood));
            kv(w, "Saturation",         String.format("%.2f", info.localPlayerSaturation));
            kv(w, "Armor",              String.format("%.0f", info.localPlayerArmor));
            kv(w, "XP level",           String.valueOf(info.localPlayerLevel));
            kv(w, "XP progress",        String.format("%.1f%%", info.localPlayerXp * 100));
            kv(w, "Total XP",           String.valueOf(info.localPlayerTotalXp));
            kv(w, "X",                  String.format("%.4f", info.localPlayerX));
            kv(w, "Y",                  String.format("%.4f", info.localPlayerY));
            kv(w, "Z",                  String.format("%.4f", info.localPlayerZ));
            kv(w, "Yaw",                String.format("%.2f°", info.localPlayerYaw));
            kv(w, "Pitch",              String.format("%.2f°", info.localPlayerPitch));
            kv(w, "Chunk X/Z",         info.localPlayerChunkX + " / " + info.localPlayerChunkZ);
            kv(w, "Region file",        "r." + info.localPlayerRegionX + "." + info.localPlayerRegionZ + ".mca");
            kv(w, "Speed",              String.format("%.4f b/t", info.localPlayerSpeed));
            kv(w, "Flying",             String.valueOf(info.localPlayerFlying));
            kv(w, "Sneaking",           String.valueOf(info.localPlayerSneaking));
            kv(w, "Sprinting",          String.valueOf(info.localPlayerSprinting));
            kv(w, "On ground",          String.valueOf(info.localPlayerOnGround));
            kv(w, "In water",           String.valueOf(info.localPlayerInWater));
            kv(w, "In lava",            String.valueOf(info.localPlayerInLava));
            kv(w, "Op",                 String.valueOf(info.localPlayerOp));
            kv(w, "Permission level",   String.valueOf(info.localPlayerPermLevel));
            kv(w, "Team",               info.localPlayerTeam);
            w.println();

            section(w, "ONLINE PLAYERS (" + info.playerCount + ")");
            if (info.onlinePlayers.isEmpty()) {
                w.println("  (none)");
            } else {
                w.printf("  %-20s  %-36s  %-9s  %s%n",
                    "Name", "UUID", "Gamemode", "Ping");
                w.println("  " + "-".repeat(80));
                for (ServerInfo.PlayerEntry p : info.onlinePlayers) {
                    w.printf("  %-20s  %-36s  %-9s  %dms%n",
                        p.name,
                        p.uuid != null ? p.uuid.toString() : "N/A",
                        p.gamemode,
                        p.latencyMs);
                }
            }
            w.println();

            section(w, "ENTITIES");
            kv(w, "Total",   String.valueOf(info.totalEntityCount));
            kv(w, "Players", String.valueOf(info.playerEntityCount));
            kv(w, "Hostile", String.valueOf(info.hostileEntityCount));
            kv(w, "Passive", String.valueOf(info.passiveEntityCount));
            kv(w, "Items",   String.valueOf(info.itemEntityCount));
            if (!info.entityTypeCounts.isEmpty()) {
                w.println("  --- By type ---");
                for (Map.Entry<String, Integer> e : info.entityTypeCounts.entrySet()) {
                    w.printf("  %-50s %d%n", e.getKey(), e.getValue());
                }
            }
            w.println();

            section(w, "CHUNKS");
            kv(w, "Loaded",             String.valueOf(info.loadedChunkCount));
            kv(w, "Client render dist", info.chunkRenderDistance + " chunks");
            kv(w, "Server render dist", info.serverRenderDistance + " chunks");
            kv(w, "Simulation dist",    info.simulationDistance + " chunks");
            w.println();

            section(w, "PERFORMANCE");
            kv(w, "TPS",        String.format("%.2f", info.tps));
            kv(w, "MSPT",       String.format("%.2f ms", info.mspt));
            kv(w, "Client FPS", String.valueOf(info.clientFps));
            kv(w, "RAM used",   info.usedMemoryMb + " MB");
            kv(w, "RAM max",    info.maxMemoryMb + " MB");
            kv(w, "RAM free",   info.freeMemoryMb + " MB");
            w.println();

            section(w, "NETWORK");
            kv(w, "Sent packets",  String.valueOf(info.networkSentPackets));
            kv(w, "Recv packets",  String.valueOf(info.networkRecvPackets));
            kv(w, "Sent bytes",    NetworkScraper.formatBytes(info.networkSentBytes));
            kv(w, "Recv bytes",    NetworkScraper.formatBytes(info.networkRecvBytes));
            w.println();

            section(w, "PLUGIN CHANNELS (" + info.pluginChannels.size() + ")");
            for (String ch : info.pluginChannels) w.println("  " + ch);
            w.println();

            section(w, "RESOURCE PACKS (" + info.serverResourcePacks.size() + ")");
            for (String rp : info.serverResourcePacks) w.println("  " + rp);
            w.println();

            section(w, "GAMERULES (" + info.gamerules.size() + ")");
            for (Map.Entry<String, String> e : info.gamerules.entrySet()) {
                w.printf("  %-40s %s%n", e.getKey(), e.getValue());
            }
            w.println();

            section(w, "SCOREBOARD");
            kv(w, "Title", info.scoreboardTitle);
            for (String l : info.scoreboardLines) w.println("  " + l);
            w.println();

            section(w, "BOSS BARS (" + info.bossBars.size() + ")");
            for (ServerInfo.BossBarEntry b : info.bossBars) {
                w.printf("  [%s]  %.0f%%  color=%s  style=%s%n",
                    b.name, b.progress * 100, b.color, b.overlay);
            }
            w.println();

            section(w, "LAST MESSAGES");
            kv(w, "Title",      info.lastTitle);
            kv(w, "Subtitle",   info.lastSubtitle);
            kv(w, "Action bar", info.lastActionBar);
            w.println();

            w.println("========================================");
            w.println("  End of export");
            w.println("========================================");
        }
    }

    private static void section(PrintWriter w, String title) {
        w.println("--- " + title + " ---");
    }

    private static void kv(PrintWriter w, String key, String value) {
        w.printf("  %-24s %s%n", key + ":", value);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // JSON
    // ─────────────────────────────────────────────────────────────────────────

    private static void writeJson(ServerInfo info, Path file) throws IOException {
        JsonObject root = new JsonObject();

        // Connection
        JsonObject conn = new JsonObject();
        conn.addProperty("address",           info.serverAddress);
        conn.addProperty("resolvedIp",        info.resolvedIp);
        conn.addProperty("port",              info.serverPort);
        conn.addProperty("brand",             info.serverBrand);
        conn.addProperty("version",           info.serverVersion);
        conn.addProperty("protocol",          info.protocolVersion);
        conn.addProperty("ping",              info.pingMs);
        conn.addProperty("onlineMode",        info.isOnlineMode);
        conn.addProperty("singleplayer",      info.isSinglePlayer);
        conn.addProperty("lan",               info.isLanServer);
        conn.addProperty("joinTime",          info.joinTime.toString());
        conn.addProperty("lastRefreshed",     info.lastRefreshed.toString());
        JsonArray swArr = new JsonArray();
        info.detectedServerSoftware.forEach(swArr::add);
        conn.add("detectedSoftware", swArr);
        root.add("connection", conn);

        // World
        JsonObject world = new JsonObject();
        world.addProperty("dimensionId",      info.dimensionId);
        world.addProperty("dimensionType",    info.dimensionType);
        world.addProperty("worldAgeRaw",      info.worldAgeRaw);
        world.addProperty("worldTimeRaw",     info.worldTimeRaw);
        world.addProperty("worldTimeFormatted", info.worldTimeFormatted);
        world.addProperty("worldAgeFormatted", info.worldAgeFormatted);
        world.addProperty("dayNumber",        info.dayNumber);
        world.addProperty("isDaytime",        info.isDaytime);
        world.addProperty("isRaining",        info.isRaining);
        world.addProperty("isThundering",     info.isThundering);
        world.addProperty("isSuperFlat",      info.isSuperFlat);
        world.addProperty("isDebugWorld",     info.isDebugWorld);
        world.addProperty("hasFixedTime",     info.hasFixedTime);
        world.addProperty("hasSkyLight",      info.hasSkyLight);
        world.addProperty("hasCeiling",       info.hasCeiling);
        world.addProperty("isUltrawarm",      info.isUltrawarm);
        world.addProperty("isNatural",        info.isNatural);
        world.addProperty("ambientLight",     info.ambientLight);
        world.addProperty("minY",             info.minY);
        world.addProperty("height",           info.height);
        world.addProperty("logicalHeight",    info.logicalHeight);
        world.addProperty("effectsId",        info.effectsId);
        world.addProperty("infiniburnTag",    info.infiniburnTag);
        root.add("world", world);

        // Gamerules
        JsonObject gr = new JsonObject();
        info.gamerules.forEach(gr::addProperty);
        root.add("gamerules", gr);

        // Player
        JsonObject player = new JsonObject();
        player.addProperty("name",            info.localPlayerName);
        player.addProperty("uuid",            info.localPlayerUuidStr);
        player.addProperty("gamemode",        info.localPlayerGamemode);
        player.addProperty("ping",            info.localPlayerPing);
        player.addProperty("health",          info.localPlayerHealth);
        player.addProperty("food",            info.localPlayerFood);
        player.addProperty("saturation",      info.localPlayerSaturation);
        player.addProperty("armor",           info.localPlayerArmor);
        player.addProperty("xpLevel",         info.localPlayerLevel);
        player.addProperty("xpProgress",      info.localPlayerXp);
        player.addProperty("totalXp",         info.localPlayerTotalXp);
        player.addProperty("x",               info.localPlayerX);
        player.addProperty("y",               info.localPlayerY);
        player.addProperty("z",               info.localPlayerZ);
        player.addProperty("yaw",             info.localPlayerYaw);
        player.addProperty("pitch",           info.localPlayerPitch);
        player.addProperty("chunkX",          info.localPlayerChunkX);
        player.addProperty("chunkZ",          info.localPlayerChunkZ);
        player.addProperty("regionX",         info.localPlayerRegionX);
        player.addProperty("regionZ",         info.localPlayerRegionZ);
        player.addProperty("speed",           info.localPlayerSpeed);
        player.addProperty("flying",          info.localPlayerFlying);
        player.addProperty("sneaking",        info.localPlayerSneaking);
        player.addProperty("sprinting",       info.localPlayerSprinting);
        player.addProperty("onGround",        info.localPlayerOnGround);
        player.addProperty("inWater",         info.localPlayerInWater);
        player.addProperty("inLava",          info.localPlayerInLava);
        player.addProperty("isOp",            info.localPlayerOp);
        player.addProperty("permLevel",       info.localPlayerPermLevel);
        player.addProperty("team",            info.localPlayerTeam);
        root.add("localPlayer", player);

        // Online players
        JsonArray playersArr = new JsonArray();
        for (ServerInfo.PlayerEntry p : info.onlinePlayers) {
            JsonObject po = new JsonObject();
            po.addProperty("name",     p.name);
            po.addProperty("uuid",     p.uuid != null ? p.uuid.toString() : "");
            po.addProperty("gamemode", p.gamemode);
            po.addProperty("ping",     p.latencyMs);
            po.addProperty("team",     p.team);
            playersArr.add(po);
        }
        root.add("onlinePlayers", playersArr);

        // Entities
        JsonObject ents = new JsonObject();
        ents.addProperty("total",    info.totalEntityCount);
        ents.addProperty("players",  info.playerEntityCount);
        ents.addProperty("hostile",  info.hostileEntityCount);
        ents.addProperty("passive",  info.passiveEntityCount);
        ents.addProperty("items",    info.itemEntityCount);
        JsonObject typeCounts = new JsonObject();
        info.entityTypeCounts.forEach(typeCounts::addProperty);
        ents.add("byType", typeCounts);
        root.add("entities", ents);

        // Chunks + Perf
        JsonObject perf = new JsonObject();
        perf.addProperty("tps",                 info.tps);
        perf.addProperty("mspt",                info.mspt);
        perf.addProperty("clientFps",           info.clientFps);
        perf.addProperty("usedMemoryMb",        info.usedMemoryMb);
        perf.addProperty("maxMemoryMb",         info.maxMemoryMb);
        perf.addProperty("loadedChunks",        info.loadedChunkCount);
        perf.addProperty("clientRenderDist",    info.chunkRenderDistance);
        perf.addProperty("serverRenderDist",    info.serverRenderDistance);
        perf.addProperty("simulationDist",      info.simulationDistance);
        perf.addProperty("sentPackets",         info.networkSentPackets);
        perf.addProperty("recvPackets",         info.networkRecvPackets);
        perf.addProperty("sentBytes",           info.networkSentBytes);
        perf.addProperty("recvBytes",           info.networkRecvBytes);
        root.add("performance", perf);

        // Plugin channels
        JsonArray chArr = new JsonArray();
        info.pluginChannels.forEach(chArr::add);
        root.add("pluginChannels", chArr);

        // Resource packs
        JsonArray rpArr = new JsonArray();
        info.serverResourcePacks.forEach(rpArr::add);
        root.add("resourcePacks", rpArr);

        // Boss bars
        JsonArray bbArr = new JsonArray();
        for (ServerInfo.BossBarEntry b : info.bossBars) {
            JsonObject bo = new JsonObject();
            bo.addProperty("name",     b.name);
            bo.addProperty("progress", b.progress);
            bo.addProperty("color",    b.color);
            bo.addProperty("overlay",  b.overlay);
            bbArr.add(bo);
        }
        root.add("bossBars", bbArr);

        // Scoreboard
        JsonObject sb = new JsonObject();
        sb.addProperty("title", info.scoreboardTitle);
        JsonArray sbLines = new JsonArray();
        info.scoreboardLines.forEach(sbLines::add);
        sb.add("lines", sbLines);
        root.add("scoreboard", sb);

        // Last messages
        JsonObject msgs = new JsonObject();
        msgs.addProperty("title",     info.lastTitle);
        msgs.addProperty("subtitle",  info.lastSubtitle);
        msgs.addProperty("actionBar", info.lastActionBar);
        root.add("lastMessages", msgs);

        try (Writer w = Files.newBufferedWriter(file)) {
            GSON.toJson(root, w);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CSV  (flat key=value rows — most spreadsheet-friendly format)
    // ─────────────────────────────────────────────────────────────────────────

    private static void writeCsv(ServerInfo info, Path file) throws IOException {
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(file))) {
            w.println("category,key,value");
            csv(w, "connection",  "address",          info.serverAddress);
            csv(w, "connection",  "resolvedIp",        info.resolvedIp);
            csv(w, "connection",  "port",              String.valueOf(info.serverPort));
            csv(w, "connection",  "brand",             info.serverBrand);
            csv(w, "connection",  "version",           info.serverVersion);
            csv(w, "connection",  "protocol",          String.valueOf(info.protocolVersion));
            csv(w, "connection",  "ping",              String.valueOf(info.pingMs));
            csv(w, "connection",  "onlineMode",        String.valueOf(info.isOnlineMode));
            csv(w, "connection",  "singleplayer",      String.valueOf(info.isSinglePlayer));
            csv(w, "connection",  "detectedSoftware",  String.join("|", info.detectedServerSoftware));
            csv(w, "world",       "dimensionId",       info.dimensionId);
            csv(w, "world",       "dimensionType",     info.dimensionType);
            csv(w, "world",       "timeFormatted",     info.worldTimeFormatted);
            csv(w, "world",       "dayNumber",         String.valueOf(info.dayNumber));
            csv(w, "world",       "ageFormatted",      info.worldAgeFormatted);
            csv(w, "world",       "weather",           info.isThundering ? "thunder" : info.isRaining ? "rain" : "clear");
            csv(w, "world",       "superflat",         String.valueOf(info.isSuperFlat));
            csv(w, "world",       "hasSkyLight",       String.valueOf(info.hasSkyLight));
            csv(w, "world",       "minY",              String.valueOf(info.minY));
            csv(w, "world",       "height",            String.valueOf(info.height));
            csv(w, "player",      "name",              info.localPlayerName);
            csv(w, "player",      "uuid",              info.localPlayerUuidStr);
            csv(w, "player",      "gamemode",          info.localPlayerGamemode);
            csv(w, "player",      "ping",              String.valueOf(info.localPlayerPing));
            csv(w, "player",      "health",            String.format("%.2f", info.localPlayerHealth));
            csv(w, "player",      "food",              String.format("%.0f", info.localPlayerFood));
            csv(w, "player",      "armor",             String.format("%.0f", info.localPlayerArmor));
            csv(w, "player",      "xpLevel",           String.valueOf(info.localPlayerLevel));
            csv(w, "player",      "x",                 String.format("%.4f", info.localPlayerX));
            csv(w, "player",      "y",                 String.format("%.4f", info.localPlayerY));
            csv(w, "player",      "z",                 String.format("%.4f", info.localPlayerZ));
            csv(w, "player",      "chunkX",            String.valueOf(info.localPlayerChunkX));
            csv(w, "player",      "chunkZ",            String.valueOf(info.localPlayerChunkZ));
            csv(w, "player",      "flying",            String.valueOf(info.localPlayerFlying));
            csv(w, "player",      "isOp",              String.valueOf(info.localPlayerOp));
            csv(w, "entities",    "total",             String.valueOf(info.totalEntityCount));
            csv(w, "entities",    "hostile",           String.valueOf(info.hostileEntityCount));
            csv(w, "entities",    "passive",           String.valueOf(info.passiveEntityCount));
            csv(w, "entities",    "items",             String.valueOf(info.itemEntityCount));
            csv(w, "performance", "tps",               String.format("%.2f", info.tps));
            csv(w, "performance", "mspt",              String.format("%.2f", info.mspt));
            csv(w, "performance", "fps",               String.valueOf(info.clientFps));
            csv(w, "performance", "usedMemoryMb",      String.valueOf(info.usedMemoryMb));
            csv(w, "performance", "loadedChunks",      String.valueOf(info.loadedChunkCount));
            csv(w, "performance", "serverRenderDist",  String.valueOf(info.serverRenderDistance));
            csv(w, "network",     "sentPackets",       String.valueOf(info.networkSentPackets));
            csv(w, "network",     "recvPackets",       String.valueOf(info.networkRecvPackets));
            csv(w, "network",     "sentBytes",         NetworkScraper.formatBytes(info.networkSentBytes));
            csv(w, "network",     "recvBytes",         NetworkScraper.formatBytes(info.networkRecvBytes));

            // Gamerules
            for (Map.Entry<String, String> e : info.gamerules.entrySet()) {
                csv(w, "gamerule", e.getKey(), e.getValue());
            }

            // Online players — one row each
            for (ServerInfo.PlayerEntry p : info.onlinePlayers) {
                csv(w, "player_list", p.name,
                    p.gamemode + "|" + p.latencyMs + "ms|" + (p.uuid != null ? p.uuid : ""));
            }

            // Plugin channels
            for (String ch : info.pluginChannels) {
                csv(w, "plugin_channel", ch, "");
            }
        }
    }

    private static void csv(PrintWriter w, String category, String key, String value) {
        // Escape commas and quotes in values
        String v = value == null ? "" : value.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            v = "\"" + v + "\"";
        }
        w.println(category + "," + key + "," + v);
    }
}
