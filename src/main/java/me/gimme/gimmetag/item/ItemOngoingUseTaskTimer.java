package me.gimme.gimmetag.item;

import me.gimme.gimmetag.GimmeTag;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ItemOngoingUseTaskTimer extends BukkitRunnable {
    protected GimmeTag plugin;
    protected Player user;
    protected ItemStack item;
    private int ticksPerRecalculation;
    @Nullable
    private StopCondition stopCondition;

    private int ticksUntilRecalculation = 0;

    public ItemOngoingUseTaskTimer(@NotNull GimmeTag plugin, @NotNull Player user, @NotNull ItemStack item,
                            int ticksPerRecalculation, @Nullable StopCondition stopCondition) {
        this.plugin = plugin;
        this.user = user;
        this.item = item;
        this.ticksPerRecalculation = ticksPerRecalculation;
        this.stopCondition = stopCondition;
    }

    @Override
    public void run() {
        if (--ticksUntilRecalculation <= 0) {
            ticksUntilRecalculation = ticksPerRecalculation;

            if (!user.isOnline() || item.getType().equals(Material.AIR) || (stopCondition != null && stopCondition.shouldStop())) {
                cancel();
                return;
            }

            onRecalculation();
        }

        onTick();
    }

    public abstract void onRecalculation();

    public abstract void onTick();

    public abstract void onFinish();

    @Override
    public synchronized void cancel() throws IllegalStateException {
        super.cancel();
        onFinish();
    }

    @NotNull
    public ItemOngoingUseTaskTimer start() {
        return start(1);
    }

    @NotNull
    public ItemOngoingUseTaskTimer start(long period) {
        runTaskTimer(plugin, 0, period);
        return this;
    }

    public interface StopCondition {
        boolean shouldStop();
    }
}
