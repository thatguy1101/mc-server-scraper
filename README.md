# ServerScraper

A **client-side only** Fabric mod for Minecraft 1.21.1 that gives you a deep,
real-time view of everything happening on any server you join — without sending
a single extra packet to the server.

---

## Features

### In-Game HUD Overlay
- Toggleable, positionable (top-left / top-right / bottom-left / bottom-right)
- Scalable (0.5× – 3.0×), with optional semi-transparent background
- Compact mode (dense single-line sections) or full mode
- Individual section toggles: Connection · World · Player · Players · Entities · Performance · Scoreboard
- Auto-hides during F3 debug screen and open GUIs

### Connection Info
- Server address, resolved IP, and port
- Server brand string (Paper, Purpur, Fabric, Vanilla, etc.)
- Automatic server software detection (heuristic from brand + plugin channels)
- Minecraft version and protocol version
- Online-mode detection, LAN server detection
- Plugin channel enumeration
- Server resource pack URL and hash logging

### World & Dimension
- Current dimension ID and friendly type name
- Full in-game time of day (12-hour clock + tick value)
- Day number and formatted world age (days/hours/minutes/seconds)
- Weather: rain, thunder, gradient levels
- Dimension flags: sky light, ceiling, ultrawarm, natural, fixed time, superflat, debug world
- Build limits: min Y, height, logical height, ambient light
- Effects ID and infiniburn tag

### Gamerules
- Full server gamerule dump (every rule and its current value)

### Local Player
- Name, UUID, gamemode, permission level / op status
- Health, food, saturation, armor points
- XP level, progress %, total XP
- Precise XYZ coordinates, yaw/pitch
- Chunk coordinates (X/Z) and region file name (r.X.Z.mca)
- Nether ↔ Overworld portal coordinate conversion in `/ss coords`
- Movement speed (blocks/tick), flying, sneaking, sprinting, on-ground, in-water, in-lava flags
- Scoreboard team

### Online Players (Tab List)
- Full player roster with names, UUIDs, gamemodes, and latencies
- Team membership
- Sorted alphabetically for stable display

### Entities
- Total loaded entity count, broken down by: Players · Hostile · Passive · Items · Other
- Per-type counts sorted by frequency (top 15 shown in HUD, full list in export)

### Chunks & Distances
- Loaded chunk count (from client chunk manager)
- Client render distance, server-reported render distance, simulation distance

### Performance
- **TPS estimation** — sliding 100-sample window over `WorldTimeUpdate` packet intervals; no server access needed
- MSPT (milliseconds per tick) derived from the same window
- Client FPS
- JVM heap: used / max / free (MB)
- Network traffic totals: packets sent/received, bytes sent/received

### Scoreboard & Boss Bars
- Active sidebar scoreboard title and lines
- All active boss bars: name, progress %, color, overlay style

### Server Messages
- Last title, subtitle, and action-bar text captured from packets

### Alerts
- Player join / leave notifications (chat message, configurable)
- Low-TPS threshold alert (configurable threshold, auto-clears on recovery)

### Export
Three export formats, accessible via `/ss export` or auto-triggered on join/leave:

| Format | Description |
|--------|-------------|
| `TXT`  | Human-readable, fully formatted report |
| `JSON` | Machine-readable, complete nested object |
| `CSV`  | Flat `category,key,value` rows — paste into Excel/Sheets |

Output goes to `.minecraft/scraper_exports/` by default (configurable).
Filenames include the server address and a timestamp.
The export path appears as a **clickable link** in chat after export.

---

## Commands

All commands are **client-side only** — none are sent to the server.
Use `/scraper` or the short alias `/ss`.

```
/ss                         — Full info dump (all sections)
/ss info [section]          — Section: connection | world | player | players |
                              entities | chunks | performance | gamerules |
                              scoreboard | bossbars | network | plugins
/ss refresh                 — Force immediate data refresh
/ss export [txt|json|csv]   — Export data to file

/ss players                 — List all tab-list players
/ss gamerules               — Dump all gamerules
/ss coords                  — Your coords + portal calc + region file
/ss ping                    — Ping + server info
/ss tps                     — TPS & MSPT (colour-coded)
/ss time                    — World time, day, age
/ss weather                 — Current weather
/ss entities                — Entity counts by type
/ss channels                — Plugin channels
/ss brand                   — Server software detection
/ss seed                    — World seed (singleplayer only)
/ss network                 — Packet & byte counters
/ss reset                   — Reset collected data

/ss hud toggle              — Toggle HUD on/off
/ss hud compact             — Toggle compact/full mode
/ss hud pos <position>      — TOP_LEFT | TOP_RIGHT | BOTTOM_LEFT | BOTTOM_RIGHT
/ss hud scale <n>           — Scale factor (0.5 – 3.0)
/ss hud show <section>      — Show a HUD section
/ss hud hide <section>      — Hide a HUD section
/ss hud sections            — List section visibility

/ss config reload           — Reload config from disk
/ss config save             — Save config to disk
/ss config autojoin         — Toggle auto-export on server join
/ss config autoleave        — Toggle auto-export on server leave
/ss config logfile          — Toggle session log file
/ss config alertjoin        — Toggle player-join alerts
/ss config alertleave       — Toggle player-leave alerts
/ss config alerttps         — Toggle low-TPS alerts
/ss config interval <ticks> — Set refresh interval (1–200 ticks)

/ss help                    — Show command list
```

---

## Configuration

Config file: `.minecraft/config/serverscraper.json`

Edited automatically by `/ss config` commands or manually.
Use `/ss config reload` after editing by hand.

Key options:

```json
{
  "hudEnabled": true,
  "hudPosition": "TOP_LEFT",
  "hudOffsetX": 4,
  "hudOffsetY": 4,
  "hudScale": 1.0,
  "hudBackground": true,
  "hudCompact": false,
  "autoExportOnJoin": false,
  "autoExportOnLeave": false,
  "exportFormat": "TXT",
  "exportDirectory": "scraper_exports",
  "refreshIntervalTicks": 20,
  "alertOnPlayerJoin": false,
  "alertOnPlayerLeave": false,
  "alertLowTps": false,
  "alertLowTpsThreshold": 15.0
}
```

---

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.1
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Drop `serverscraper-<version>.jar` into your `.minecraft/mods/` folder
4. Launch the game

---

## Building from source

Requires JDK 21.

```bash
git clone https://github.com/Max_W/ServerScraper.git
cd ServerScraper
./gradlew build
```

The built jar will be at `build/libs/serverscraper-<version>.jar`.

---

## How it works

ServerScraper never sends data to the server. Everything is collected from:

- **Packets the server already sends** (game join, world time, player list, custom payload, etc.) — intercepted via Mixin
- **Client-side game state** (entities, chunks, scoreboard, boss bars, player stats) — read directly from Minecraft's own objects every N ticks
- **Client JVM metrics** — Java `Runtime` for memory figures

TPS is estimated by measuring the wall-clock interval between consecutive
`WorldTimeUpdateS2CPacket` arrivals, averaged over a 100-sample sliding window.
The server sends this packet once per game tick, so the gap directly encodes tick duration.

---

## Privacy & Ethics

This mod is intended for legitimate use: server admins inspecting their own
servers, players diagnosing lag, or developers testing mods.

Export files may contain your username, UUID, and server IP.
They are saved locally only — the mod makes no outbound network requests.

---

## License

MIT — see [LICENSE.txt](LICENSE.txt)
