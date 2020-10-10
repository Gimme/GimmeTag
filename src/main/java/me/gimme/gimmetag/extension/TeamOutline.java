package me.gimme.gimmetag.extension;

import me.gimme.gimmetag.config.Config;
import me.gimme.gimmetag.events.PlayerRoleSetEvent;
import me.gimme.gimmetag.tag.Role;
import me.gimme.gimmetag.tag.TagManager;
import me.gimme.gimmetag.utils.outline.OutlineEffect;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Show outlines of teammates (can be seen through walls).
 */
public class TeamOutline implements Listener {
    private final Server server;
    private final TagManager tagManager;

    public TeamOutline(@NotNull Plugin plugin, @NotNull TagManager tagManager) {
        this.server = plugin.getServer();
        this.tagManager = tagManager;
        new OutlineEffect(plugin, this::showOutline).show();
    }

    /**
     * Refreshes players after team changes.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerRoleSet(PlayerRoleSetEvent event) {
        OutlineEffect.refreshPlayers();
    }

    /**
     * Returns if an outline should be shown of the entity with the specified entityId to the specified player.
     *
     * @param player   the player to see the outline
     * @param entityId the entityId of the entity to show the outline of
     * @return if an outline should be shown of the entity to the specified player
     */
    private boolean showOutline(@NotNull Player player, int entityId) {
        Role role = tagManager.getRole(player);

        if (role == null) return false;
        if (role == Role.HUNTER && !Config.HUNTER_TEAMMATE_OUTLINE.getValue()) return false;
        if (role == Role.RUNNER && !Config.RUNNER_TEAMMATE_OUTLINE.getValue()) return false;

        return hasRole(entityId, role);
    }

    /**
     * Returns if the entity with the specified entityId has the specified role.
     *
     * @param entityId the entityId of the entity to check the role of
     * @param role     the role to check against
     * @return if the entity has the specified role
     */
    private boolean hasRole(int entityId, @NotNull Role role) {
        return tagManager.getPlayersByRole(role).stream()
                .map(server::getPlayer)
                .filter(Objects::nonNull)
                .map(Entity::getEntityId)
                .anyMatch(id -> id == entityId);
    }
}
