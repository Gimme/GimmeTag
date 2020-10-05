package me.gimme.gimmetag.sfx;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Sound effect that can be played locally for players or at specific locations in the world.
 */
public abstract class SFX implements GlobalSFX {

    public static final float DEFAULT_VOLUME = 1f;

    /**
     * Plays this sound effect locally for the specified player at the player's location.
     *
     * @param player the player to play the sound effect for
     */
    public void playLocal(@NotNull Player player) {
        playLocal(player, getFrontOfPlayer(player));
    }

    /**
     * Plays this sound effect locally for the player at the specified location.
     *
     * @param player   the player to play the sound effect for
     * @param location the location to play the sound at
     */
    public abstract void playLocal(@NotNull Player player, @NotNull Location location);

    /**
     * Plays this sound effect at the specified player's location.
     * <p>
     * Can be heard by anyone within range.
     *
     * @param player the player whose location to play the sound effect at
     */
    public void play(@NotNull Player player) {
        play(getFrontOfPlayer(player));
    }

    /**
     * Plays this sound effect at the specified location.
     * <p>
     * Can be heard by anyone within range.
     *
     * @param location the location to play the sound effect at
     */
    public abstract void play(@NotNull Location location);

    private static Location getFrontOfPlayer(@NotNull Player player) {
        return player.getLocation().add(player.getLocation().getDirection().multiply(0.5));
    }
}
