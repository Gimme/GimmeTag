package me.gimme.gimmetag.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public class TagStartEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;

    private Set<UUID> hunters;
    private Set<UUID> runners;

    public TagStartEvent(@NotNull Set<UUID> hunters, @NotNull Set<UUID> runners) {
        this.cancelled = false;
        this.hunters = hunters;
        this.runners = runners;
    }

    /**
     * @return the chosen hunters
     */
    public Set<UUID> getHunters() {
        return hunters;
    }

    /**
     * @return the chosen runners
     */
    public Set<UUID> getRunners() {
        return runners;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean isCancelled) {
        this.cancelled = isCancelled;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
