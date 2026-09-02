package mc.mod.serverscraper.mixin.client;

import mc.mod.serverscraper.scraper.ConnectionScraper;
import mc.mod.serverscraper.scraper.MasterScraper;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.packet.BrandCustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts the brand custom payload packet to capture the server's brand
 * string (e.g. "Paper", "vanilla", "Fabric").
 */
@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientBrandCustomPayload {

    @Inject(
        method = "onCustomPayload",
        at = @At("HEAD")
    )
    private void serverscraper$onCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        if (packet.payload() instanceof BrandCustomPayload brandPayload) {
            ConnectionScraper.onBrandReceived(MasterScraper.INFO, brandPayload.brand());
        }
        // Track all channel identifiers for plugin detection
        String channelId = packet.payload().getId().id().toString();
        MasterScraper.INFO.pluginChannels.add(channelId);
    }
}
