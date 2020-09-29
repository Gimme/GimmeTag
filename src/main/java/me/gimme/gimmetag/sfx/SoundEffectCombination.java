package me.gimme.gimmetag.sfx;

import me.gimme.gimmetag.GimmeTag;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

class SoundEffectCombination extends SoundEffect {
    private SoundEffect[] soundEffects;
    private int delayTicks;

    SoundEffectCombination(SoundEffect... soundEffects) {
        this(0, soundEffects);
    }

    SoundEffectCombination(int delayTicks, SoundEffect... soundEffects) {
        this.delayTicks = delayTicks;
        this.soundEffects = soundEffects;
    }

    @Override
    public void play(@NotNull Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (SoundEffect soundEffect : soundEffects) {
                    soundEffect.play(player);
                }
            }
        }.runTaskLater(GimmeTag.getPlugin(), delayTicks);
    }
}
