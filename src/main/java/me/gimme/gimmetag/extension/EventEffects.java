package me.gimme.gimmetag.extension;

import me.gimme.gimmetag.events.PlayerRoleSetEvent;
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

public class EventEffects implements Listener {
    private Server server;

    public EventEffects(@NotNull Server server) {
        this.server = server;
    }

    /**
     * Signals tag events with sound effects, chat message and lightning.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onTag(PlayerTaggedEvent event) {
        if (event.isCancelled()) return;

        Player hunter = event.getHunter();
        Player runner = event.getRunner();

        SoundEffect.TAG.playLocal(hunter);
        SoundEffect.TAGGED.playLocal(runner);

        server.broadcastMessage(Role.HUNTER.playerDisplayName(runner) + " was tagged!");
        for (Player p : server.getOnlinePlayers()) {
            if (p.getUniqueId().equals(runner.getUniqueId())) continue;
            if (p.getUniqueId().equals(hunter.getUniqueId())) continue;

            SoundEffect.TAG_BROADCAST.playLocal(p);
        }

        // Lightning effect
        runner.getWorld().spigot().strikeLightningEffect(runner.getLocation(), true);
    }

    /**
     * Lightning effect on the first chosen hunters.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerRoleSet(PlayerRoleSetEvent event) {
        if (event.getRole() == null || !event.getRole().equals(Role.HUNTER)) return;
        if (!event.getReason().equals(PlayerRoleSetEvent.Reason.ROUND_START)) return;

        Player hunter = event.getPlayer();

        hunter.getWorld().spigot().strikeLightningEffect(hunter.getLocation(), true);
    }

    /**
     * Signals the start of a round with sound effects.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onTagStart(TagStartEvent event) {
        for (Player player : server.getOnlinePlayers()) {
            if (event.getHunters().contains(player.getUniqueId())) SoundEffect.HUNTER_GAME_START.playLocal(player);
            else if (event.getRunners().contains(player.getUniqueId())) SoundEffect.RUNNER_GAME_START.playLocal(player);
        }
    }

    /**
     * Signals the end of a round with sound effects.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onTagEnd(TagEndEvent event) {
        Player winner = event.getWinner();
        if (winner == null) return;

        SoundEffect.GAME_OVER_WIN.playLocal(winner);
        for (Player player : server.getOnlinePlayers()) {
            if (player.getUniqueId().equals(winner.getUniqueId())) continue;
            SoundEffect.GAME_OVER.playLocal(player);
        }
    }
}
