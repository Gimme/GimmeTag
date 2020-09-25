package me.gimme.gimmetag.tag;

import me.gimme.gimmecore.util.ChatTableBuilder;
import me.gimme.gimmecore.util.TableBuilder;
import me.gimme.gimmetag.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Keeps and displays the scores of players in an active round of tag.
 */
class TagScoreboard implements Listener {

    private static final String OBJECTIVE_NAME = "tag-game";
    private static final String OBJECTIVE_DISPLAyNAME = "Score";
    private static final String OBJECTIVE_CRITERIA = "dummy";

    private static final String HUNTERS_TEAM_NAME = "Hunters";
    private static final ChatColor HUNTERS_TEAM_COLOR = ChatColor.RED;
    private static final String RUNNERS_TEAM_NAME = "Runners";
    private static final ChatColor RUNNERS_TEAM_COLOR = ChatColor.BLUE;
    private static final boolean RUNNERS_FRIENDLY_FIRE = false; // TODO

    private Server server;

    private Scoreboard scoreboard;
    private Objective objective;
    private Team huntersTeam;
    private Team runnersTeam;

    private Map<UUID, Integer> scores = new HashMap<>();
    private int pointsCapacity = 100;

    TagScoreboard(@NotNull Server server) {
        this.server = server;

        scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();

        huntersTeam = scoreboard.registerNewTeam(HUNTERS_TEAM_NAME);
        huntersTeam.setColor(HUNTERS_TEAM_COLOR);
        huntersTeam.setPrefix("" + HUNTERS_TEAM_COLOR);
        huntersTeam.setCanSeeFriendlyInvisibles(true);
        huntersTeam.setAllowFriendlyFire(false);

        runnersTeam = scoreboard.registerNewTeam(RUNNERS_TEAM_NAME);
        runnersTeam.setColor(RUNNERS_TEAM_COLOR);
        runnersTeam.setPrefix("" + RUNNERS_TEAM_COLOR);
        runnersTeam.setCanSeeFriendlyInvisibles(true);
        runnersTeam.setAllowFriendlyFire(RUNNERS_FRIENDLY_FIRE);

        objective = scoreboard.registerNewObjective(OBJECTIVE_NAME, OBJECTIVE_CRITERIA, OBJECTIVE_DISPLAyNAME, RenderType.INTEGER);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    /**
     * @return the player with the most points if above the capacity, else null (no winner yet)
     */
    @Nullable
    Player getWinner() {
        if (scores.size() == 0) return null;

        Map.Entry<UUID, Integer> max = Collections.max(scores.entrySet(), Map.Entry.comparingByValue());

        if (max.getValue() < pointsCapacity) return null;

        return server.getPlayer(max.getKey());
    }

    /**
     * Adds the specified player to the hunter team.
     *
     * @param player the player to add to the hunter team
     */
    void setHunter(@NotNull Player player) {
        initPlayer(player);
        huntersTeam.addEntry(entry(player)); // Automatically removes from any current team
    }

    /**
     * Adds the specified player to the runner team.
     *
     * @param player the player to add to the runner team
     */
    void setRunner(@NotNull Player player) {
        initPlayer(player);
        runnersTeam.addEntry(entry(player)); // Automatically removes from any current team
    }

    /**
     * Resets all scores and teams.
     */
    void reset() {
        scores.clear();
        scoreboard.getEntries().forEach(e -> scoreboard.resetScores(e)); // TODO: might be unnecessary because scores are set to 0 in initPlayer
        huntersTeam.getEntries().forEach(e -> huntersTeam.removeEntry(e));
        runnersTeam.getEntries().forEach(e -> runnersTeam.removeEntry(e));
    }

    /**
     * Adds the specified amount of points to the specified player's score.
     *
     * @param player the player to give the points to
     * @param points the points to give to the player
     */
    void addPoints(@NotNull Player player, int points) {
        int score = scores.merge(player.getUniqueId(), points, Integer::sum);

        float level = getLevel(score);
        int floorLevel = (int) level;

        player.setLevel(floorLevel);
        player.setExp(level - floorLevel);

        objective.getScore(entry(player)).setScore(floorLevel);
    }

    /**
     * Broadcasts the current scores to the chat.
     */
    void printScores() {
        TableBuilder tableBuilder = new ChatTableBuilder()
                .setEllipsize(true)
                .addCol(ChatTableBuilder.Alignment.LEFT, 0.3)
                .addCol(ChatTableBuilder.Alignment.LEFT, 0.7)
                .addRow("" + ChatColor.UNDERLINE + ChatColor.GOLD + "Player", "" + ChatColor.UNDERLINE + ChatColor.GOLD + "Score")
                .addRow();

        for (UUID uuid : scores.keySet()) {
            OfflinePlayer player = server.getOfflinePlayer(uuid);

            tableBuilder.addRow(player.getName(), "" + ChatColor.YELLOW + getLevel(getScore(player)));
        }

        server.broadcastMessage(tableBuilder.build());
    }

    void setPointsCapacity(int pointsCapacity) {
        this.pointsCapacity = pointsCapacity;
    }

    private void initPlayer(@NotNull Player player) {
        if (scores.containsKey(player.getUniqueId())) return;
        scores.put(player.getUniqueId(), 0);
        player.setScoreboard(scoreboard);
        initScore(player);
    }

    private void initScore(@NotNull Player player) {
        addPoints(player, 0);
    }

    private int getScore(@NotNull OfflinePlayer player) {
        return scores.get(player.getUniqueId());
    }

    /**
     * Prevents players from getting experience naturally.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onExpChange(PlayerExpChangeEvent event) {
        if (!scores.containsKey(event.getPlayer().getUniqueId())) return;
        event.setAmount(0);
    }


    private static String entry(@NotNull Player player) {
        return player.getName();
    }

    private static float getLevel(int points) {
        return points / (float) Config.SCORING_POINTS_PER_LEVEL.getValue();
    }
}
