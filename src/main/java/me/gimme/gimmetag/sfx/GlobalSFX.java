package me.gimme.gimmetag.sfx;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface GlobalSFX {
    /**
     * Plays this sound effect at the specified location.
     * <p>
     * Can be heard by anyone within range.
     *
     * @param location the location to play the sound effect at
     */
    void play(@NotNull Location location);
}
