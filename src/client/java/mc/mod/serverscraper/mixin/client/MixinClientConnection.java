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
 * Intercepts outgoing and incoming packets on the ClientConnection to update
 * the NetworkScraper's packet and byte counters.
 *
 * NOTE: Byte counting is approximate — we increment by 1 per packet for
 * packets where we can't easily get the wire size from this injection point.
 * The packet count is exact.
 */
@Mixin(ClientConnection.class)
public class MixinClientConnection {

    // Outgoing
    @Inject(
        method = "sendImmediately",
        at = @At("HEAD")
    )
    private void serverscraper$onSend(Packet<?> packet, PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
        NetworkScraper.sentPackets.incrementAndGet();
    }

    // Incoming — channelRead0 is the Netty handler entry point for received packets
    @Inject(
        method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V",
        at = @At("HEAD")
    )
    private void serverscraper$onReceive(
            io.netty.channel.ChannelHandlerContext ctx,
            Packet<?> packet,
            CallbackInfo ci) {
        NetworkScraper.recvPackets.incrementAndGet();
    }
}
