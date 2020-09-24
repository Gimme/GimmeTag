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
    TELEPORT(org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 100, 1.4f),
    ACTIVATE(Instrument.BIT, Note.natural(1, Note.Tone.C)),
    USE_EFFECT(Sound.ENTITY_ZOMBIE_INFECT, 100, 1.4f);

    private org.bukkit.Sound sound = null;
    private float volume = 100;
    private float pitch = 1;

    private Instrument instrument = null;
    private Note note = null;

    SoundEffect(@NotNull Instrument instrument, @NotNull Note note){
        this.instrument = instrument;
        this.note = note;
    }

    SoundEffect(@NotNull org.bukkit.Sound sound) {
        this.sound = sound;
    }

    SoundEffect(@NotNull org.bukkit.Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public void play(@NotNull Player player) {
        if (sound != null) player.playSound(getFrontOfPlayer(player), sound, SoundCategory.MASTER, volume, pitch);
        if (instrument != null && note != null) player.playNote(getFrontOfPlayer(player), instrument, note);
    }

    private static Location getFrontOfPlayer(@NotNull Player player) {
        return player.getLocation().add(player.getLocation().getDirection());
    }
}
