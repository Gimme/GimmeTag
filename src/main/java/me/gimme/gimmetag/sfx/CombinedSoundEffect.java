package me.gimme.gimmetag.sfx;

import me.gimme.gimmetag.GimmeTag;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

class CombinedSoundEffect extends SoundEffect {
    private SoundEffect[] soundEffects;
    private int delayTicks;

    CombinedSoundEffect(SoundEffect... soundEffects) {
        this(0, soundEffects);
    }

    CombinedSoundEffect(int delayTicks, SoundEffect... soundEffects) {
        this.delayTicks = delayTicks;
        this.soundEffects = soundEffects;
    }

    @Override
    public void playLocal(@NotNull Player player, @NotNull Location location) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (SoundEffect soundEffect : soundEffects) {
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
                for (SoundEffect soundEffect : soundEffects) {
                    soundEffect.play(location);
                }
            }
        }.runTaskLater(GimmeTag.getPlugin(), delayTicks);
    }
}
