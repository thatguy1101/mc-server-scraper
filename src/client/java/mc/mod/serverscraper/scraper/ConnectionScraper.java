package mc.mod.serverscraper.scraper;

import mc.mod.serverscraper.data.ServerInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Scrapes connection-level data: IP, port, brand, version, ping, online-mode.
 */
@Environment(EnvType.CLIENT)
public class ConnectionScraper {

    private static final Logger LOGGER = LoggerFactory.getLogger("ServerScraper/Connection");

    public static void scrape(mc.mod.serverscraper.data.ServerInfo info) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;

        // ── Single-player vs multiplayer ──────────────────────────────────────
        info.isSinglePlayer = mc.isInSingleplayer();

        ClientPlayNetworkHandler handler = mc.getNetworkHandler();
        if (handler == null) return;

        // ── Server address / port ─────────────────────────────────────────────
        SocketAddress socketAddr = handler.getConnection().getAddress();
        if (socketAddr instanceof InetSocketAddress inet) {
            info.resolvedIp = inet.getAddress() != null
                    ? inet.getAddress().getHostAddress()
                    : inet.getHostName();
            info.serverPort = inet.getPort();
        }

        // ── Server list entry (address as typed, version string) ──────────────
        net.minecraft.client.network.ServerInfo serverListEntry = mc.getCurrentServerEntry();
        if (serverListEntry != null) {
            info.serverAddress   = serverListEntry.address;
            info.serverVersion   = serverListEntry.version != null
                    ? serverListEntry.version.getString()
                    : "N/A";
            info.isOnlineMode    = !serverListEntry.isLocal();
            info.isLanServer     = serverListEntry.isLocal();
            info.pingMs          = (int) serverListEntry.ping;
        }

        // ── Protocol version ──────────────────────────────────────────────────
        if (handler.getConnection().channel() != null) {
            info.protocolVersion = net.minecraft.SharedConstants.getGameVersion().getProtocolVersion();
        }

        // ── Online mode heuristic ─────────────────────────────────────────────
        if (info.isSinglePlayer) {
            info.isOnlineMode = false;
            info.serverAddress = "Singleplayer";
            info.resolvedIp    = "127.0.0.1";
            info.serverPort    = -1;
        }
    }

    /** Called by the brand mixin when the server sends its brand string. */
    public static void onBrandReceived(mc.mod.serverscraper.data.ServerInfo info, String brand) {
        info.serverBrand = brand != null ? brand : "vanilla";
        LOGGER.info("Server brand: {}", info.serverBrand);

        // Heuristic detection of server software from brand string
        String lower = info.serverBrand.toLowerCase();
        if (lower.contains("paper"))       info.detectedServerSoftware.add("Paper");
        if (lower.contains("purpur"))      info.detectedServerSoftware.add("Purpur");
        if (lower.contains("spigot"))      info.detectedServerSoftware.add("Spigot");
        if (lower.contains("bukkit"))      info.detectedServerSoftware.add("Bukkit");
        if (lower.contains("velocity"))    info.detectedServerSoftware.add("Velocity");
        if (lower.contains("bungeecord")) info.detectedServerSoftware.add("BungeeCord");
        if (lower.contains("waterfall"))  info.detectedServerSoftware.add("Waterfall");
        if (lower.contains("fabric"))     info.detectedServerSoftware.add("Fabric");
        if (lower.contains("forge"))      info.detectedServerSoftware.add("Forge");
        if (lower.contains("neoforge"))   info.detectedServerSoftware.add("NeoForge");
        if (lower.contains("vanilla"))    info.detectedServerSoftware.add("Vanilla");
        if (lower.contains("folia"))      info.detectedServerSoftware.add("Folia");
        if (lower.contains("minestom"))   info.detectedServerSoftware.add("Minestom");
        if (lower.contains("pufferfish")) info.detectedServerSoftware.add("Pufferfish");
    }
}
