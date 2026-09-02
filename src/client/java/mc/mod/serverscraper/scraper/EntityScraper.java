package mc.mod.serverscraper.scraper;

import mc.mod.serverscraper.data.ServerInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Counts and categorises all loaded entities in the current world chunk cache.
 */
@Environment(EnvType.CLIENT)
public class EntityScraper {

    public static void scrape(ServerInfo info) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.world == null) return;

        int total    = 0;
        int players  = 0;
        int hostile  = 0;
        int passive  = 0;
        int items    = 0;

        Map<String, Integer> typeCounts = new LinkedHashMap<>();

        for (Entity entity : mc.world.getEntities()) {
            total++;

            // Type count
            String typeId = Registries.ENTITY_TYPE.getId(entity.getType()).toString();
            typeCounts.merge(typeId, 1, Integer::sum);

            if (entity instanceof PlayerEntity)      players++;
            else if (entity instanceof HostileEntity) hostile++;
            else if (entity instanceof AnimalEntity)  passive++;
            else if (entity instanceof ItemEntity)    items++;
        }

        info.totalEntityCount   = total;
        info.playerEntityCount  = players;
        info.hostileEntityCount = hostile;
        info.passiveEntityCount = passive;
        info.itemEntityCount    = items;

        // Sort by count descending for readable output
        info.entityTypeCounts.clear();
        typeCounts.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .forEach(e -> info.entityTypeCounts.put(e.getKey(), e.getValue()));
    }
}
