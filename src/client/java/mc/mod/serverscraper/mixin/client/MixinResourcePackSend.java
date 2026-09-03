package mc.mod.serverscraper.mixin.client;

import mc.mod.serverscraper.scraper.MasterScraper;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Records server-sent resource pack URLs/hashes for inspection.
 */
@Mixin(value = ClientCommonNetworkHandler.class, priority = 900)
public class MixinResourcePackSend {

    @Inject(method = "onResourcePackSend", at = @At("HEAD"), require = 0)
    private void serverscraper$onResourcePack(ResourcePackSendS2CPacket packet, CallbackInfo ci) {
        String entry = packet.url() + "  [" + packet.hash() + "]"
                + (packet.required() ? "  (required)" : "  (optional)");
        if (!MasterScraper.INFO.serverResourcePacks.contains(entry)) {
            MasterScraper.INFO.serverResourcePacks.add(entry);
        }
    }
}
