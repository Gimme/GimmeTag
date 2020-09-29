package me.gimme.gimmetag.sfx;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

class SoundEffectCombination extends SoundEffect {

    private SoundEffect[] soundEffects;

    SoundEffectCombination(SoundEffect... soundEffects) {
        this.soundEffects = soundEffects;
    }

    @Override
    public void play(@NotNull Player player) {
        for (SoundEffect soundEffect : soundEffects) {
            soundEffect.play(player);
        }
    }
}
