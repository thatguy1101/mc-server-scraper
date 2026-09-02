package mc.mod.serverscraper.scraper;

import mc.mod.serverscraper.data.ServerInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Estimates server TPS by tracking the real-time intervals between
 * WorldTimeUpdateS2CPacket arrivals (intercepted via mixin).
 *
 * The server sends this packet once per game-tick (nominally every 50 ms).
 * By measuring the real wall-clock gap between packets and averaging over a
 * sliding window we get a solid TPS and MSPT estimate without needing server
 * access.
 */
@Environment(EnvType.CLIENT)
public class TpsTracker {

    private static final int WINDOW = 100;          // samples to average over
    private static final long[] timestamps = new long[WINDOW];
    private static int  head    = 0;
    private static int  count   = 0;
    private static long lastTs  = -1;

    /** Called by the WorldTimeUpdateS2CPacket mixin on every time packet. */
    public static void onTimePacket() {
        long now = System.currentTimeMillis();
        if (lastTs > 0) {
            long delta = now - lastTs;
            timestamps[head] = delta;
            head  = (head + 1) % WINDOW;
            count = Math.min(count + 1, WINDOW);
        }
        lastTs = now;
    }

    /** Push the latest estimate into the shared ServerInfo object. */
    public static void push(ServerInfo info) {
        if (count < 2) {
            info.tps  = 20.0;
            info.mspt = 50.0;
            return;
        }

        // Average millisecond gap between packets over the window
        long sum = 0;
        int  n   = count;
        for (int i = 0; i < n; i++) {
            sum += timestamps[i];
        }
        double avgMs = (double) sum / n;
        // Each packet = 1 server tick
        double mspt  = avgMs;
        double tps   = Math.min(20.0, 1000.0 / mspt);

        info.mspt = mspt;
        info.tps  = tps;
    }

    public static void reset() {
        head   = 0;
        count  = 0;
        lastTs = -1;
    }
}
