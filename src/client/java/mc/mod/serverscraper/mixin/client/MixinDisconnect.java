package mc.mod.serverscraper.mixin.client;

import mc.mod.serverscraper.config.ScraperConfig;
import mc.mod.serverscraper.export.DataExporter;
import mc.mod.serverscraper.scraper.MasterScraper;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fires auto-export on leave and signals disconnect to MasterScraper.
 */
@Mixin(ClientPlayNetworkHandler.class)
public class MixinDisconnect {

    @Inject(
        method = "onDisconnect",
        at = @At("HEAD")
    )
    private void serverscraper$onDisconnect(DisconnectionInfo info, CallbackInfo ci) {
        handleLeave();
    }

    private static void handleLeave() {
        ScraperConfig cfg = ScraperConfig.get();
        if (cfg.autoExportOnLeave && MasterScraper.isConnected()) {
            try {
                DataExporter.export(MasterScraper.INFO, cfg.exportFormat);
            } catch (Exception ignored) {}
        }
        MasterScraper.onDisconnect();
    }
}
