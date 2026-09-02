package mc.mod.serverscraper.scraper;

import mc.mod.serverscraper.data.ServerInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

/**
 * Reads active boss bars from the BossBarHud.
 */
@Environment(EnvType.CLIENT)
public class BossBarScraper {

    // Field name from yarn mappings: bossBars (Map<UUID, ClientBossBar>)
    private static Field bossBarField = null;

    @SuppressWarnings("unchecked")
    public static void scrape(ServerInfo info) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.inGameHud == null) return;

        BossBarHud hud = mc.inGameHud.getBossBarHud();
        info.bossBars.clear();

        try {
            if (bossBarField == null) {
                bossBarField = BossBarHud.class.getDeclaredField("bossBars");
                bossBarField.setAccessible(true);
            }
            Map<UUID, ClientBossBar> bars =
                    (Map<UUID, ClientBossBar>) bossBarField.get(hud);

            for (ClientBossBar bar : bars.values()) {
                String name    = bar.getName().getString();
                float  pct     = bar.getPercent();
                String color   = bar.getColor().getName();
                String overlay = bar.getStyle().getName();
                info.bossBars.add(new ServerInfo.BossBarEntry(name, pct, color, overlay));
            }
        } catch (Exception e) {
            // reflection failed — skip silently
        }
    }
}
