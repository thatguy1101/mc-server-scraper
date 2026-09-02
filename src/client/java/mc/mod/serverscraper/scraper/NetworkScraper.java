package mc.mod.serverscraper.scraper;

import mc.mod.serverscraper.data.ServerInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Counts packets and bytes flowing over the connection.
 * Counters are incremented by the packet-intercept mixins and polled here.
 */
@Environment(EnvType.CLIENT)
public class NetworkScraper {

    // Atomic so mixin callbacks on the netty thread are safe
    public static final AtomicInteger sentPackets = new AtomicInteger(0);
    public static final AtomicInteger recvPackets = new AtomicInteger(0);
    public static final AtomicLong    sentBytes   = new AtomicLong(0);
    public static final AtomicLong    recvBytes   = new AtomicLong(0);

    public static void scrape(ServerInfo info) {
        info.networkSentPackets = sentPackets.get();
        info.networkRecvPackets = recvPackets.get();
        info.networkSentBytes   = sentBytes.get();
        info.networkRecvBytes   = recvBytes.get();
    }

    public static void reset() {
        sentPackets.set(0);
        recvPackets.set(0);
        sentBytes.set(0);
        recvBytes.set(0);
    }

    public static String formatBytes(long bytes) {
        if (bytes < 1024)                  return bytes + " B";
        if (bytes < 1024 * 1024)           return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024L)   return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
