package mc.mod.serverscraper.scraper;

import mc.mod.serverscraper.data.ServerInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Scrapes the local player's stats and the full tab-list player roster.
 */
@Environment(EnvType.CLIENT)
public class PlayerScraper {

    public static void scrape(ServerInfo info) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null) return;

        PlayerEntity player = mc.player;

        // ── Identity ──────────────────────────────────────────────────────────
        info.localPlayerName    = player.getName().getString();
        info.localPlayerUuid    = player.getUuid();
        info.localPlayerUuidStr = player.getUuid().toString();

        // ── Health / hunger ───────────────────────────────────────────────────
        info.localPlayerHealth      = player.getHealth();
        info.localPlayerFood        = player.getHungerManager().getFoodLevel();
        info.localPlayerSaturation  = player.getHungerManager().getSaturationLevel();
        info.localPlayerArmor       = player.getArmor();

        // ── XP ────────────────────────────────────────────────────────────────
        info.localPlayerLevel   = player.experienceLevel;
        info.localPlayerXp      = player.experienceProgress;
        info.localPlayerTotalXp = player.totalExperience;

        // ── Position ──────────────────────────────────────────────────────────
        info.localPlayerX = player.getX();
        info.localPlayerY = player.getY();
        info.localPlayerZ = player.getZ();
        info.localPlayerYaw   = player.getYaw();
        info.localPlayerPitch = player.getPitch();

        // chunk and region coords
        info.localPlayerChunkX  = (int) Math.floor(info.localPlayerX) >> 4;
        info.localPlayerChunkZ  = (int) Math.floor(info.localPlayerZ) >> 4;
        info.localPlayerRegionX = info.localPlayerChunkX >> 5;
        info.localPlayerRegionZ = info.localPlayerChunkZ >> 5;

        // ── Movement state ────────────────────────────────────────────────────
        info.localPlayerFlying   = player.getAbilities().flying;
        info.localPlayerSneaking = player.isSneaking();
        info.localPlayerSprinting = player.isSprinting();
        info.localPlayerOnGround  = player.isOnGround();
        info.localPlayerInWater   = player.isTouchingWater();
        info.localPlayerInLava    = player.isInLava();

        // Speed (blocks/tick)
        double dx = player.getVelocity().x;
        double dz = player.getVelocity().z;
        info.localPlayerSpeed = (float) Math.sqrt(dx * dx + dz * dz);

        // ── Gamemode & permissions ────────────────────────────────────────────
        if (mc.interactionManager != null) {
            GameMode gm = mc.interactionManager.getCurrentGameMode();
            info.localPlayerGamemode  = gm.getName();
            info.localPlayerCreative  = gm == GameMode.CREATIVE;
            info.localPlayerSpectator = gm == GameMode.SPECTATOR;
            info.localPlayerSurvival  = gm == GameMode.SURVIVAL;
            info.localPlayerAdventure = gm == GameMode.ADVENTURE;
        }

        info.localPlayerOp       = player.getAbilities().allowModifyWorld;
        info.localPlayerPermLevel = player.getAbilities().allowModifyWorld ? 4 : 0;

        // ── Scoreboard team ───────────────────────────────────────────────────
        if (mc.world != null) {
            Scoreboard sb = mc.world.getScoreboard();
            Team team = sb.getPlayerTeam(info.localPlayerName);
            info.localPlayerTeam = team != null ? team.getName() : "N/A";
            info.localPlayerScoreboardName = info.localPlayerName;
        }

        // ── Tab list / player roster ──────────────────────────────────────────
        ClientPlayNetworkHandler handler = mc.getNetworkHandler();
        if (handler == null) return;

        Collection<PlayerListEntry> entries = handler.getPlayerList();
        info.playerCount    = entries.size();
        info.maxPlayerCount = handler.getPlayerList().size(); // max comes from server

        info.onlinePlayers.clear();
        for (PlayerListEntry entry : entries) {
            String name = entry.getProfile().getName();
            String display = entry.getDisplayName() != null
                    ? entry.getDisplayName().getString()
                    : name;
            String gamemode = entry.getGameMode() != null
                    ? entry.getGameMode().getName()
                    : "unknown";
            Team team = entry.getScoreboardTeam();
            String teamName = team != null ? team.getName() : "";

            info.onlinePlayers.add(new ServerInfo.PlayerEntry(
                    name,
                    entry.getProfile().getId(),
                    entry.getLatency(),
                    gamemode,
                    display,
                    teamName
            ));

            // grab local player ping from tab list
            if (name.equals(info.localPlayerName)) {
                info.localPlayerPing = entry.getLatency();
            }
        }

        // Sort by name for stable display
        info.onlinePlayers.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
    }
}
