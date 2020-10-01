package me.gimme.gimmetag.extension;

import me.gimme.gimmetag.events.PlayerTaggedEvent;
import me.gimme.gimmetag.events.TagEndEvent;
import me.gimme.gimmetag.events.TagStartEvent;
import me.gimme.gimmetag.sfx.SoundEffect;
import me.gimme.gimmetag.tag.Role;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class TagEventEffects implements Listener {
    private Server server;

    public TagEventEffects(@NotNull Server server) {
        this.server = server;
    }

    /**
     * Signals tag events with sound effects and chat message.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onTag(PlayerTaggedEvent event) {
        if (event.isCancelled()) return;

        Player hunter = event.getHunter();
        Player runner = event.getRunner();

        SoundEffect.TAG.play(hunter);
        SoundEffect.TAGGED.play(runner);

        server.broadcastMessage(Role.HUNTER.playerDisplayName(runner) + " was tagged!");
        for (Player p : server.getOnlinePlayers()) {
            if (p.getUniqueId().equals(runner.getUniqueId())) continue;
            if (p.getUniqueId().equals(hunter.getUniqueId())) continue;

            SoundEffect.TAG_BROADCAST.play(p);
        }
    }

    /**
     * Signals the start of a round with sound effects.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onTagStart(TagStartEvent event) {
        for (Player player : server.getOnlinePlayers()) {
            if (event.getHunters().contains(player.getUniqueId())) SoundEffect.HUNTER_GAME_START.play(player);
            SoundEffect.RUNNER_GAME_START.play(player);
        }
    }

    /**
     * Signals the end of a round with sound effects.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onTagEnd(TagEndEvent event) {
        Player winner = event.getWinner();
        if (winner == null) return;

        SoundEffect.GAME_OVER_WIN.play(winner);
        for (Player player : server.getOnlinePlayers()) {
            if (player.getUniqueId().equals(winner.getUniqueId())) continue;
            SoundEffect.GAME_OVER.play(player);
        }
    }
}
