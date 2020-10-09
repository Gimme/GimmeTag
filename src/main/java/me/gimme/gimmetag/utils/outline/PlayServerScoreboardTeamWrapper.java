package me.gimme.gimmetag.utils.outline;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

/**
 * Used to send scoreboard team color packets.
 * <p>
 * To set the team color of entities, a {@link Mode#TEAM_CREATED} packet including the color needs to be sent followed
 * by a {@link Mode#ENTRIES_ADDED} packet including the entities.
 * <p>
 * If a team already exists with the name that was given, the team creation packet gets ignored and can safely be resent
 * any number of times.
 */
class PlayServerScoreboardTeamWrapper {

    private static final PacketType PACKET_TYPE = PacketType.Play.Server.SCOREBOARD_TEAM;

    private final PacketContainer handle;

    PlayServerScoreboardTeamWrapper(@NotNull Mode mode) {
        this.handle = new PacketContainer(PACKET_TYPE);

        handle.getModifier().writeDefaults();
        setMode(mode);
    }

    void send(@NotNull Player receiver) {
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(receiver, handle);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Cannot send packet", e);
        }
    }

    void broadcast() {
        ProtocolLibrary.getProtocolManager().broadcastServerPacket(handle);
    }

    void setColor(ChatColor color) {
        handle.getEnumModifier(ChatColor.class, MinecraftReflection.getMinecraftClass("EnumChatFormat")).write(0, color);
    }

    void setName(String name) {
        handle.getStrings().write(0, name);
    }

    void setEntries(List<String> value) {
        handle.getSpecificModifier(Collection.class).write(0, value);
    }

    private void setMode(@NotNull Mode mode) {
        handle.getIntegers().write(0, mode.getValue());
    }

    enum Mode {
        TEAM_CREATED(0),
        ENTRIES_ADDED(3);

        private final int value;

        Mode(int value) {
            this.value = value;
        }

        private int getValue() {
            return value;
        }
    }
}
