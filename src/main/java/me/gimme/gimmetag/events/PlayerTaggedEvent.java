package me.gimme.gimmetag.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a runner becomes tagged by a hunter.
 */
public class PlayerTaggedEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled = false;

    private Player runner;
    private Player hunter;

    public PlayerTaggedEvent(@NotNull final Player runner, @NotNull final Player hunter) {
        super(runner);

        this.runner = runner;
        this.hunter = hunter;
    }

    /**
     * @return the player that was tagged (runner)
     */
    @NotNull
    public Player getRunner() {
        return runner;
    }

    /**
     * @return the player that tagged the runner (hunter)
     */
    @NotNull
    public Player getHunter() {
        return hunter;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
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
