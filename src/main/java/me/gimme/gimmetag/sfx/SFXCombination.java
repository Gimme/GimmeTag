package me.gimme.gimmetag.sfx;

import me.gimme.gimmetag.GimmeTag;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

class SFXCombination extends SFX {
    private SFX[] soundEffects;
    private int delayTicks;

    SFXCombination(SFX... soundEffects) {
        this(0, soundEffects);
    }

    SFXCombination(int delayTicks, SFX... soundEffects) {
        this.delayTicks = delayTicks;
        this.soundEffects = soundEffects;
    }

    @Override
    public void play(@NotNull Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (SFX soundEffect : soundEffects) {
                    soundEffect.play(player);
                }
            }
        }.runTaskLater(GimmeTag.getPlugin(), delayTicks);
    }
}
