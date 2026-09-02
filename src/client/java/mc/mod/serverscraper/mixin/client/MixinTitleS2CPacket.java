package mc.mod.serverscraper.mixin.client;

import mc.mod.serverscraper.scraper.MasterScraper;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Captures titles, subtitles, and action bar messages sent by the server.
 */
@Mixin(ClientPlayNetworkHandler.class)
public class MixinTitleS2CPacket {

    @Inject(method = "onTitle",    at = @At("HEAD"))
    private void serverscraper$onTitle(TitleS2CPacket packet, CallbackInfo ci) {
        MasterScraper.INFO.lastTitle = packet.title().getString();
    }

    @Inject(method = "onSubtitle", at = @At("HEAD"))
    private void serverscraper$onSubtitle(SubtitleS2CPacket packet, CallbackInfo ci) {
        MasterScraper.INFO.lastSubtitle = packet.subtitle().getString();
    }

    @Inject(method = "onOverlayMessage", at = @At("HEAD"))
    private void serverscraper$onActionBar(OverlayMessageS2CPacket packet, CallbackInfo ci) {
        MasterScraper.INFO.lastActionBar = packet.message().getString();
    }
}
