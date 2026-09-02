package mc.mod.serverscraper.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import mc.mod.serverscraper.config.ScraperConfig;
import mc.mod.serverscraper.data.ServerInfo;
import mc.mod.serverscraper.export.DataExporter;
import mc.mod.serverscraper.scraper.MasterScraper;
import mc.mod.serverscraper.scraper.NetworkScraper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

/**
 * Registers /scraper and all its sub-commands.
 * All commands are handled entirely client-side — nothing is sent to the server.
 */
@Environment(EnvType.CLIENT)
public class ScraperCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("scraper")
            .then(literal("info")
                .executes(ctx -> cmdInfo(ctx, "all"))
                .then(literal("connection")  .executes(ctx -> cmdInfo(ctx, "connection")))
                .then(literal("world")       .executes(ctx -> cmdInfo(ctx, "world")))
                .then(literal("player")      .executes(ctx -> cmdInfo(ctx, "player")))
                .then(literal("players")     .executes(ctx -> cmdInfo(ctx, "players")))
                .then(literal("entities")    .executes(ctx -> cmdInfo(ctx, "entities")))
                .then(literal("chunks")      .executes(ctx -> cmdInfo(ctx, "chunks")))
                .then(literal("performance") .executes(ctx -> cmdInfo(ctx, "performance")))
                .then(literal("gamerules")   .executes(ctx -> cmdInfo(ctx, "gamerules")))
                .then(literal("scoreboard")  .executes(ctx -> cmdInfo(ctx, "scoreboard")))
                .then(literal("bossbars")    .executes(ctx -> cmdInfo(ctx, "bossbars")))
                .then(literal("network")     .executes(ctx -> cmdInfo(ctx, "network")))
                .then(literal("plugins")     .executes(ctx -> cmdInfo(ctx, "plugins")))
            )
            .then(literal("refresh")         .executes(ScraperCommand::cmdRefresh))
            .then(literal("export")
                .executes(ctx -> cmdExport(ctx, "TXT"))
                .then(literal("txt")  .executes(ctx -> cmdExport(ctx, "TXT")))
                .then(literal("json") .executes(ctx -> cmdExport(ctx, "JSON")))
                .then(literal("csv")  .executes(ctx -> cmdExport(ctx, "CSV")))
            )
            .then(literal("hud")
                .then(literal("toggle")  .executes(ScraperCommand::cmdHudToggle))
                .then(literal("compact") .executes(ScraperCommand::cmdHudCompact))
                .then(literal("pos")
                    .then(argument("position", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            builder.suggest("TOP_LEFT");
                            builder.suggest("TOP_RIGHT");
                            builder.suggest("BOTTOM_LEFT");
                            builder.suggest("BOTTOM_RIGHT");
                            return builder.buildFuture();
                        })
                        .executes(ScraperCommand::cmdHudPos)
                    )
                )
                .then(literal("scale")
                    .then(argument("scale", StringArgumentType.word())
                        .executes(ScraperCommand::cmdHudScale)
                    )
                )
                .then(literal("sections")    .executes(ScraperCommand::cmdHudSections))
                .then(literal("show")
                    .then(argument("section", StringArgumentType.word())
                        .suggests((ctx, b) -> {
                            for (String s : HUD_SECTIONS) b.suggest(s);
                            return b.buildFuture();
                        })
                        .executes(ctx -> cmdHudSection(ctx, true))
                    )
                )
                .then(literal("hide")
                    .then(argument("section", StringArgumentType.word())
                        .suggests((ctx, b) -> {
                            for (String s : HUD_SECTIONS) b.suggest(s);
                            return b.buildFuture();
                        })
                        .executes(ctx -> cmdHudSection(ctx, false))
                    )
                )
            )
            .then(literal("config")
                .then(literal("reload")      .executes(ScraperCommand::cmdConfigReload))
                .then(literal("save")        .executes(ScraperCommand::cmdConfigSave))
                .then(literal("autojoin")    .executes(ctx -> cmdConfigToggle(ctx, "autojoin")))
                .then(literal("autoleave")   .executes(ctx -> cmdConfigToggle(ctx, "autoleave")))
                .then(literal("logfile")     .executes(ctx -> cmdConfigToggle(ctx, "logfile")))
                .then(literal("alertjoin")   .executes(ctx -> cmdConfigToggle(ctx, "alertjoin")))
                .then(literal("alertleave")  .executes(ctx -> cmdConfigToggle(ctx, "alertleave")))
                .then(literal("alerttps")    .executes(ctx -> cmdConfigToggle(ctx, "alerttps")))
                .then(literal("interval")
                    .then(argument("ticks", IntegerArgumentType.integer(1, 200))
                        .executes(ScraperCommand::cmdConfigInterval)
                    )
                )
            )
            .then(literal("players")         .executes(ScraperCommand::cmdPlayers))
            .then(literal("gamerules")       .executes(ScraperCommand::cmdGamerules))
            .then(literal("coords")          .executes(ScraperCommand::cmdCoords))
            .then(literal("ping")            .executes(ScraperCommand::cmdPing))
            .then(literal("tps")             .executes(ScraperCommand::cmdTps))
            .then(literal("time")            .executes(ScraperCommand::cmdTime))
            .then(literal("weather")         .executes(ScraperCommand::cmdWeather))
            .then(literal("entities")        .executes(ScraperCommand::cmdEntities))
            .then(literal("channels")        .executes(ScraperCommand::cmdChannels))
            .then(literal("brand")           .executes(ScraperCommand::cmdBrand))
            .then(literal("seed")            .executes(ScraperCommand::cmdSeed))
            .then(literal("network")         .executes(ScraperCommand::cmdNetwork))
            .then(literal("reset")           .executes(ScraperCommand::cmdReset))
            .then(literal("help")            .executes(ScraperCommand::cmdHelp))
            .executes(ctx -> cmdInfo(ctx, "all"))
        );

        // Short alias
        dispatcher.register(literal("ss")
            .redirect(dispatcher.getRoot().getChild("scraper"))
        );
    }

    // ── Section names for autocomplete ────────────────────────────────────────
    private static final String[] HUD_SECTIONS = {
        "connection", "world", "player", "players", "entities", "performance", "scoreboard"
    };

    // ─────────────────────────────────────────────────────────────────────────
    // Command implementations
    // ─────────────────────────────────────────────────────────────────────────

    private static int cmdInfo(CommandContext<FabricClientCommandSource> ctx, String section) {
        MasterScraper.refresh();
        ServerInfo info = MasterScraper.INFO;
        FabricClientCommandSource src = ctx.getSource();

        switch (section) {
            case "connection" -> printConnection(src, info);
            case "world"      -> printWorld(src, info);
            case "player"     -> printPlayer(src, info);
            case "players"    -> printPlayers(src, info);
            case "entities"   -> printEntities(src, info);
            case "chunks"     -> printChunks(src, info);
            case "performance"-> printPerformance(src, info);
            case "gamerules"  -> printGamerules(src, info);
            case "scoreboard" -> printScoreboard(src, info);
            case "bossbars"   -> printBossBars(src, info);
            case "network"    -> printNetwork(src, info);
            case "plugins"    -> printPlugins(src, info);
            default -> {
                printConnection(src, info);
                printWorld(src, info);
                printPlayer(src, info);
                printPlayers(src, info);
                printEntities(src, info);
                printChunks(src, info);
                printPerformance(src, info);
                printNetwork(src, info);
            }
        }
        return 1;
    }

    private static int cmdRefresh(CommandContext<FabricClientCommandSource> ctx) {
        MasterScraper.refresh();
        ctx.getSource().sendFeedback(ok("Data refreshed."));
        return 1;
    }

    private static int cmdExport(CommandContext<FabricClientCommandSource> ctx, String format) {
        MasterScraper.refresh();
        try {
            String path = DataExporter.export(MasterScraper.INFO, format);
            MutableText msg = ok("Exported as " + format + " → ")
                .append(Text.literal(path)
                    .styled(s -> s.withColor(Formatting.AQUA)
                        .withUnderline(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, path))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            Text.literal("Click to open file")))
                    )
                );
            ctx.getSource().sendFeedback(msg);
        } catch (Exception e) {
            ctx.getSource().sendFeedback(err("Export failed: " + e.getMessage()));
        }
        return 1;
    }

    private static int cmdHudToggle(CommandContext<FabricClientCommandSource> ctx) {
        ScraperConfig cfg = ScraperConfig.get();
        cfg.hudEnabled = !cfg.hudEnabled;
        cfg.save();
        ctx.getSource().sendFeedback(ok("HUD " + (cfg.hudEnabled ? "enabled" : "disabled") + "."));
        return 1;
    }

    private static int cmdHudCompact(CommandContext<FabricClientCommandSource> ctx) {
        ScraperConfig cfg = ScraperConfig.get();
        cfg.hudCompact = !cfg.hudCompact;
        cfg.save();
        ctx.getSource().sendFeedback(ok("HUD compact mode " + (cfg.hudCompact ? "on" : "off") + "."));
        return 1;
    }

    private static int cmdHudPos(CommandContext<FabricClientCommandSource> ctx) {
        String pos = StringArgumentType.getString(ctx, "position").toUpperCase();
        if (!pos.equals("TOP_LEFT") && !pos.equals("TOP_RIGHT")
                && !pos.equals("BOTTOM_LEFT") && !pos.equals("BOTTOM_RIGHT")) {
            ctx.getSource().sendFeedback(err("Valid positions: TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT"));
            return 0;
        }
        ScraperConfig cfg = ScraperConfig.get();
        cfg.hudPosition = pos;
        cfg.save();
        ctx.getSource().sendFeedback(ok("HUD position set to " + pos + "."));
        return 1;
    }

    private static int cmdHudScale(CommandContext<FabricClientCommandSource> ctx) {
        try {
            float scale = Float.parseFloat(StringArgumentType.getString(ctx, "scale"));
            if (scale < 0.5f || scale > 3.0f) {
                ctx.getSource().sendFeedback(err("Scale must be between 0.5 and 3.0."));
                return 0;
            }
            ScraperConfig cfg = ScraperConfig.get();
            cfg.hudScale = scale;
            cfg.save();
            ctx.getSource().sendFeedback(ok("HUD scale set to " + scale + "."));
            return 1;
        } catch (NumberFormatException e) {
            ctx.getSource().sendFeedback(err("Invalid scale value."));
            return 0;
        }
    }

    private static int cmdHudSections(CommandContext<FabricClientCommandSource> ctx) {
        ScraperConfig cfg = ScraperConfig.get();
        FabricClientCommandSource src = ctx.getSource();
        src.sendFeedback(header("HUD Sections"));
        src.sendFeedback(kv("connection",   cfg.hudShowConnection));
        src.sendFeedback(kv("world",        cfg.hudShowWorld));
        src.sendFeedback(kv("player",       cfg.hudShowPlayer));
        src.sendFeedback(kv("players",      cfg.hudShowPlayers));
        src.sendFeedback(kv("entities",     cfg.hudShowEntities));
        src.sendFeedback(kv("performance",  cfg.hudShowPerf));
        src.sendFeedback(kv("scoreboard",   cfg.hudShowScoreboard));
        return 1;
    }

    private static int cmdHudSection(CommandContext<FabricClientCommandSource> ctx, boolean show) {
        String section = StringArgumentType.getString(ctx, "section").toLowerCase();
        ScraperConfig cfg = ScraperConfig.get();
        switch (section) {
            case "connection"   -> cfg.hudShowConnection  = show;
            case "world"        -> cfg.hudShowWorld       = show;
            case "player"       -> cfg.hudShowPlayer      = show;
            case "players"      -> cfg.hudShowPlayers     = show;
            case "entities"     -> cfg.hudShowEntities    = show;
            case "performance"  -> cfg.hudShowPerf        = show;
            case "scoreboard"   -> cfg.hudShowScoreboard  = show;
            default -> {
                ctx.getSource().sendFeedback(err("Unknown section: " + section));
                return 0;
            }
        }
        cfg.save();
        ctx.getSource().sendFeedback(ok("Section '" + section + "' " + (show ? "shown" : "hidden") + "."));
        return 1;
    }

    private static int cmdConfigReload(CommandContext<FabricClientCommandSource> ctx) {
        ScraperConfig.get().reload();
        ctx.getSource().sendFeedback(ok("Config reloaded from disk."));
        return 1;
    }

    private static int cmdConfigSave(CommandContext<FabricClientCommandSource> ctx) {
        ScraperConfig.get().save();
        ctx.getSource().sendFeedback(ok("Config saved to disk."));
        return 1;
    }

    private static int cmdConfigToggle(CommandContext<FabricClientCommandSource> ctx, String key) {
        ScraperConfig cfg = ScraperConfig.get();
        String label;
        boolean newVal;
        switch (key) {
            case "autojoin"   -> { cfg.autoExportOnJoin  = !cfg.autoExportOnJoin;  newVal = cfg.autoExportOnJoin;  label = "Auto-export on join"; }
            case "autoleave"  -> { cfg.autoExportOnLeave = !cfg.autoExportOnLeave; newVal = cfg.autoExportOnLeave; label = "Auto-export on leave"; }
            case "logfile"    -> { cfg.logToFile         = !cfg.logToFile;         newVal = cfg.logToFile;         label = "Log to file"; }
            case "alertjoin"  -> { cfg.alertOnPlayerJoin = !cfg.alertOnPlayerJoin; newVal = cfg.alertOnPlayerJoin; label = "Alert on player join"; }
            case "alertleave" -> { cfg.alertOnPlayerLeave= !cfg.alertOnPlayerLeave;newVal = cfg.alertOnPlayerLeave;label = "Alert on player leave"; }
            case "alerttps"   -> { cfg.alertLowTps       = !cfg.alertLowTps;       newVal = cfg.alertLowTps;       label = "Low TPS alert"; }
            default -> { ctx.getSource().sendFeedback(err("Unknown config key.")); return 0; }
        }
        cfg.save();
        ctx.getSource().sendFeedback(ok(label + " " + (newVal ? "enabled" : "disabled") + "."));
        return 1;
    }

    private static int cmdConfigInterval(CommandContext<FabricClientCommandSource> ctx) {
        int ticks = IntegerArgumentType.getInteger(ctx, "ticks");
        ScraperConfig cfg = ScraperConfig.get();
        cfg.refreshIntervalTicks = ticks;
        cfg.save();
        ctx.getSource().sendFeedback(ok("Refresh interval set to " + ticks + " ticks (" +
                String.format("%.1f", ticks / 20.0) + "s)."));
        return 1;
    }

    private static int cmdPlayers(CommandContext<FabricClientCommandSource> ctx) {
        MasterScraper.refresh();
        printPlayers(ctx.getSource(), MasterScraper.INFO);
        return 1;
    }

    private static int cmdGamerules(CommandContext<FabricClientCommandSource> ctx) {
        MasterScraper.refresh();
        printGamerules(ctx.getSource(), MasterScraper.INFO);
        return 1;
    }

    private static int cmdCoords(CommandContext<FabricClientCommandSource> ctx) {
        MasterScraper.refresh();
        ServerInfo info = MasterScraper.INFO;
        FabricClientCommandSource src = ctx.getSource();
        src.sendFeedback(header("Coordinates"));
        src.sendFeedback(kv("XYZ",    String.format("%.3f, %.3f, %.3f",
                info.localPlayerX, info.localPlayerY, info.localPlayerZ)));
        src.sendFeedback(kv("Facing", String.format("%.1f° yaw, %.1f° pitch",
                info.localPlayerYaw, info.localPlayerPitch)));
        src.sendFeedback(kv("Chunk",  info.localPlayerChunkX + ", " + info.localPlayerChunkZ));
        src.sendFeedback(kv("Region", "r." + info.localPlayerRegionX + "." + info.localPlayerRegionZ + ".mca"));
        src.sendFeedback(kv("Dimension", info.dimensionType));

        // Nether / Overworld coordinate conversion
        if (info.dimensionId.equals("minecraft:overworld")) {
            src.sendFeedback(kv("Nether portal",
                String.format("%.0f, %.0f, %.0f",
                        info.localPlayerX / 8, info.localPlayerY, info.localPlayerZ / 8)));
        } else if (info.dimensionId.equals("minecraft:the_nether")) {
            src.sendFeedback(kv("Overworld portal",
                String.format("%.0f, %.0f, %.0f",
                        info.localPlayerX * 8, info.localPlayerY, info.localPlayerZ * 8)));
        }
        return 1;
    }

    private static int cmdPing(CommandContext<FabricClientCommandSource> ctx) {
        MasterScraper.refresh();
        ServerInfo info = MasterScraper.INFO;
        ctx.getSource().sendFeedback(ok(String.format(
            "Your ping: %dms | Server brand: %s | Address: %s:%d",
            info.localPlayerPing, info.serverBrand, info.serverAddress, info.serverPort)));
        return 1;
    }

    private static int cmdTps(CommandContext<FabricClientCommandSource> ctx) {
        MasterScraper.refresh();
        ServerInfo info = MasterScraper.INFO;
        Formatting color = info.tps >= 19.0 ? Formatting.GREEN
                         : info.tps >= 15.0 ? Formatting.YELLOW
                         : Formatting.RED;
        ctx.getSource().sendFeedback(
            Text.literal("[ServerScraper] ").formatted(Formatting.AQUA)
                .append(Text.literal(String.format("TPS: %.2f", info.tps)).formatted(color))
                .append(Text.literal(String.format("  MSPT: %.2fms", info.mspt)).formatted(Formatting.GRAY))
        );
        return 1;
    }

    private static int cmdTime(CommandContext<FabricClientCommandSource> ctx) {
        MasterScraper.refresh();
        ServerInfo info = MasterScraper.INFO;
        FabricClientCommandSource src = ctx.getSource();
        src.sendFeedback(header("World Time"));
        src.sendFeedback(kv("Time of day", info.worldTimeFormatted));
        src.sendFeedback(kv("Daytime",     info.isDaytime ? "Yes" : "No"));
        src.sendFeedback(kv("Day number",  String.valueOf(info.dayNumber)));
        src.sendFeedback(kv("World age",   info.worldAgeFormatted));
        src.sendFeedback(kv("Fixed time",  info.hasFixedTime ? "Yes" : "No"));
        return 1;
    }

    private static int cmdWeather(CommandContext<FabricClientCommandSource> ctx) {
        MasterScraper.refresh();
        ServerInfo info = MasterScraper.INFO;
        FabricClientCommandSource src = ctx.getSource();
        src.sendFeedback(header("Weather"));
        src.sendFeedback(kv("Raining",    info.isRaining + " (level " + String.format("%.2f", info.rainLevel) + ")"));
        src.sendFeedback(kv("Thunder",    info.isThundering + " (level " + String.format("%.2f", info.thunderLevel) + ")"));
        return 1;
    }

    private static int cmdEntities(CommandContext<FabricClientCommandSource> ctx) {
        MasterScraper.refresh();
        printEntities(ctx.getSource(), MasterScraper.INFO);
        return 1;
    }

    private static int cmdChannels(CommandContext<FabricClientCommandSource> ctx) {
        MasterScraper.refresh();
        ServerInfo info = MasterScraper.INFO;
        FabricClientCommandSource src = ctx.getSource();
        src.sendFeedback(header("Plugin Channels (" + info.pluginChannels.size() + ")"));
        if (info.pluginChannels.isEmpty()) {
            src.sendFeedback(dim("  None detected."));
        } else {
            for (String ch : info.pluginChannels) {
                src.sendFeedback(dim("  · " + ch));
            }
        }
        return 1;
    }

    private static int cmdBrand(CommandContext<FabricClientCommandSource> ctx) {
        MasterScraper.refresh();
        ServerInfo info = MasterScraper.INFO;
        FabricClientCommandSource src = ctx.getSource();
        src.sendFeedback(header("Server Software"));
        src.sendFeedback(kv("Brand string", info.serverBrand));
        src.sendFeedback(kv("Version",      info.serverVersion));
        src.sendFeedback(kv("Protocol",     String.valueOf(info.protocolVersion)));
        if (!info.detectedServerSoftware.isEmpty()) {
            src.sendFeedback(kv("Detected",
                String.join(", ", info.detectedServerSoftware)));
        }
        if (!info.pluginChannels.isEmpty()) {
            // Show channels that hint at plugins
            src.sendFeedback(kv("Plugin channels", String.valueOf(info.pluginChannels.size())));
        }
        return 1;
    }

    private static int cmdSeed(CommandContext<FabricClientCommandSource> ctx) {
        ServerInfo info = MasterScraper.INFO;
        if (info.isSinglePlayer && info.worldSeed != 0) {
            ctx.getSource().sendFeedback(ok("World seed: " + info.worldSeed));
        } else {
            ctx.getSource().sendFeedback(err(
                "Seed is only available in singleplayer (servers don't send it)."));
        }
        return 1;
    }

    private static int cmdNetwork(CommandContext<FabricClientCommandSource> ctx) {
        MasterScraper.refresh();
        printNetwork(ctx.getSource(), MasterScraper.INFO);
        return 1;
    }

    private static int cmdReset(CommandContext<FabricClientCommandSource> ctx) {
        MasterScraper.INFO.reset();
        NetworkScraper.reset();
        ctx.getSource().sendFeedback(ok("Data reset."));
        return 1;
    }

    private static int cmdHelp(CommandContext<FabricClientCommandSource> ctx) {
        FabricClientCommandSource src = ctx.getSource();
        src.sendFeedback(header("ServerScraper Commands (/scraper or /ss)"));
        String[][] cmds = {
            {"info [section]",     "Full dump or specific section"},
            {"refresh",            "Force immediate data refresh"},
            {"export [txt|json|csv]", "Export data to file"},
            {"players",            "List all online players"},
            {"gamerules",          "List all server gamerules"},
            {"coords",             "Your coordinates + portal calc"},
            {"ping",               "Your ping + server info"},
            {"tps",                "Server TPS & MSPT estimate"},
            {"time",               "World time & age"},
            {"weather",            "Current weather"},
            {"entities",           "Entity counts by type"},
            {"channels",           "Plugin channels"},
            {"brand",              "Server software detection"},
            {"seed",               "World seed (singleplayer only)"},
            {"network",            "Packet/byte counters"},
            {"hud toggle",         "Toggle the HUD overlay"},
            {"hud compact",        "Toggle compact HUD"},
            {"hud pos <position>", "Move HUD (TOP_LEFT etc.)"},
            {"hud scale <n>",      "Set HUD scale (0.5–3.0)"},
            {"hud show/hide <s>",  "Show/hide HUD section"},
            {"config reload",      "Reload config from disk"},
            {"config save",        "Save config to disk"},
            {"config autojoin",    "Toggle auto-export on join"},
            {"config autoleave",   "Toggle auto-export on leave"},
            {"config interval <t>","Set refresh interval (ticks)"},
            {"reset",              "Reset all collected data"},
            {"help",               "Show this help message"},
        };
        for (String[] cmd : cmds) {
            src.sendFeedback(
                Text.literal("  /ss " + cmd[0]).formatted(Formatting.YELLOW)
                    .append(Text.literal(" — " + cmd[1]).formatted(Formatting.GRAY))
            );
        }
        return 1;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Print helpers
    // ─────────────────────────────────────────────────────────────────────────

    private static void printConnection(FabricClientCommandSource src, ServerInfo info) {
        src.sendFeedback(header("Connection"));
        src.sendFeedback(kv("Address",     info.serverAddress));
        src.sendFeedback(kv("Resolved IP", info.resolvedIp + ":" + info.serverPort));
        src.sendFeedback(kv("Brand",       info.serverBrand));
        src.sendFeedback(kv("Version",     info.serverVersion));
        src.sendFeedback(kv("Protocol",    String.valueOf(info.protocolVersion)));
        src.sendFeedback(kv("Ping",        info.pingMs + "ms"));
        src.sendFeedback(kv("Online mode", String.valueOf(info.isOnlineMode)));
        src.sendFeedback(kv("Type",        info.isSinglePlayer ? "Singleplayer"
                           : info.isLanServer ? "LAN" : "Online server"));
        if (!info.detectedServerSoftware.isEmpty())
            src.sendFeedback(kv("Detected software",
                String.join(", ", info.detectedServerSoftware)));
    }

    private static void printWorld(FabricClientCommandSource src, ServerInfo info) {
        src.sendFeedback(header("World"));
        src.sendFeedback(kv("Dimension",   info.dimensionType));
        src.sendFeedback(kv("Time",        info.worldTimeFormatted));
        src.sendFeedback(kv("Day",         String.valueOf(info.dayNumber)));
        src.sendFeedback(kv("Age",         info.worldAgeFormatted));
        src.sendFeedback(kv("Weather",     info.isThundering ? "Thunder"
                           : info.isRaining ? "Rain" : "Clear"));
        src.sendFeedback(kv("Superflat",   String.valueOf(info.isSuperFlat)));
        src.sendFeedback(kv("Debug world", String.valueOf(info.isDebugWorld)));
        src.sendFeedback(kv("Sky light",   String.valueOf(info.hasSkyLight)));
        src.sendFeedback(kv("Ceiling",     String.valueOf(info.hasCeiling)));
        src.sendFeedback(kv("Ultrawarm",   String.valueOf(info.isUltrawarm)));
        src.sendFeedback(kv("Min Y / Max Y", info.minY + " / " + (info.minY + info.height)));
        src.sendFeedback(kv("Logical height", String.valueOf(info.logicalHeight)));
        src.sendFeedback(kv("Fixed time",  String.valueOf(info.hasFixedTime)));
        src.sendFeedback(kv("Effects",     info.effectsId));
    }

    private static void printPlayer(FabricClientCommandSource src, ServerInfo info) {
        src.sendFeedback(header("Local Player"));
        src.sendFeedback(kv("Name",        info.localPlayerName));
        src.sendFeedback(kv("UUID",        info.localPlayerUuidStr));
        src.sendFeedback(kv("Gamemode",    info.localPlayerGamemode));
        src.sendFeedback(kv("Ping",        info.localPlayerPing + "ms"));
        src.sendFeedback(kv("Health",      String.format("%.1f / 20.0", info.localPlayerHealth)));
        src.sendFeedback(kv("Food",        String.format("%.0f / 20", info.localPlayerFood)));
        src.sendFeedback(kv("Saturation",  String.format("%.1f", info.localPlayerSaturation)));
        src.sendFeedback(kv("Armor",       String.format("%.0f", info.localPlayerArmor)));
        src.sendFeedback(kv("Level / XP",  info.localPlayerLevel + " (" +
                String.format("%.1f%%", info.localPlayerXp * 100) + ")"));
        src.sendFeedback(kv("Total XP",    String.valueOf(info.localPlayerTotalXp)));
        src.sendFeedback(kv("XYZ",         String.format("%.3f, %.3f, %.3f",
                info.localPlayerX, info.localPlayerY, info.localPlayerZ)));
        src.sendFeedback(kv("Facing",      String.format("%.1f° yaw, %.1f° pitch",
                info.localPlayerYaw, info.localPlayerPitch)));
        src.sendFeedback(kv("Chunk",       info.localPlayerChunkX + ", " + info.localPlayerChunkZ));
        src.sendFeedback(kv("Region",      "r." + info.localPlayerRegionX + "." + info.localPlayerRegionZ + ".mca"));
        src.sendFeedback(kv("Speed",       String.format("%.3f b/t", info.localPlayerSpeed)));
        src.sendFeedback(kv("Flying",      String.valueOf(info.localPlayerFlying)));
        src.sendFeedback(kv("Sneaking",    String.valueOf(info.localPlayerSneaking)));
        src.sendFeedback(kv("On ground",   String.valueOf(info.localPlayerOnGround)));
        src.sendFeedback(kv("In water",    String.valueOf(info.localPlayerInWater)));
        src.sendFeedback(kv("In lava",     String.valueOf(info.localPlayerInLava)));
        src.sendFeedback(kv("Op / Perms",  info.localPlayerOp + " (level " + info.localPlayerPermLevel + ")"));
        src.sendFeedback(kv("Team",        info.localPlayerTeam));
    }

    private static void printPlayers(FabricClientCommandSource src, ServerInfo info) {
        src.sendFeedback(header("Online Players (" + info.playerCount + ")"));
        if (info.onlinePlayers.isEmpty()) {
            src.sendFeedback(dim("  No players in tab list."));
            return;
        }
        for (ServerInfo.PlayerEntry p : info.onlinePlayers) {
            String latency = p.latencyMs >= 0 ? p.latencyMs + "ms" : "?";
            String line = String.format("  %-20s  %-9s  %s",
                    p.name, p.gamemode, latency);
            if (!p.team.isEmpty()) line += "  [" + p.team + "]";
            src.sendFeedback(dim(line));
        }
    }

    private static void printEntities(FabricClientCommandSource src, ServerInfo info) {
        src.sendFeedback(header("Entities (loaded)"));
        src.sendFeedback(kv("Total",    String.valueOf(info.totalEntityCount)));
        src.sendFeedback(kv("Players",  String.valueOf(info.playerEntityCount)));
        src.sendFeedback(kv("Hostile",  String.valueOf(info.hostileEntityCount)));
        src.sendFeedback(kv("Passive",  String.valueOf(info.passiveEntityCount)));
        src.sendFeedback(kv("Items",    String.valueOf(info.itemEntityCount)));
        src.sendFeedback(kv("Other",    String.valueOf(
            info.totalEntityCount - info.playerEntityCount
            - info.hostileEntityCount - info.passiveEntityCount
            - info.itemEntityCount)));

        if (!info.entityTypeCounts.isEmpty()) {
            src.sendFeedback(dim("  --- Top types ---"));
            info.entityTypeCounts.entrySet().stream().limit(15).forEach(e ->
                src.sendFeedback(dim("  " + e.getKey() + ": " + e.getValue()))
            );
        }
    }

    private static void printChunks(FabricClientCommandSource src, ServerInfo info) {
        src.sendFeedback(header("Chunks"));
        src.sendFeedback(kv("Loaded chunks",       String.valueOf(info.loadedChunkCount)));
        src.sendFeedback(kv("Client render dist",  info.chunkRenderDistance + " chunks"));
        src.sendFeedback(kv("Server render dist",  info.serverRenderDistance + " chunks"));
        src.sendFeedback(kv("Simulation dist",     info.simulationDistance + " chunks"));
    }

    private static void printPerformance(FabricClientCommandSource src, ServerInfo info) {
        src.sendFeedback(header("Performance"));
        Formatting tpsColor = info.tps >= 19.0 ? Formatting.GREEN
                            : info.tps >= 15.0 ? Formatting.YELLOW : Formatting.RED;
        src.sendFeedback(
            Text.literal("  TPS: ").formatted(Formatting.GRAY)
                .append(Text.literal(String.format("%.2f", info.tps)).formatted(tpsColor))
                .append(Text.literal("  MSPT: " + String.format("%.2fms", info.mspt)).formatted(Formatting.GRAY))
        );
        src.sendFeedback(kv("Client FPS",    String.valueOf(info.clientFps)));
        src.sendFeedback(kv("Memory (used)", info.usedMemoryMb + " MB / " + info.maxMemoryMb + " MB"));
        src.sendFeedback(kv("Memory (free)", info.freeMemoryMb + " MB"));
    }

    private static void printGamerules(FabricClientCommandSource src, ServerInfo info) {
        src.sendFeedback(header("Gamerules (" + info.gamerules.size() + ")"));
        if (info.gamerules.isEmpty()) {
            src.sendFeedback(dim("  No gamerule data (join a world first)."));
            return;
        }
        for (Map.Entry<String, String> e : info.gamerules.entrySet()) {
            src.sendFeedback(
                Text.literal("  " + e.getKey()).formatted(Formatting.YELLOW)
                    .append(Text.literal(" = " + e.getValue()).formatted(Formatting.WHITE))
            );
        }
    }

    private static void printScoreboard(FabricClientCommandSource src, ServerInfo info) {
        src.sendFeedback(header("Scoreboard: " + info.scoreboardTitle));
        if (info.scoreboardLines.isEmpty()) {
            src.sendFeedback(dim("  No sidebar scoreboard active."));
            return;
        }
        for (String line : info.scoreboardLines) {
            src.sendFeedback(dim("  " + line));
        }
    }

    private static void printBossBars(FabricClientCommandSource src, ServerInfo info) {
        src.sendFeedback(header("Boss Bars (" + info.bossBars.size() + ")"));
        if (info.bossBars.isEmpty()) {
            src.sendFeedback(dim("  No active boss bars."));
            return;
        }
        for (ServerInfo.BossBarEntry bar : info.bossBars) {
            src.sendFeedback(dim(String.format("  [%s] %.0f%%  color=%s  style=%s",
                    bar.name, bar.progress * 100, bar.color, bar.overlay)));
        }
    }

    private static void printNetwork(FabricClientCommandSource src, ServerInfo info) {
        src.sendFeedback(header("Network"));
        src.sendFeedback(kv("Sent packets",  String.valueOf(info.networkSentPackets)));
        src.sendFeedback(kv("Recv packets",  String.valueOf(info.networkRecvPackets)));
        src.sendFeedback(kv("Sent bytes",    NetworkScraper.formatBytes(info.networkSentBytes)));
        src.sendFeedback(kv("Recv bytes",    NetworkScraper.formatBytes(info.networkRecvBytes)));
    }

    private static void printPlugins(FabricClientCommandSource src, ServerInfo info) {
        src.sendFeedback(header("Detected Plugins / Software"));
        src.sendFeedback(kv("Brand",         info.serverBrand));
        src.sendFeedback(kv("Software",      info.detectedServerSoftware.isEmpty() ? "Unknown"
                : String.join(", ", info.detectedServerSoftware)));
        src.sendFeedback(kv("Plugin channels", String.valueOf(info.pluginChannels.size())));
        if (!info.pluginChannels.isEmpty()) {
            for (String ch : info.pluginChannels) src.sendFeedback(dim("    " + ch));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Text factory helpers
    // ─────────────────────────────────────────────────────────────────────────

    private static Text header(String title) {
        return Text.literal("━━ " + title + " ━━")
                .styled(s -> s.withColor(Formatting.AQUA).withBold(true));
    }

    private static Text kv(String key, String value) {
        return Text.literal("  " + key + ": ").formatted(Formatting.GRAY)
                .append(Text.literal(value).formatted(Formatting.WHITE));
    }

    private static Text kv(String key, boolean value) {
        Formatting color = value ? Formatting.GREEN : Formatting.RED;
        return Text.literal("  " + key + ": ").formatted(Formatting.GRAY)
                .append(Text.literal(String.valueOf(value)).formatted(color));
    }

    private static Text dim(String text) {
        return Text.literal(text).formatted(Formatting.GRAY);
    }

    private static MutableText ok(String text) {
        return Text.literal("[ServerScraper] ").formatted(Formatting.AQUA)
                .append(Text.literal(text).formatted(Formatting.GREEN));
    }

    private static Text err(String text) {
        return Text.literal("[ServerScraper] ").formatted(Formatting.AQUA)
                .append(Text.literal(text).formatted(Formatting.RED));
    }
}
