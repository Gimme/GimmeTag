package me.gimme.gimmetag.sfx;

import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

public abstract class SoundEffects {

    // GENERIC SOUNDS

    public static final SoundEffect TELEPORT = new StandardSoundEffect(Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, SoundEffect.DEFAULT_VOLUME, 1.4f);
    public static final SoundEffect GLOBAL_THUNDER = new StandardSoundEffect(Sound.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 1000000f, 1.4f);


    // UI

    private static final float UI_CLICK_VOLUME = 0.25f;
    public static final SoundEffect CLICK = new StandardSoundEffect(Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, UI_CLICK_VOLUME);
    public static final SoundEffect CLICK_ACCEPT = CLICK;
    public static final SoundEffect CLICK_DECLINE = new StandardSoundEffect(Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, UI_CLICK_VOLUME, 0.80f);


    // ITEM SOUNDS

    public static final SoundEffect USE_EFFECT = new StandardSoundEffect(Sound.ENTITY_ZOMBIE_INFECT, SoundCategory.NEUTRAL, SoundEffect.DEFAULT_VOLUME, 1.4f);
    public static final SoundEffect THROW = new StandardSoundEffect(Sound.ENTITY_WITCH_THROW, SoundCategory.NEUTRAL, 0.8f, 1f);
    public static final SoundEffect ACTIVATE = new NoteSoundEffect(Instrument.BIT, Note.natural(1, Note.Tone.C));
    public static final SoundEffect DEACTIVATE = new NoteSoundEffect(Instrument.BIT, Note.natural(0, Note.Tone.C));
    public static final SoundEffect SMOKE_EXPLOSION = new CombinedSoundEffect(
            new StandardSoundEffect(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.NEUTRAL, 0.5f),
            new CombinedSoundEffect(1, new StandardSoundEffect(Sound.ENTITY_CREEPER_HURT, SoundCategory.NEUTRAL, SoundEffect.DEFAULT_VOLUME, 0.7f)),
            new CombinedSoundEffect(5, new StandardSoundEffect(Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.NEUTRAL, SoundEffect.DEFAULT_VOLUME, 0.5f))
    );
    public static final SoundEffect IMPULSE_EXPLOSION = new CombinedSoundEffect(
            new StandardSoundEffect(Sound.BLOCK_CONDUIT_ACTIVATE, SoundCategory.NEUTRAL, 3f),
            new StandardSoundEffect(Sound.BLOCK_CONDUIT_DEACTIVATE, SoundCategory.NEUTRAL, 3f)
    );
    public static final SoundEffect SPY_EYE_ACTIVATION = new StandardSoundEffect(Sound.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.NEUTRAL, 0.7f, 0.9f);


    // GAME EVENTS

    public static final SoundEffect COUNTDOWN = new NoteSoundEffect(Instrument.BASS_GUITAR, Note.natural(0, Note.Tone.C));
    public static final SoundEffect COUNTDOWN_FINISH = new NoteSoundEffect(Instrument.GUITAR, Note.natural(1, Note.Tone.C));

    public static final SoundEffect TAG = new NoteSoundEffect(Instrument.CHIME, Note.natural(1, Note.Tone.C));
    public static final SoundEffect TAGGED = new NoteSoundEffect(Instrument.CHIME, Note.natural(0, Note.Tone.C));
    public static final SoundEffect TAG_BROADCAST = new CombinedSoundEffect(TAG, TAGGED, GLOBAL_THUNDER);

    public static final SoundEffect HUNTER_GAME_START = new StandardSoundEffect(Sound.BLOCK_END_PORTAL_SPAWN, 0.5f);
    public static final SoundEffect RUNNER_GAME_START = new StandardSoundEffect(Sound.BLOCK_BEACON_POWER_SELECT);
    public static final SoundEffect GAME_OVER = new CombinedSoundEffect(
            new StandardSoundEffect(Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR),
            new CombinedSoundEffect(10, new StandardSoundEffect(Sound.ENTITY_FIREWORK_ROCKET_TWINKLE_FAR))
    );
    public static final SoundEffect GAME_OVER_WIN = new CombinedSoundEffect(
            GAME_OVER,
            new StandardSoundEffect(Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.5f)
    );
}
