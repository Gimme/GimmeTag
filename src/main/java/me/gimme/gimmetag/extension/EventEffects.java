package me.gimme.gimmetag.extension;

import me.gimme.gimmetag.events.PlayerRoleSetEvent;
import me.gimme.gimmetag.events.PlayerTaggedEvent;
import me.gimme.gimmetag.events.TagEndEvent;
import me.gimme.gimmetag.events.TagStartEvent;
import me.gimme.gimmetag.sfx.SoundEffects;
import me.gimme.gimmetag.tag.Role;
import org.bukkit.Location;
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

        SoundEffects.TAG.play(hunter);
        SoundEffects.TAGGED.play(runner);

        server.broadcastMessage(Role.HUNTER.playerDisplayName(runner) + " was tagged!");
        for (Player p : server.getOnlinePlayers()) {
            if (p.getUniqueId().equals(runner.getUniqueId())) continue;
            if (p.getUniqueId().equals(hunter.getUniqueId())) continue;

            SoundEffects.TAG_BROADCAST.play(p);
        }

        // Lightning visual
        runner.getWorld().spigot().strikeLightningEffect(runner.getLocation(), true);

        // Lightning sound
        Location location = runner.getLocation();
        double offset = 7;
        for (Player p : server.getOnlinePlayers()) {
            if (!p.getWorld().equals(location.getWorld())) continue;
            Location pLocation = p.getLocation();
            location.setY(pLocation.getY());

            if (pLocation.distanceSquared(location) > offset * offset) {
                location = pLocation.add(location.subtract(pLocation).toVector().normalize().multiply(offset));
            }
            SoundEffects.GLOBAL_THUNDER.play(p, location);
        }
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
            if (event.getHunters().contains(player.getUniqueId())) SoundEffects.HUNTER_GAME_START.play(player);
            else if (event.getRunners().contains(player.getUniqueId())) SoundEffects.RUNNER_GAME_START.play(player);
        }
    }

    /**
     * Signals the end of a round with sound effects.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onTagEnd(TagEndEvent event) {
        Player winner = event.getWinner();
        if (winner == null) return;

        SoundEffects.GAME_OVER_WIN.play(winner);
        for (Player player : server.getOnlinePlayers()) {
            if (player.getUniqueId().equals(winner.getUniqueId())) continue;
            SoundEffects.GAME_OVER.play(player);
        }
    }
}
