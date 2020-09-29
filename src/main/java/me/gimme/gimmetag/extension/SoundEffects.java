package me.gimme.gimmetag.extension;

import me.gimme.gimmetag.events.PlayerTaggedEvent;
import me.gimme.gimmetag.sfx.SoundEffect;
import me.gimme.gimmetag.tag.Role;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class SoundEffects implements Listener {
    private Server server;

    public SoundEffects(@NotNull Server server) {
        this.server = server;
    }

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

    //TODO play sfx on round start and end
}
