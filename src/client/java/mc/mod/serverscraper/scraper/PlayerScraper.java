package mc.mod.serverscraper.scraper;

import java.util.Collection;

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
        info.localPlayerFlying    = player.getAbilities().flying;
        info.localPlayerSneaking  = player.isSneaking();
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
            // In 1.21.1, GameMode#asString() replaces the old getName()
            info.localPlayerGamemode  = gm.asString();
            info.localPlayerCreative  = gm == GameMode.CREATIVE;
            info.localPlayerSpectator = gm == GameMode.SPECTATOR;
            info.localPlayerSurvival  = gm == GameMode.SURVIVAL;
            info.localPlayerAdventure = gm == GameMode.ADVENTURE;
        }

        info.localPlayerOp        = player.getAbilities().allowModifyWorld;
        info.localPlayerPermLevel = player.getAbilities().allowModifyWorld ? 4 : 0;

        // ── Scoreboard team ───────────────────────────────────────────────────
        if (mc.world != null) {
            Scoreboard sb = mc.world.getScoreboard();
            // In 1.21.11, getPlayerTeam() was renamed to getScoreHolderTeam()
            Team team = sb.getScoreHolderTeam(info.localPlayerName);
            info.localPlayerTeam = team != null ? team.getName() : "N/A";
            info.localPlayerScoreboardName = info.localPlayerName;
        }

        // ── Tab list / player roster ──────────────────────────────────────────
        ClientPlayNetworkHandler handler = mc.getNetworkHandler();
        if (handler == null) return;

        Collection<PlayerListEntry> entries = handler.getPlayerList();
        info.playerCount = entries.size();

        info.onlinePlayers.clear();
        for (PlayerListEntry entry : entries) {
            // In authlib used by 1.21.11: GameProfile uses .name() and .id() (not getName/getId)
            com.mojang.authlib.GameProfile profile = entry.getProfile();
            String name    = profile.name();
            String display = entry.getDisplayName() != null
                    ? entry.getDisplayName().getString()
                    : name;
            // GameMode#asString() replaces old getName()
            String gamemode = entry.getGameMode() != null
                    ? entry.getGameMode().asString()
                    : "unknown";
            Team team = entry.getScoreboardTeam();
            String teamName = team != null ? team.getName() : "";

            info.onlinePlayers.add(new ServerInfo.PlayerEntry(
                    name,
                    profile.id(),
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
