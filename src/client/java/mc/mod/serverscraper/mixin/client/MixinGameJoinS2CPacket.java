package mc.mod.serverscraper.mixin.client;

import mc.mod.serverscraper.scraper.MasterScraper;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts the Game Join packet to grab render/simulation distance and
 * trigger the connect lifecycle.
 */
@Mixin(ClientPlayNetworkHandler.class)
public class MixinGameJoinS2CPacket {

    @Inject(
        method = "onGameJoin",
        at = @At("TAIL")
    )
    private void serverscraper$onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        // In 1.21.1 the accessor is viewDistance() (was chunkLoadDistance())
        MasterScraper.INFO.serverRenderDistance = packet.viewDistance();
        MasterScraper.INFO.simulationDistance   = packet.simulationDistance();
        MasterScraper.onConnect();
    }
}
