package me.gimme.gimmetag.sfx;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public enum SoundEffect {
    TAG(Instrument.CHIME, Note.natural(1, Note.Tone.C)),
    TAGGED(Instrument.CHIME, Note.natural(0, Note.Tone.C)),
    TAG_BROADCAST(Instrument.CHIME, Note.natural(0,  Note.Tone.C)),
    COUNTDOWN(Instrument.BASS_GUITAR, Note.natural(0, Note.Tone.C)),
    COUNTDOWN_FINISH(Instrument.GUITAR, Note.natural(1, Note.Tone.C)),
    TELEPORT(org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.4f),
    ACTIVATE(Instrument.BIT, Note.natural(1, Note.Tone.C)),
    USE_EFFECT(Sound.ENTITY_ZOMBIE_INFECT, 1f, 1.4f),
    THROW(Sound.ENTITY_WITCH_THROW);

    private org.bukkit.Sound sound = null;
    private float volume = 1f;
    private float pitch = 1f;

    private Instrument instrument = null;
    private Note note = null;

    private boolean local;

    SoundEffect(@NotNull Instrument instrument, @NotNull Note note){
        this.instrument = instrument;
        this.note = note;
        this.local = true;
    }

    SoundEffect(@NotNull org.bukkit.Sound sound) {
        this.sound = sound;
        this.local = false;
    }

    SoundEffect(@NotNull org.bukkit.Sound sound, float volume, float pitch) {
        this(sound);
        this.volume = volume;
        this.pitch = pitch;
    }

    SoundEffect(@NotNull org.bukkit.Sound sound, float volume, float pitch, boolean local) {
        this(sound, volume, pitch);
        this.local = local;
    }

    public void play(@NotNull Player player) {
        if (local) {
            if (sound != null) player.playSound(getFrontOfPlayer(player), sound, volume, pitch);
            if (instrument != null && note != null) player.playNote(getFrontOfPlayer(player), instrument, note);
        } else {
            World world = player.getWorld();
            if (sound != null) world.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    private static Location getFrontOfPlayer(@NotNull Player player) {
        return player.getLocation().add(player.getLocation().getDirection());
    }
}
