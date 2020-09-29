package me.gimme.gimmetag.sfx;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class SFX {
    public abstract void play(@NotNull Player player);

    protected static Location getFrontOfPlayer(@NotNull Player player) {
        return player.getLocation().add(player.getLocation().getDirection());
    }
}
