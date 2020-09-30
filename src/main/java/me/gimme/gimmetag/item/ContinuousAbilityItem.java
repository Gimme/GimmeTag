package me.gimme.gimmetag.item;

import me.gimme.gimmetag.GimmeTag;
import me.gimme.gimmetag.sfx.SoundEffect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class ContinuousAbilityItem extends AbilityItem {
    private int durationTicks;
    private int ticksPerCalculation = 10;

    private Set<UUID> activeItems = new HashSet<>();

    public ContinuousAbilityItem(@NotNull String name, @NotNull Material type, boolean glowing, boolean consumable) {
        this(name, type, glowing, consumable, -1);
    }

    public ContinuousAbilityItem(@NotNull String name, @NotNull Material type, boolean glowing, boolean consumable, double duration) {
        super(name, type, glowing, 0.5d, consumable);

        setDuration(duration);
        mute();

        if (0 < duration && duration < 1000) showDuration(durationTicks);
        else hideCooldown();
    }

    @NotNull
    protected abstract ContinuousUse createContinuousUse(@NotNull ItemStack itemStack, @NotNull Player user);

    protected void setDuration(double seconds) {
        this.durationTicks = (int) Math.round(seconds * 20);
    }

    protected void setTicksPerCalculation(int ticks) {
        this.ticksPerCalculation = ticks;
    }

    /**
     * @return if the duration is infinite
     */
    protected boolean isInfinite() {
        return durationTicks < 0;
    }

    protected double getDuration() {
        return durationTicks / 20d;
    }

    protected int getDurationTicks() {
        return durationTicks;
    }

    @Override
    protected boolean onUse(@NotNull ItemStack itemStack, @NotNull Player user) {
        UUID uuid = getUniqueId(itemStack);

        if (activeItems.contains(uuid)) return false;
        activeItems.add(uuid);

        ContinuousUse continuousUse = createContinuousUse(itemStack, user);

        new ItemOngoingUseTaskTimer(user, itemStack, ticksPerCalculation, durationTicks) {
            @Override
            public void onCalculate() {
                continuousUse.onCalculate();
            }

            @Override
            public void onTick() {
                continuousUse.onTick();
            }

            @Override
            public void onFinish() {
                continuousUse.onFinish();
                activeItems.remove(uuid);
            }
        }.start();

        SoundEffect.ACTIVATE.play(user);
        return true;
    }


    protected static boolean isItemInHand(@NotNull Player player, @NotNull ItemStack itemStack) {
        UUID uuid = getUniqueId(itemStack);
        if (uuid == null) return false;

        PlayerInventory inventory = player.getInventory();

        return uuid.equals(getUniqueId(inventory.getItemInMainHand())) || uuid.equals(getUniqueId(inventory.getItemInOffHand()));
    }


    protected interface ContinuousUse {
        void onCalculate();

        void onTick();

        void onFinish();
    }

    protected abstract static class ItemOngoingUseTaskTimer extends BukkitRunnable {
        private Player user;
        private ItemStack item;
        private int durationTicks;
        private int ticksPerCalculation;

        private int ticksLeft;
        private int ticksUntilCalculation;

        protected ItemOngoingUseTaskTimer(@NotNull Player user, @NotNull ItemStack item, int ticksPerCalculation,
                                          int durationTicks) {
            this.user = user;
            this.item = item;
            this.ticksPerCalculation = ticksPerCalculation;
            this.durationTicks = durationTicks;

            this.ticksLeft = durationTicks;
            this.ticksUntilCalculation = 0;
        }

        @Override
        public void run() {
            if (durationTicks >= 0 && ticksLeft-- < 0) {
                cancel();
                return;
            }

            if (--ticksUntilCalculation <= 0) {
                ticksUntilCalculation = ticksPerCalculation;

                if (!user.isOnline() || item.getType().equals(Material.AIR)) {
                    cancel();
                    return;
                }

                onCalculate();
            }

            onTick();
        }

        protected abstract void onCalculate();

        protected abstract void onTick();

        protected abstract void onFinish();

        @Override
        public synchronized void cancel() throws IllegalStateException {
            super.cancel();
            onFinish();
        }

        @NotNull
        protected ContinuousAbilityItem.ItemOngoingUseTaskTimer start() {
            runTaskTimer(GimmeTag.getPlugin(), 0, 1);
            return this;
        }
    }
}
