package me.gimme.gimmetag.events;

import me.gimme.gimmetag.tag.Role;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a player gets a role set.
 */
public class PlayerRoleSetEvent extends PlayerEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private Role role;
    private Reason reason;

    public PlayerRoleSetEvent(@NotNull final Player player, @Nullable final Role role, @NotNull Reason reason) {
        super(player);

        this.role = role;
        this.reason = reason;
    }

    /**
     * @return the role that was set for the player, or null if the role was unset
     */
    @Nullable
    public Role getRole() {
        return role;
    }

    /**
     * @return the reason that the role was set
     */
    @NotNull
    public Reason getReason() {
        return reason;
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


    public enum Reason {
        TAG,
        ROUND_START,
        ROUND_END,
        JOIN,
        QUIT
    }
}
