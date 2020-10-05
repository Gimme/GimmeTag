package me.gimme.gimmetag.sfx;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SoundSFX extends SFX {
    private final Sound sound;
    private final SoundCategory soundCategory;
    private final float volume;
    private final float pitch;

    public SoundSFX(@NotNull Sound sound) {
        this(sound, SoundCategory.MASTER);
    }

    public SoundSFX(@NotNull Sound sound, @NotNull SoundCategory soundCategory) {
        this(sound, soundCategory, DEFAULT_VOLUME, 1f);
    }

    public SoundSFX(@NotNull Sound sound, float volume) {
        this(sound, volume, 1f);
    }

    public SoundSFX(@NotNull Sound sound, float volume, float pitch) {
        this(sound, SoundCategory.MASTER, volume, pitch);
    }

    public SoundSFX(@NotNull Sound sound, @NotNull SoundCategory soundCategory, float volume, float pitch) {
        this.sound = sound;
        this.soundCategory = soundCategory;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public void playLocal(@NotNull Player player, @NotNull Location location) {
        player.playSound(location, sound, soundCategory, volume, pitch);
    }

    @Override
    public void play(@NotNull Location location) {
        Objects.requireNonNull(location.getWorld()).playSound(location, sound, soundCategory, volume, pitch);
    }
}
