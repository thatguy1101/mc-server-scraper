package mc.mod.serverscraper.mixin.client;

import mc.mod.serverscraper.scraper.MasterScraper;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * When the player respawns / changes dimension, re-trigger a scrape refresh
 * so dimension info and world flags stay current.
 */
@Mixin(ClientPlayNetworkHandler.class)
public class MixinPlayerRespawn {

    @Inject(
        method = "onPlayerRespawn",
        at = @At("TAIL")
    )
    private void serverscraper$onRespawn(PlayerRespawnS2CPacket packet, CallbackInfo ci) {
        // schedule a refresh on the next tick rather than doing it mid-packet-handling
        MasterScraper.INFO.dimensionId = packet.commonPlayerSpawnInfo().dimension().getValue().toString();
    }
}
