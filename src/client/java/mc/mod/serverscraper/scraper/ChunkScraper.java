package mc.mod.serverscraper.scraper;

import mc.mod.serverscraper.data.ServerInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientChunkManager;

/**
 * Scrapes chunk-related stats: loaded chunk count, render distance, simulation distance.
 */
@Environment(EnvType.CLIENT)
public class ChunkScraper {

    public static void scrape(ServerInfo info) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.world == null) return;

        // Render distance from client options
        info.chunkRenderDistance = mc.options.getViewDistance().getValue();
        info.simulationDistance  = mc.options.getSimulationDistance().getValue();

        // Server-reported render distance (comes via the GameJoinS2CPacket / login packet)
        // stored in the client world
        ClientChunkManager chunkManager = mc.world.getChunkManager();

        // Loaded chunk count — iterate the chunk cache
        // ClientChunkManager.chunks is a ChunkBiomeArray; we access it via the world
        try {
            // Use reflection-free approach: count via chunk provider
            // In 1.21 the loadedChunks count is accessible through the debug string
            String debugInfo = chunkManager.getDebugString();
            // Format is typically: "Client Chunk Cache: <size>, <loaded>"
            if (debugInfo != null) {
                String[] parts = debugInfo.split(",\\s*");
                for (String part : parts) {
                    part = part.trim();
                    if (part.matches("\\d+")) {
                        try {
                            info.loadedChunkCount = Integer.parseInt(part);
                            break;
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        } catch (Exception ignored) {}

        // Server render distance is sent in LightUpdateS2CPacket and stored by the network handler
        if (mc.getNetworkHandler() != null) {
            // world.getChunkManager().chunkLoadDistance is not directly exposed,
            // but the world stores the server-sent chunk load distance
            info.serverRenderDistance = mc.world.getSimulationDistance() > 0
                    ? mc.world.getSimulationDistance()
                    : info.chunkRenderDistance;
        }
    }
}
