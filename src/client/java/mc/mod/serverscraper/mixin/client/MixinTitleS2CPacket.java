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
 * Captures title, subtitle, and action bar text from the server.
 */
@Mixin(value = ClientPlayNetworkHandler.class, priority = 900)
public class MixinTitleS2CPacket {

    @Inject(method = "onTitle", at = @At("HEAD"), require = 0)
    private void serverscraper$onTitle(TitleS2CPacket packet, CallbackInfo ci) {
        try { MasterScraper.INFO.lastTitle = packet.text().getString(); } catch (Exception ignored) {}
    }

    @Inject(method = "onSubtitle", at = @At("HEAD"), require = 0)
    private void serverscraper$onSubtitle(SubtitleS2CPacket packet, CallbackInfo ci) {
        try { MasterScraper.INFO.lastSubtitle = packet.text().getString(); } catch (Exception ignored) {}
    }

    @Inject(method = "onOverlayMessage", at = @At("HEAD"), require = 0)
    private void serverscraper$onActionBar(OverlayMessageS2CPacket packet, CallbackInfo ci) {
        try { MasterScraper.INFO.lastActionBar = packet.text().getString(); } catch (Exception ignored) {}
    }
}
