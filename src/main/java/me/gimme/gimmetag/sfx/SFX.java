package me.gimme.gimmetag.sfx;

import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.Sound;

public abstract class SFX {
    public static final SoundEffect TAG = new NoteSoundEffect(Instrument.CHIME, Note.natural(1, Note.Tone.C));
    public static final SoundEffect TAGGED = new NoteSoundEffect(Instrument.CHIME, Note.natural(0, Note.Tone.C));
    public static final SoundEffect TAG_BROADCAST = new NoteSoundEffect(Instrument.CHIME, Note.natural(0,  Note.Tone.C));
    public static final SoundEffect COUNTDOWN = new NoteSoundEffect(Instrument.BASS_GUITAR, Note.natural(0, Note.Tone.C));
    public static final SoundEffect COUNTDOWN_FINISH = new NoteSoundEffect(Instrument.GUITAR, Note.natural(1, Note.Tone.C));
    public static final SoundEffect TELEPORT = new StandardSoundEffect(Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.4f);
    public static final SoundEffect ACTIVATE = new NoteSoundEffect(Instrument.BIT, Note.natural(1, Note.Tone.C));
    public static final SoundEffect USE_EFFECT = new StandardSoundEffect(Sound.ENTITY_ZOMBIE_INFECT, 1f, 1.4f);
    public static final SoundEffect THROW = new StandardSoundEffect(Sound.ENTITY_WITCH_THROW);
    public static final SoundEffect GAME_OVER = new SoundEffectCombination(
            new StandardSoundEffect(Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR, true),
            new StandardSoundEffect(Sound.ENTITY_FIREWORK_ROCKET_TWINKLE_FAR, true));
}
