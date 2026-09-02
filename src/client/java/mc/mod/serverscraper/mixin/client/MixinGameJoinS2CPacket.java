package mc.mod.serverscraper.mixin.client;

import mc.mod.serverscraper.scraper.MasterScraper;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts the Game Join packet (sent once on join) to grab:
 * - server render distance
 * - simulation distance
 * - online mode flag
 * - reduced debug info flag
 * - respawn screen flag
 * - limited crafting flag
 */
@Mixin(ClientPlayNetworkHandler.class)
public class MixinGameJoinS2CPacket {

    @Inject(
        method = "onGameJoin",
        at = @At("TAIL")
    )
    private void serverscraper$onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        MasterScraper.INFO.serverRenderDistance  = packet.chunkLoadDistance();
        MasterScraper.INFO.simulationDistance    = packet.simulationDistance();
        // Trigger a full connect lifecycle
        MasterScraper.onConnect();
    }
}
