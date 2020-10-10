package me.gimme.gimmetag.utils.outline;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Creates an outline effect around targets which is only visible to certain players.
 */
public class OutlineEffect {
    private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
    private final PacketListener packetListener;

    private boolean isShown;

    /**
     * Creates an outline effect around targets that is only visible to the specified entity.
     * <p>
     * It's an entity for compatibility reasons, but an outline effect only makes sense to be displayed to players.
     *
     * @param plugin          the plugin to register the effect with
     * @param entity          the entity that will be the only one to see this outline effect
     * @param showForEntityId a predicate deciding if the entity with the given entityId should have an outline
     * @return the created outline effect
     */
    public static OutlineEffect personalEffect(@NotNull Plugin plugin, @NotNull Entity entity, @NotNull Predicate<Integer> showForEntityId) {
        return new OutlineEffect(plugin, ((p, entityId) -> entity.getUniqueId().equals(p.getUniqueId()) && showForEntityId.test(entityId)));
    }

    /**
     * Creates an outline effect around targets that is only visible to certain players.
     *
     * @param plugin    the plugin to register the effect with
     * @param condition a condition that decides if the the entity with the given entityId should have an outline
     *                  displayed to the given player
     */
    public OutlineEffect(@NotNull Plugin plugin, @NotNull ShowOutlineCondition condition) {
        this.packetListener = new PacketAdapter(plugin, PacketType.Play.Server.ENTITY_METADATA, PacketType.Play.Server.NAMED_ENTITY_SPAWN) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Player player = event.getPlayer();
                int entityId = event.getPacket().getIntegers().read(0);
                PacketType packetType = event.getPacketType();

                if (!condition.showOutline(player, entityId)) return;

                if (packetType.equals(PacketType.Play.Server.ENTITY_METADATA)) {
                    List<WrappedWatchableObject> watchableObjects = event.getPacket().getWatchableCollectionModifier().read(0);
                    for (WrappedWatchableObject watchableObject : watchableObjects) {
                        if (watchableObject.getIndex() != 0) continue;
                        byte b = (byte) watchableObject.getValue();
                        b |= 0b01000000;
                        watchableObject.setValue(b);
                    }
                } else if (packetType.equals(PacketType.Play.Server.NAMED_ENTITY_SPAWN)) {
                    WrappedDataWatcher dataWatcher = event.getPacket().getDataWatcherModifier().read(0);
                    if (dataWatcher.hasIndex(0)) {
                        byte b = dataWatcher.getByte(0);
                        b |= 0b01000000;
                        dataWatcher.setObject(0, b);
                    }
                }
            }
        };
    }

    /**
     * Shows this outline effect.
     *
     * @return if the state changed
     */
    public boolean show() {
        if (isShown) return false;
        isShown = true;
        protocolManager.addPacketListener(packetListener);
        return true;
    }

    /**
     * Hides this outline effect.
     *
     * @return if the state changed
     */
    public boolean hide() {
        if (!isShown) return false;
        isShown = false;
        protocolManager.removePacketListener(packetListener);
        return true;
    }

    /**
     * @return if this outline effect is currently shown
     */
    public boolean isShown() {
        return isShown;
    }

    /**
     * Refreshes the outline status on all players on the server.
     */
    public static void refreshPlayers() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            refresh(player);
        }
    }

    /**
     * Refreshes the outline status on the specified entities.
     */
    public static void refresh(@NotNull Iterable<Entity> entities) {
        for (Entity entity : entities) {
            refresh(entity);
        }
    }

    /**
     * Refreshes the specified entity.
     * <p>
     * This forces the server send new metadata packets for that entity, which can then be intercepted and have the
     * outline effect added.
     *
     * @param entity the entity to refresh
     */
    public static void refresh(@NotNull Entity entity) {
        boolean isGlowing = entity.isGlowing();
        entity.setGlowing(!isGlowing);
        entity.setGlowing(isGlowing);
    }

    /**
     * Sets the outline color of the given entities for the given player. If the color is null, the given player's team
     * color will be used instead.
     * <p>
     * Note that any entity that is in a scoreboard team will use that team's color above all else.
     *
     * @param color    the color to set for the outline, or null to use the given player's team color
     * @param player   the player that will see the color change
     * @param entities the entities to set the outline color of
     */
    public static void setColor(@Nullable ChatColor color, @NotNull Player player, @NotNull Entity... entities) {
        if (color == null) {
            Team playerTeam = player.getScoreboard().getEntryTeam(player.getName());
            if (playerTeam != null) color = playerTeam.getColor();
            if (color == null) color = ChatColor.WHITE; // Default outline color
        }

        setColor(color, Collections.singletonList(player), entities);
    }

    /**
     * Sets the outline color of the given entities for all online players.
     * <p>
     * Note that any entity that is in a scoreboard team will use that team's color above all else.
     *
     * @param color    the color to set for the outline
     * @param entities the entities to set the outline color of
     */
    public static void broadcastColor(@NotNull ChatColor color, @NotNull Entity... entities) {
        setColor(color, (Iterable<Player>) null, entities);
    }

    /**
     * Sets the outline color of the given entities for the given players, or null for all online players.
     * <p>
     * Note that any entity that is in a scoreboard team will use that team's color above all else.
     *
     * @param color    the color to set for the outline
     * @param players  the players that will see the color change, or null for all online players
     * @param entities the entities to set the outline color of
     */
    private static void setColor(@NotNull ChatColor color, @Nullable Iterable<Player> players, @NotNull Entity... entities) {
        String teamName = getTeamName(color);

        PlayServerScoreboardTeamWrapper createTeamPacket = new PlayServerScoreboardTeamWrapper(PlayServerScoreboardTeamWrapper.Mode.TEAM_CREATED);
        createTeamPacket.setName(teamName);
        createTeamPacket.setColor(color);

        PlayServerScoreboardTeamWrapper addEntitiesPacket = new PlayServerScoreboardTeamWrapper(PlayServerScoreboardTeamWrapper.Mode.ENTRIES_ADDED);
        addEntitiesPacket.setName(teamName);
        addEntitiesPacket.setEntries(Arrays.stream(entities)
                .map(e -> e.getType() == EntityType.PLAYER ? e.getName() : e.getUniqueId().toString())
                .collect(Collectors.toList()));

        if (players != null) {
            for (Player player : Objects.requireNonNull(players)) {
                createTeamPacket.send(player);
                addEntitiesPacket.send(player);
            }
        } else {
            createTeamPacket.broadcast();
            addEntitiesPacket.broadcast();
        }
    }

    /**
     * Returns the team name to use for the specified color.
     * <p>
     * This is used to keep the names at max 16 characters and avoid conflicts of already used team names.
     *
     * @param color the color to get the team name of
     * @return the team name to use for the specified color
     */
    @NotNull
    private static String getTeamName(@NotNull ChatColor color) {
        String name = "_" + color.name();
        // Max team name length is 16 or the client crashes.
        if (name.length() > 16) name = name.subSequence(0, 16).toString();
        return name;
    }


    public interface ShowOutlineCondition {
        /**
         * Returns if an outline should be shown of the entity with the specified entityId to the specified player.
         *
         * @param player   the player to see the outline
         * @param entityId the entityId of the entity to show the outline of
         * @return if an outline should be shown of the entity to the specified player
         */
        boolean showOutline(@NotNull Player player, int entityId);
    }
}
