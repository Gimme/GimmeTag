package me.gimme.gimmetag.sfx;

import me.gimme.gimmetag.GimmeTag;
import org.bukkit.Location;
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
    public void playLocal(@NotNull Player player, @NotNull Location location) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (SFX soundEffect : soundEffects) {
                    soundEffect.playLocal(player, location);
                }
            }
        }.runTaskLater(GimmeTag.getPlugin(), delayTicks);
    }

    @Override
    public void play(@NotNull Location location) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (SFX soundEffect : soundEffects) {
                    soundEffect.play(location);
                }
            }
        }.runTaskLater(GimmeTag.getPlugin(), delayTicks);
    }
}
