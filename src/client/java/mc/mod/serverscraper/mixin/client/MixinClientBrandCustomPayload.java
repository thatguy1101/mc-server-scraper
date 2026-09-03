package mc.mod.serverscraper.mixin.client;

import mc.mod.serverscraper.scraper.ConnectionScraper;
import mc.mod.serverscraper.scraper.MasterScraper;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.packet.BrandCustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts custom payload packets to capture server brand and plugin channels.
 */
@Mixin(value = ClientCommonNetworkHandler.class, priority = 900)
public class MixinClientBrandCustomPayload {

    @Inject(method = "onCustomPayload", at = @At("HEAD"), require = 0)
    private void serverscraper$onCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        try {
            if (packet.payload() instanceof BrandCustomPayload brandPayload) {
                ConnectionScraper.onBrandReceived(MasterScraper.INFO, brandPayload.brand());
            }
            String channelId = packet.payload().getId().id().toString();
            MasterScraper.INFO.pluginChannels.add(channelId);
        } catch (Exception ignored) {}
    }
}
