package me.gimme.gimmetag.sfx;

import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.Sound;

public abstract class SoundEffect {
    public static final SFX COUNTDOWN = new NoteSFX(Instrument.BASS_GUITAR, Note.natural(0, Note.Tone.C));
    public static final SFX COUNTDOWN_FINISH = new NoteSFX(Instrument.GUITAR, Note.natural(1, Note.Tone.C));

    public static final SFX TAG = new NoteSFX(Instrument.CHIME, Note.natural(1, Note.Tone.C));
    public static final SFX TAGGED = new NoteSFX(Instrument.CHIME, Note.natural(0, Note.Tone.C));
    public static final SFX TAG_BROADCAST = new NoteSFX(Instrument.CHIME, Note.natural(0,  Note.Tone.C));

    public static final SFX TELEPORT = new SoundSFX(Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.4f);
    public static final SFX ACTIVATE = new NoteSFX(Instrument.BIT, Note.natural(1, Note.Tone.C));
    public static final SFX DEACTIVATE = new NoteSFX(Instrument.BIT, Note.natural(0, Note.Tone.C));
    public static final SFX USE_EFFECT = new SoundSFX(Sound.ENTITY_ZOMBIE_INFECT, 1f, 1.4f);
    public static final SFX THROW = new SoundSFX(Sound.ENTITY_WITCH_THROW);
    public static final SFX THUNDER = new SoundSFX(Sound.ENTITY_LIGHTNING_BOLT_THUNDER);

    public static final SFX HUNTER_GAME_START = new SoundSFX(Sound.BLOCK_END_PORTAL_SPAWN, 0.5f);
    public static final SFX RUNNER_GAME_START = new SoundSFX(Sound.BLOCK_BEACON_POWER_SELECT);
    public static final SFX GAME_OVER = new SFXCombination(
            new SoundSFX(Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR),
            new SFXCombination(10, new SoundSFX(Sound.ENTITY_FIREWORK_ROCKET_TWINKLE_FAR))
    );
    public static final SFX GAME_OVER_WIN = new SFXCombination(
            GAME_OVER,
            new SoundSFX(Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.5f)
    );
}
