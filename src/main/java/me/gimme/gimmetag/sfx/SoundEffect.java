package me.gimme.gimmetag.sfx;

import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

public abstract class SoundEffect {

    // GENERIC SOUNDS

    public static final SFX TELEPORT = new SoundSFX(Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, SFX.DEFAULT_VOLUME, 1.4f);
    public static final SFX GLOBAL_THUNDER = new SoundSFX(Sound.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 1000000f, 1.4f);


    // ITEM SOUNDS

    public static final SFX USE_EFFECT = new SoundSFX(Sound.ENTITY_ZOMBIE_INFECT, SoundCategory.NEUTRAL, SFX.DEFAULT_VOLUME, 1.4f);
    public static final SFX THROW = new SoundSFX(Sound.ENTITY_WITCH_THROW, SoundCategory.NEUTRAL, 0.8f, 1f);
    public static final SFX ACTIVATE = new NoteSFX(Instrument.BIT, Note.natural(1, Note.Tone.C));
    public static final SFX DEACTIVATE = new NoteSFX(Instrument.BIT, Note.natural(0, Note.Tone.C));
    public static final SFX SMOKE_EXPLOSION_SOUND = new SoundSFX(Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.NEUTRAL, SFX.DEFAULT_VOLUME, 0.5f);


    // GAME EVENTS

    public static final SFX COUNTDOWN = new NoteSFX(Instrument.BASS_GUITAR, Note.natural(0, Note.Tone.C));
    public static final SFX COUNTDOWN_FINISH = new NoteSFX(Instrument.GUITAR, Note.natural(1, Note.Tone.C));

    public static final SFX TAG = new NoteSFX(Instrument.CHIME, Note.natural(1, Note.Tone.C));
    public static final SFX TAGGED = new NoteSFX(Instrument.CHIME, Note.natural(0, Note.Tone.C));
    public static final SFX TAG_BROADCAST = new SFXCombination(TAG, TAGGED, GLOBAL_THUNDER);

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
