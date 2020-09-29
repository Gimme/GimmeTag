package me.gimme.gimmetag.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a hunter starts sleeping.
 */
public class HunterSleepEvent extends PlayerEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private int sleepSeconds;

    public HunterSleepEvent(@NotNull Player player, int sleepSeconds) {
        super(player);
        this.sleepSeconds = sleepSeconds;
    }

    /**
     * @return the sleep duration in seconds
     */
    public int getSleepSeconds() {
        return sleepSeconds;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
