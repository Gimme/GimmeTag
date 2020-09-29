package me.gimme.gimmetag.sfx;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

class SoundSFX extends SFX {
    private Sound sound;
    private float volume;
    private float pitch;
    private boolean local;

    SoundSFX(@NotNull Sound sound) {
        this(sound, 1f, 1f);
    }

    SoundSFX(@NotNull Sound sound, float volume, float pitch) {
        this(sound, volume, pitch, false);
    }

    SoundSFX(@NotNull Sound sound, boolean local) {
        this(sound, 1f, 1f, local);
    }

    SoundSFX(@NotNull Sound sound, float volume, float pitch, boolean local) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.local = local;
    }

    @Override
    public void play(@NotNull Player player) {
        if (local) player.playSound(getFrontOfPlayer(player), sound, volume, pitch);
        else player.getWorld().playSound(player.getLocation(), sound, volume, pitch);
    }
}
