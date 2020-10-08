package me.gimme.gimmetag.extension;

import me.gimme.gimmetag.tag.TagManager;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Prevents sleeping players from using items in their hands.
 */
public class SleepStun implements Listener {

    private final TagManager tagManager;

    public SleepStun(@NotNull TagManager tagManager) {
        this.tagManager = tagManager;
    }

    @EventHandler
    private void onClick(PlayerInteractEvent event) {
        if (event.useItemInHand() == Event.Result.DENY) return;
        if (!tagManager.isSleeping(event.getPlayer())) return;
        event.setUseItemInHand(Event.Result.DENY);
    }
}
