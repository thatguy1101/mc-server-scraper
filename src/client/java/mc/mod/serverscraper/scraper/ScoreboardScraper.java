package mc.mod.serverscraper.scraper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mc.mod.serverscraper.data.ServerInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;

/**
 * Reads the sidebar scoreboard currently displayed to the player.
 */
@Environment(EnvType.CLIENT)
public class ScoreboardScraper {

    public static void scrape(ServerInfo info) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.world == null) return;

        Scoreboard scoreboard = mc.world.getScoreboard();

        // Get the sidebar objective (slot SIDEBAR)
        ScoreboardObjective sidebar = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (sidebar == null) {
            info.scoreboardTitle = "N/A";
            info.scoreboardLines.clear();
            return;
        }

        info.scoreboardTitle = sidebar.getDisplayName().getString();
        List<String> lines = new ArrayList<>();

        Collection<ScoreboardEntry> scores = scoreboard.getScoreboardEntries(sidebar);
        // Sort by value descending (normal sidebar order)
        scores.stream()
                .sorted((a, b) -> Integer.compare(b.value(), a.value()))
                .limit(15)
                .forEach(score -> {
                    // In 1.21.11, getPlayerTeam() is now getScoreHolderTeam()
                    Team team = scoreboard.getScoreHolderTeam(score.owner());
                    String display = team != null
                            ? Team.decorateName(team, net.minecraft.text.Text.literal(score.owner())).getString()
                            : score.owner();
                    lines.add(display + ": " + score.value());
                });

        info.scoreboardLines.clear();
        info.scoreboardLines.addAll(lines);
    }
}
