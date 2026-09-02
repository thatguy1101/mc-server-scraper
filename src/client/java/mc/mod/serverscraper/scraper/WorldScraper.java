package mc.mod.serverscraper.scraper;

import mc.mod.serverscraper.data.ServerInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.DimensionType;

/**
 * Scrapes world/dimension data: dimension, time, weather, gamerules, world flags.
 */
@Environment(EnvType.CLIENT)
public class WorldScraper {

    public static void scrape(ServerInfo info) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.world == null) return;

        ClientWorld world = mc.world;

        // ── Dimension ─────────────────────────────────────────────────────────
        RegistryKey<net.minecraft.world.World> dimKey = world.getRegistryKey();
        info.dimensionId = dimKey.getValue().toString();

        DimensionType dimType = world.getDimension();
        info.hasSkyLight   = dimType.hasSkyLight();
        info.hasCeiling    = dimType.hasCeiling();
        info.isUltrawarm   = dimType.ultrawarm();
        info.isNatural     = dimType.natural();
        info.ambientLight  = dimType.ambientLight();
        info.logicalHeight = dimType.logicalHeight();
        info.minY          = dimType.minY();
        info.height        = dimType.height();
        info.hasFixedTime  = dimType.fixedTime().isPresent();
        info.infiniburnTag = dimType.infiniburn().id().toString();
        info.effectsId     = dimType.effects().toString();

        // Friendly dimension name
        String dimId = info.dimensionId;
        info.dimensionType = switch (dimId) {
            case "minecraft:overworld" -> "Overworld";
            case "minecraft:the_nether" -> "The Nether";
            case "minecraft:the_end" -> "The End";
            default -> dimId;
        };

        // ── Time ──────────────────────────────────────────────────────────────
        info.worldAgeRaw       = world.getTime();
        info.worldTimeRaw      = world.getTimeOfDay();
        info.dayNumber         = info.worldAgeRaw / 24000L;
        info.worldAgeFormatted = formatAge(info.worldAgeRaw);
        info.worldTimeFormatted = formatTimeOfDay(info.worldTimeRaw);
        long timeOfDay = info.worldTimeRaw % 24000L;
        info.isDaytime = timeOfDay >= 0 && timeOfDay < 13000;

        // ── Weather ───────────────────────────────────────────────────────────
        info.isRaining     = world.isRaining();
        info.isThundering  = world.isThundering();
        info.rainLevel     = world.getRainGradient(1.0f);
        info.thunderLevel  = world.getThunderGradient(1.0f);

        // ── World type flags ──────────────────────────────────────────────────
        info.isSuperFlat  = world.getLevelProperties().isFlatWorld();
        info.isDebugWorld = world.getLevelProperties().isDebugWorld();

        // ── Gamerules ─────────────────────────────────────────────────────────
        info.gamerules.clear();
        GameRules rules = world.getGameRules();
        GameRules.accept(new GameRules.Visitor() {
            @Override
            public <T extends GameRules.Rule<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                info.gamerules.put(key.getName(), rules.get(key).toString());
            }
        });
    }

    private static String formatTimeOfDay(long ticks) {
        long t = ((ticks % 24000L) + 24000L) % 24000L;
        // MC time 0 = 6:00 AM
        long totalMinutes = (t * 1440L) / 24000L;
        long hours   = (totalMinutes / 60 + 6) % 24;
        long minutes = totalMinutes % 60;
        String ampm  = hours < 12 ? "AM" : "PM";
        long h12     = hours % 12;
        if (h12 == 0) h12 = 12;
        return String.format("%d:%02d %s (tick %d)", h12, minutes, ampm, t);
    }

    private static String formatAge(long ticks) {
        long seconds = ticks / 20;
        long minutes = seconds / 60;
        long hours   = minutes / 60;
        long days    = hours   / 24;
        return String.format("%dd %02dh %02dm %02ds", days, hours % 24, minutes % 60, seconds % 60);
    }
}
