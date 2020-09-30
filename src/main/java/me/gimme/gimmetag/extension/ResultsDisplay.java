package me.gimme.gimmetag.extension;

import me.gimme.gimmecore.util.ChatTableBuilder;
import me.gimme.gimmecore.util.TableBuilder;
import me.gimme.gimmetag.config.Config;
import me.gimme.gimmetag.events.TagEndEvent;
import me.gimme.gimmetag.tag.TagScoreboard;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ResultsDisplay implements Listener {
    private Server server;

    public ResultsDisplay(@NotNull Server server) {
        this.server = server;
    }

    /**
     * Display the results of the round.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onTagEnd(TagEndEvent event) {
        broadcastGameOverTitle(event.getWinner());
        // Display the final scoreboard
        printScores(event.getScores());
    }

    /**
     * Sends a game over title message to all players showing who won the round.
     *
     * @param winner the player who won the round
     */
    private void broadcastGameOverTitle(@Nullable Player winner) {
        for (Player player : server.getOnlinePlayers()) {
            if (winner != null) {
                player.sendTitle(
                        (player.equals(winner) ? ChatColor.GREEN : ChatColor.RED) + "GAME OVER",
                        winner.getDisplayName() + ChatColor.YELLOW + " won",
                        0,
                        80,
                        30);
            } else {
                player.sendTitle(
                        ChatColor.YELLOW + "GAME OVER",
                        "",
                        0,
                        60,
                        30);
            }
        }
    }

    /**
     * Broadcasts the current scores to the chat.
     */
    void printScores(Map<UUID, Integer> scores) {
        TableBuilder tableBuilder = new ChatTableBuilder()
                .setEllipsize(true)
                .addCol(ChatTableBuilder.Alignment.LEFT, 0.3)
                .addCol(ChatTableBuilder.Alignment.LEFT, 0.7)
                .addRow("" + ChatColor.UNDERLINE + ChatColor.GOLD + "Player", "" + ChatColor.UNDERLINE + ChatColor.GOLD + "Score")
                .addRow();

        for (UUID uuid : scores.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> (Config.SCORING_HIGHEST_SCORE_WINS.getValue() ? -1 : 1) * e.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList())) {
            OfflinePlayer player = server.getOfflinePlayer(uuid);

            tableBuilder.addRow(player.getName(), "" + ChatColor.YELLOW + TagScoreboard.getLevel(scores.get(player.getUniqueId())));
        }

        String border = ChatColor.BLACK + "----------------------" + ChatColor.RESET;
        server.broadcastMessage(border + "\n"
                + tableBuilder.build() + "\n"
                + border
        );
    }
}
