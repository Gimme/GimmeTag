package me.gimme.gimmetag.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Creates an outline effect around targets which is only visible to certain players.
 */
public class OutlineEffect {
    private PacketListener packetListener;

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
     * Show this outline effect.
     */
    public void show() {
        ProtocolLibrary.getProtocolManager().addPacketListener(packetListener);
    }

    /**
     * Hide this outline effect.
     */
    public void hide() {
        ProtocolLibrary.getProtocolManager().removePacketListener(packetListener);
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
