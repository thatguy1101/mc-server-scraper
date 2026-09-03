package mc.mod.serverscraper.mixin.client;

import mc.mod.serverscraper.scraper.NetworkScraper;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Counts outgoing packets. Uses require=0 so a missing method won't crash the game.
 */
@Mixin(value = ClientConnection.class, priority = 900)
public class MixinClientConnection {

    @Inject(
        method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V",
        at = @At("HEAD"),
        require = 0
    )
    private void serverscraper$onSend(Packet<?> packet, PacketCallbacks callbacks, CallbackInfo ci) {
        NetworkScraper.sentPackets.incrementAndGet();
    }
}
