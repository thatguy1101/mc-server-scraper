package mc.mod.serverscraper.config;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;

/**
 * Persistent JSON config stored at .minecraft/config/serverscraper.json
 * All fields have sensible defaults so the mod works out of the box.
 */
public class ScraperConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("ServerScraper/Config");
    private static final Path CONFIG_FILE = FabricLoader.getInstance()
            .getConfigDir().resolve("serverscraper.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // ── HUD ───────────────────────────────────────────────────────────────────
    public boolean hudEnabled         = true;
    public String  hudPosition        = "TOP_LEFT";   // TOP_LEFT | TOP_RIGHT | BOTTOM_LEFT | BOTTOM_RIGHT
    public int     hudOffsetX         = 4;
    public int     hudOffsetY         = 4;
    public float   hudScale           = 1.0f;
    public boolean hudBackground      = true;
    public int     hudBackgroundColor = 0x88000000;   // ARGB
    public int     hudTextColor       = 0xFFFFFF;
    public boolean hudShowConnection  = true;
    public boolean hudShowWorld       = true;
    public boolean hudShowPlayer      = true;
    public boolean hudShowPlayers     = true;
    public boolean hudShowEntities    = true;
    public boolean hudShowPerf        = true;
    public boolean hudShowScoreboard  = false;
    public boolean hudCompact         = false;

    // ── Auto-export ───────────────────────────────────────────────────────────
    public boolean autoExportOnJoin   = false;
    public boolean autoExportOnLeave  = false;
    public String  exportFormat       = "TXT";        // TXT | JSON | CSV
    public String  exportDirectory    = "scraper_exports";

    // ── Refresh rates ─────────────────────────────────────────────────────────
    public int     refreshIntervalTicks = 20;         // how often live data is polled (client ticks)

    // ── Alerts ────────────────────────────────────────────────────────────────
    public boolean alertOnPlayerJoin  = false;
    public boolean alertOnPlayerLeave = false;
    public boolean alertLowTps        = false;
    public double  alertLowTpsThreshold = 15.0;
    public boolean alertBossBar       = false;

    // ── Misc ──────────────────────────────────────────────────────────────────
    public boolean logToFile          = false;
    public boolean verboseLogging     = false;

    // ─────────────────────────────────────────────────────────────────────────

    private static ScraperConfig INSTANCE;

    public static ScraperConfig get() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    public static ScraperConfig load() {
        if (Files.exists(CONFIG_FILE)) {
            try (Reader r = Files.newBufferedReader(CONFIG_FILE)) {
                ScraperConfig cfg = GSON.fromJson(r, ScraperConfig.class);
                if (cfg != null) {
                    LOGGER.info("Config loaded from {}", CONFIG_FILE);
                    return cfg;
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to load config, using defaults: {}", e.getMessage());
            }
        }
        ScraperConfig defaults = new ScraperConfig();
        defaults.save();
        return defaults;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            try (Writer w = Files.newBufferedWriter(CONFIG_FILE)) {
                GSON.toJson(this, w);
            }
            LOGGER.info("Config saved to {}", CONFIG_FILE);
        } catch (IOException e) {
            LOGGER.error("Failed to save config: {}", e.getMessage());
        }
    }

    public void reload() {
        ScraperConfig fresh = load();
        // Copy all fields from fresh into this instance so references to get() stay valid
        this.hudEnabled          = fresh.hudEnabled;
        this.hudPosition         = fresh.hudPosition;
        this.hudOffsetX          = fresh.hudOffsetX;
        this.hudOffsetY          = fresh.hudOffsetY;
        this.hudScale            = fresh.hudScale;
        this.hudBackground       = fresh.hudBackground;
        this.hudBackgroundColor  = fresh.hudBackgroundColor;
        this.hudTextColor        = fresh.hudTextColor;
        this.hudShowConnection   = fresh.hudShowConnection;
        this.hudShowWorld        = fresh.hudShowWorld;
        this.hudShowPlayer       = fresh.hudShowPlayer;
        this.hudShowPlayers      = fresh.hudShowPlayers;
        this.hudShowEntities     = fresh.hudShowEntities;
        this.hudShowPerf         = fresh.hudShowPerf;
        this.hudShowScoreboard   = fresh.hudShowScoreboard;
        this.hudCompact          = fresh.hudCompact;
        this.autoExportOnJoin    = fresh.autoExportOnJoin;
        this.autoExportOnLeave   = fresh.autoExportOnLeave;
        this.exportFormat        = fresh.exportFormat;
        this.exportDirectory     = fresh.exportDirectory;
        this.refreshIntervalTicks = fresh.refreshIntervalTicks;
        this.alertOnPlayerJoin   = fresh.alertOnPlayerJoin;
        this.alertOnPlayerLeave  = fresh.alertOnPlayerLeave;
        this.alertLowTps         = fresh.alertLowTps;
        this.alertLowTpsThreshold = fresh.alertLowTpsThreshold;
        this.alertBossBar        = fresh.alertBossBar;
        this.logToFile           = fresh.logToFile;
        this.verboseLogging      = fresh.verboseLogging;
        LOGGER.info("Config reloaded.");
    }
}
