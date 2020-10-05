package me.gimme.gimmetag.sfx;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SoundSFX extends SFX {
    private Sound sound;
    private float volume;
    private float pitch;

    public SoundSFX(@NotNull Sound sound) {
        this(sound, 1f);
    }

    public SoundSFX(@NotNull Sound sound, float volume) {
        this(sound, volume, 1f);
    }

    public SoundSFX(@NotNull Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public void playLocal(@NotNull Player player, @NotNull Location location) {
        player.playSound(location, sound, volume, pitch);
    }

    @Override
    public void play(@NotNull Location location) {
        Objects.requireNonNull(location.getWorld()).playSound(location, sound, volume, pitch);
    }
}
