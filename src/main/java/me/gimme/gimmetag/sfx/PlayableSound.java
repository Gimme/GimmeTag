package me.gimme.gimmetag.sfx;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a sound that can be played at a given location in the world.
 */
public interface PlayableSound {
    /**
     * Plays this sound at the specified location.
     * <p>
     * Can be heard by anyone within range.
     *
     * @param location the location to play the sound at
     */
    void playAt(@NotNull Location location);
}
