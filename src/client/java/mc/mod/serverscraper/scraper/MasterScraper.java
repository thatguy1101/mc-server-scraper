package mc.mod.serverscraper.scraper;

import mc.mod.serverscraper.config.ScraperConfig;
import mc.mod.serverscraper.data.ServerInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Orchestrates all individual scrapers.  Called on a configurable tick cadence
 * from the client tick event registered in ServerscraperClient.
 */
@Environment(EnvType.CLIENT)
public class MasterScraper {

    private static final Logger LOGGER = LoggerFactory.getLogger("ServerScraper");

    /** Singleton data object shared across the whole mod. */
    public static final ServerInfo INFO = new ServerInfo();

    private static int ticksSinceRefresh = 0;
    private static boolean connected     = false;

    /** Called every client tick. */
    public static void tick() {
        if (!connected) return;

        ticksSinceRefresh++;
        int interval = ScraperConfig.get().refreshIntervalTicks;
        if (interval <= 0) interval = 20;

        if (ticksSinceRefresh >= interval) {
            ticksSinceRefresh = 0;
            refresh();
        }
    }

    /** Force a full immediate refresh (e.g. when a command is run). */
    public static void refresh() {
        try {
            ConnectionScraper.scrape(INFO);
            WorldScraper.scrape(INFO);
            PlayerScraper.scrape(INFO);
            EntityScraper.scrape(INFO);
            ChunkScraper.scrape(INFO);
            PerformanceScraper.scrape(INFO);
            NetworkScraper.scrape(INFO);
            ScoreboardScraper.scrape(INFO);
            BossBarScraper.scrape(INFO);
            TpsTracker.push(INFO);
            INFO.lastRefreshed = Instant.now();
        } catch (Exception e) {
            if (ScraperConfig.get().verboseLogging) {
                LOGGER.error("Error during scrape refresh", e);
            }
        }
    }

    public static void onConnect() {
        INFO.reset();
        NetworkScraper.reset();
        TpsTracker.reset();
        ticksSinceRefresh = 0;
        connected = true;
        LOGGER.info("ServerScraper connected — data collection started.");
    }

    public static void onDisconnect() {
        connected = false;
        LOGGER.info("ServerScraper disconnected.");
    }

    public static boolean isConnected() {
        return connected;
    }
}
