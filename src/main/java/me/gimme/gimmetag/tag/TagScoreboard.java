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
import java.util.stream.Collectors;

/**
 * Keeps and displays the scores of players in an active round of tag.
 */
public class TagScoreboard implements Listener {

    private static final String OBJECTIVE_NAME = "tag-game";
    private static final String OBJECTIVE_DISPLAyNAME = "Score";
    private static final String OBJECTIVE_CRITERIA = "dummy";

    private static final String HUNTERS_TEAM_NAME = "Hunters";
    private static final ChatColor HUNTERS_TEAM_COLOR = ChatColor.RED;
    private static final String RUNNERS_TEAM_NAME = "Runners";
    private static final ChatColor RUNNERS_TEAM_COLOR = ChatColor.BLUE;

    private Server server;

    private Scoreboard scoreboard;
    private Objective objective;
    private Team huntersTeam;
    private Team runnersTeam;

    private Map<UUID, Integer> scores = new HashMap<>();
    private int levelsToEnd = Config.SCORING_LEVELS_TO_END.getValue();

    TagScoreboard(@NotNull Server server) {
        this.server = server;

        scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();

        huntersTeam = scoreboard.registerNewTeam(HUNTERS_TEAM_NAME);
        huntersTeam.setColor(HUNTERS_TEAM_COLOR);
        huntersTeam.setPrefix("" + HUNTERS_TEAM_COLOR);
        huntersTeam.setCanSeeFriendlyInvisibles(true);
        huntersTeam.setAllowFriendlyFire(Config.ENABLE_PVP.getValue());
        if (Config.HUNTER_HIDE_NAME_TAG.getValue())
            huntersTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);
        huntersTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OWN_TEAM);

        runnersTeam = scoreboard.registerNewTeam(RUNNERS_TEAM_NAME);
        runnersTeam.setColor(RUNNERS_TEAM_COLOR);
        runnersTeam.setPrefix("" + RUNNERS_TEAM_COLOR);
        runnersTeam.setCanSeeFriendlyInvisibles(true);
        runnersTeam.setAllowFriendlyFire(Config.ENABLE_PVP.getValue());
        if (Config.RUNNER_HIDE_NAME_TAG.getValue())
            runnersTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);
        if (Config.RUNNER_COLLISION_WITH_RUNNER.getValue() && Config.RUNNER_COLLISION_WITH_HUNTER.getValue()) {
            runnersTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        } else if (Config.RUNNER_COLLISION_WITH_RUNNER.getValue()) {
            runnersTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OWN_TEAM);
        } else if (Config.RUNNER_COLLISION_WITH_HUNTER.getValue()) {
            runnersTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OTHER_TEAMS);
        }

        objective = scoreboard.registerNewObjective(OBJECTIVE_NAME, OBJECTIVE_CRITERIA, OBJECTIVE_DISPLAyNAME, RenderType.INTEGER);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    /**
     * @return the winner of the round if decided, else null (no winner yet)
     */
    @Nullable
    Player getWinner() {
        if (scores.size() == 0) return null;

        Map.Entry<UUID, Integer> max = Collections.max(scores.entrySet(), Map.Entry.comparingByValue());
        Map.Entry<UUID, Integer> min = Collections.min(scores.entrySet(), Map.Entry.comparingByValue());

        if (Config.SCORING_END_CONDITION_OVER.getValue()) {
            if (getLevel(max.getValue()) < levelsToEnd) return null;
        } else {
            if (getLevel(min.getValue()) > levelsToEnd) return null;
        }

        Map.Entry<UUID, Integer> winnerEntry;
        if (Config.SCORING_HIGHEST_SCORE_WINS.getValue()) winnerEntry = max;
        else winnerEntry = min;

        return server.getPlayer(winnerEntry.getKey());
    }

    /**
     * Adds the specified player to their role's team.
     *
     * @param player the player to add to the team
     * @param role   the role to decide the team
     */
    void setTeam(@NotNull Player player, @NotNull Role role) {
        initPlayer(player);
        String entry = entry(player);
        if (role.equals(Role.HUNTER)) huntersTeam.addEntry(entry);
        else runnersTeam.addEntry(entry);
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
        int floorLevel = (int) Math.floor(level);
        int shownExpLevel = Math.max(floorLevel, 0); // Players can't have negative experience levels

        player.setLevel(shownExpLevel);
        player.setExp(level - floorLevel);

        objective.getScore(entry(player)).setScore(floorLevel);
    }

    /**
     * Adds the specified amount of levels to the specified player's score.
     *
     * @param player the player to give the levels to
     * @param levels the levels to give to the player
     */
    void addLevels(@NotNull Player player, int levels) {
        addPoints(player, getPoints(levels));
    }

    void setLevelsToEnd(int levelsToEnd) {
        this.levelsToEnd = levelsToEnd;
    }

    Map<UUID, Integer> getScores() {
        return scores;
    }

    private void initPlayer(@NotNull Player player) {
        scores.putIfAbsent(player.getUniqueId(), getPoints(Config.SCORING_STARTING_LEVEL.getValue()));
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


    public static float getLevel(int points) {
        return points / (float) Config.SCORING_POINTS_PER_LEVEL.getValue();
    }

    public static int getPoints(float levels) {
        return Math.round(levels * Config.SCORING_POINTS_PER_LEVEL.getValue());
    }

    private static String entry(@NotNull Player player) {
        return player.getName();
    }
}
