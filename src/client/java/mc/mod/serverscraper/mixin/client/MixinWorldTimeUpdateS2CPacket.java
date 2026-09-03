package mc.mod.serverscraper.mixin.client;

import mc.mod.serverscraper.scraper.TpsTracker;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts every WorldTimeUpdateS2CPacket so TpsTracker can timestamp them
 * and compute a real TPS estimate. The server sends this once per game tick.
 */
@Mixin(value = ClientPlayNetworkHandler.class, priority = 900)
public class MixinWorldTimeUpdateS2CPacket {

    @Inject(method = "onWorldTimeUpdate", at = @At("HEAD"), require = 0)
    private void serverscraper$onTimeUpdate(WorldTimeUpdateS2CPacket packet, CallbackInfo ci) {
        TpsTracker.onTimePacket();
    }
}
