package me.gimme.gimmetag.tag;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public enum Role {
    HUNTER("Hunter", ChatColor.RED),
    RUNNER("Runner", ChatColor.AQUA);

    private String name;
    private ChatColor color;

    Role(@NotNull String name, ChatColor color) {
        this.name = name;
        this.color = color;
    }

    public ChatColor getColor() {
        return color;
    }

    public String getDisplayName() {
        return color + name + ChatColor.RESET;
    }

    public String playerDisplayName(@NotNull Player player) {
        return color + ChatColor.stripColor(player.getDisplayName());
    }
}
