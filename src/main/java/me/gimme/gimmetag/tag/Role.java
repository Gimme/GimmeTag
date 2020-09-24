package me.gimme.gimmetag.tag;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public enum Role {
    HUNTER("Hunter", ChatColor.RED),
    RUNNER("Runner", ChatColor.AQUA);

    String name;
    ChatColor color;

    Role(@NotNull String name, ChatColor color) {
        this.name = name;
        this.color = color;
    }

    ChatColor getColor() {
        return color;
    }

    String getDisplayName() {
        return color + name + ChatColor.RESET;
    }

    String playerDisplayName(@NotNull Player player) {
        return color + player.getDisplayName();
    }
}
