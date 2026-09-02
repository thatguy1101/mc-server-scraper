package mc.mod.serverscraper.data;

import java.time.Instant;
import java.util.*;

/**
 * Central data object holding everything the scraper has collected about the
 * current server session.  All fields are intentionally public for easy
 * read-access from renderers and exporters; writes go through the scraper
 * utilities so they stay coordinated.
 */
public class ServerInfo {

    // ── Connection ────────────────────────────────────────────────────────────
    public String serverAddress       = "N/A";
    public String resolvedIp          = "N/A";
    public int    serverPort          = -1;
    public String serverBrand         = "N/A";   // sent by the server via plugin channel
    public String serverVersion       = "N/A";
    public int    protocolVersion     = -1;
    public int    pingMs              = -1;
    public Instant joinTime           = Instant.now();
    public boolean isOnlineMode       = false;
    public boolean isSinglePlayer     = false;
    public boolean isRealmsServer     = false;
    public boolean isLanServer        = false;

    // ── World / Dimension ─────────────────────────────────────────────────────
    public String dimensionId         = "N/A";
    public String dimensionType       = "N/A";
    public long   worldSeed           = 0;        // only available in single-player
    public long   worldAgeRaw         = 0;        // ticks
    public long   worldTimeRaw        = 0;        // ticks (time of day)
    public String worldTimeFormatted  = "N/A";
    public String worldAgeFormatted   = "N/A";
    public long   dayNumber           = 0;
    public boolean isDaytime          = true;
    public boolean isRaining          = false;
    public boolean isThundering       = false;
    public float  rainLevel           = 0f;
    public float  thunderLevel        = 0f;
    public boolean isSuperFlat        = false;
    public boolean isDebugWorld       = false;
    public boolean hasFixedTime       = false;
    public boolean hasSkyLight        = true;
    public boolean hasCeiling         = false;
    public boolean isUltrawarm        = false;
    public boolean isNatural          = true;
    public double ambientLight        = 0.0;
    public int    logicalHeight       = 256;
    public int    minY                = 0;
    public int    height              = 256;
    public String infiniburnTag       = "N/A";
    public String effectsId           = "N/A";

    // ── Gamerules (as received) ───────────────────────────────────────────────
    public final Map<String, String> gamerules = new LinkedHashMap<>();

    // ── Player / Self ─────────────────────────────────────────────────────────
    public String  localPlayerName    = "N/A";
    public UUID    localPlayerUuid    = null;
    public String  localPlayerUuidStr = "N/A";
    public float   localPlayerHealth  = 0f;
    public float   localPlayerFood    = 0f;
    public float   localPlayerSaturation = 0f;
    public float   localPlayerArmor   = 0f;
    public int     localPlayerLevel   = 0;
    public float   localPlayerXp      = 0f;
    public int     localPlayerTotalXp = 0;
    public double  localPlayerX       = 0;
    public double  localPlayerY       = 0;
    public double  localPlayerZ       = 0;
    public float   localPlayerYaw     = 0;
    public float   localPlayerPitch   = 0;
    public int     localPlayerChunkX  = 0;
    public int     localPlayerChunkZ  = 0;
    public int     localPlayerRegionX = 0;
    public int     localPlayerRegionZ = 0;
    public String  localPlayerGamemode = "N/A";
    public boolean localPlayerCreative = false;
    public boolean localPlayerSpectator = false;
    public boolean localPlayerSurvival = false;
    public boolean localPlayerAdventure = false;
    public boolean localPlayerOp      = false;
    public int     localPlayerPermLevel = 0;
    public boolean localPlayerFlying  = false;
    public boolean localPlayerSneaking = false;
    public boolean localPlayerSprinting = false;
    public boolean localPlayerOnGround = false;
    public boolean localPlayerInWater = false;
    public boolean localPlayerInLava  = false;
    public float   localPlayerSpeed   = 0f;
    public String  localPlayerTeam    = "N/A";
    public String  localPlayerScoreboardName = "N/A";
    public int     localPlayerPing    = -1;

    // ── Online Players ────────────────────────────────────────────────────────
    public int     playerCount        = 0;
    public int     maxPlayerCount     = 0;
    public final List<PlayerEntry> onlinePlayers = new ArrayList<>();

    // ── Entities ──────────────────────────────────────────────────────────────
    public int     totalEntityCount   = 0;
    public int     playerEntityCount  = 0;
    public int     hostileEntityCount = 0;
    public int     passiveEntityCount = 0;
    public int     itemEntityCount    = 0;
    public final Map<String, Integer> entityTypeCounts = new LinkedHashMap<>();

    // ── Chunks ────────────────────────────────────────────────────────────────
    public int     loadedChunkCount   = 0;
    public int     chunkRenderDistance = 0;
    public int     serverRenderDistance = 0;
    public int     simulationDistance = 0;

    // ── Network / Performance ─────────────────────────────────────────────────
    public double  tps               = 20.0;    // estimated from time packets
    public double  mspt              = 50.0;    // ms per tick estimate
    public int     clientFps         = 0;
    public long    freeMemoryMb      = 0;
    public long    maxMemoryMb       = 0;
    public long    usedMemoryMb      = 0;
    public int     networkSentPackets   = 0;
    public int     networkRecvPackets   = 0;
    public long    networkSentBytes     = 0;
    public long    networkRecvBytes     = 0;

    // ── Scoreboard ────────────────────────────────────────────────────────────
    public final List<String> scoreboardLines  = new ArrayList<>();
    public String  scoreboardTitle   = "N/A";

    // ── Boss Bars ─────────────────────────────────────────────────────────────
    public final List<BossBarEntry> bossBars = new ArrayList<>();

    // ── Server-sent subtitles / actionbar ────────────────────────────────────
    public String  lastActionBar     = "";
    public String  lastTitle         = "";
    public String  lastSubtitle      = "";

    // ── Plugin channels ──────────────────────────────────────────────────────
    public final Set<String> pluginChannels = new LinkedHashSet<>();

    // ── Resource packs ────────────────────────────────────────────────────────
    public final List<String> serverResourcePacks = new ArrayList<>();

    // ── Detected plugins/mods (from brand / channels heuristics) ─────────────
    public final Set<String> detectedServerSoftware = new LinkedHashSet<>();

    // ── Timestamps ────────────────────────────────────────────────────────────
    public Instant lastRefreshed     = Instant.now();

    // ─────────────────────────────────────────────────────────────────────────
    // Inner record types
    // ─────────────────────────────────────────────────────────────────────────

    public static class PlayerEntry {
        public final String name;
        public final UUID   uuid;
        public final int    latencyMs;
        public final String gamemode;
        public final String displayName;
        public final String team;

        public PlayerEntry(String name, UUID uuid, int latencyMs, String gamemode, String displayName, String team) {
            this.name        = name;
            this.uuid        = uuid;
            this.latencyMs   = latencyMs;
            this.gamemode    = gamemode;
            this.displayName = displayName;
            this.team        = team;
        }
    }

    public static class BossBarEntry {
        public final String name;
        public final float  progress;
        public final String color;
        public final String overlay;

        public BossBarEntry(String name, float progress, String color, String overlay) {
            this.name     = name;
            this.progress = progress;
            this.color    = color;
            this.overlay  = overlay;
        }
    }

    /** Reset all fields back to defaults (called on disconnect). */
    public void reset() {
        serverAddress       = "N/A";
        resolvedIp          = "N/A";
        serverPort          = -1;
        serverBrand         = "N/A";
        serverVersion       = "N/A";
        protocolVersion     = -1;
        pingMs              = -1;
        joinTime            = Instant.now();
        isOnlineMode        = false;
        isSinglePlayer      = false;
        isRealmsServer      = false;
        isLanServer         = false;
        dimensionId         = "N/A";
        dimensionType       = "N/A";
        worldSeed           = 0;
        worldAgeRaw         = 0;
        worldTimeRaw        = 0;
        worldTimeFormatted  = "N/A";
        worldAgeFormatted   = "N/A";
        dayNumber           = 0;
        isDaytime           = true;
        isRaining           = false;
        isThundering        = false;
        rainLevel           = 0f;
        thunderLevel        = 0f;
        isSuperFlat         = false;
        isDebugWorld        = false;
        hasFixedTime        = false;
        hasSkyLight         = true;
        hasCeiling          = false;
        isUltrawarm         = false;
        isNatural           = true;
        ambientLight        = 0.0;
        logicalHeight       = 256;
        minY                = 0;
        height              = 256;
        infiniburnTag       = "N/A";
        effectsId           = "N/A";
        gamerules.clear();
        localPlayerName     = "N/A";
        localPlayerUuid     = null;
        localPlayerUuidStr  = "N/A";
        localPlayerHealth   = 0f;
        localPlayerFood     = 0f;
        localPlayerSaturation = 0f;
        localPlayerArmor    = 0f;
        localPlayerLevel    = 0;
        localPlayerXp       = 0f;
        localPlayerTotalXp  = 0;
        localPlayerX        = 0;
        localPlayerY        = 0;
        localPlayerZ        = 0;
        localPlayerYaw      = 0;
        localPlayerPitch    = 0;
        localPlayerChunkX   = 0;
        localPlayerChunkZ   = 0;
        localPlayerRegionX  = 0;
        localPlayerRegionZ  = 0;
        localPlayerGamemode = "N/A";
        localPlayerCreative = false;
        localPlayerSpectator = false;
        localPlayerSurvival = false;
        localPlayerAdventure = false;
        localPlayerOp       = false;
        localPlayerPermLevel = 0;
        localPlayerFlying   = false;
        localPlayerSneaking = false;
        localPlayerSprinting = false;
        localPlayerOnGround = false;
        localPlayerInWater  = false;
        localPlayerInLava   = false;
        localPlayerSpeed    = 0f;
        localPlayerTeam     = "N/A";
        localPlayerScoreboardName = "N/A";
        localPlayerPing     = -1;
        playerCount         = 0;
        maxPlayerCount      = 0;
        onlinePlayers.clear();
        totalEntityCount    = 0;
        playerEntityCount   = 0;
        hostileEntityCount  = 0;
        passiveEntityCount  = 0;
        itemEntityCount     = 0;
        entityTypeCounts.clear();
        loadedChunkCount    = 0;
        chunkRenderDistance = 0;
        serverRenderDistance = 0;
        simulationDistance  = 0;
        tps                 = 20.0;
        mspt                = 50.0;
        clientFps           = 0;
        freeMemoryMb        = 0;
        maxMemoryMb         = 0;
        usedMemoryMb        = 0;
        networkSentPackets  = 0;
        networkRecvPackets  = 0;
        networkSentBytes    = 0;
        networkRecvBytes    = 0;
        scoreboardLines.clear();
        scoreboardTitle     = "N/A";
        bossBars.clear();
        lastActionBar       = "";
        lastTitle           = "";
        lastSubtitle        = "";
        pluginChannels.clear();
        serverResourcePacks.clear();
        detectedServerSoftware.clear();
        lastRefreshed       = Instant.now();
    }
}
