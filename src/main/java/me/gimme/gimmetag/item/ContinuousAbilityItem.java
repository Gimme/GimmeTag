package me.gimme.gimmetag.item;

import me.gimme.gimmetag.GimmeTag;
import me.gimme.gimmetag.config.AbilityItemConfig;
import me.gimme.gimmetag.sfx.SoundEffect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class ContinuousAbilityItem extends AbilityItem {

    private final Map<UUID, BukkitRunnable> activeItems = new HashMap<>();

    private int ticksPerCalculation = 10;
    private boolean toggleable = false;

    public ContinuousAbilityItem(@NotNull String name, @NotNull Material type, @NotNull AbilityItemConfig config) {
        super(name, type, config);

        if (isInfinite()) {
            showDuration(false);
            showCooldown(false);
            setToggleable(true);
        }

        mute();
        if (getCooldownTicks() <= 0) setCooldown(0.5d);
    }

    @NotNull
    protected abstract ContinuousUse createContinuousUse(@NotNull ItemStack itemStack, @NotNull Player user);

    protected void setTicksPerCalculation(int ticks) {
        this.ticksPerCalculation = ticks;
    }

    protected void setToggleable(boolean toggleable) {
        this.toggleable = toggleable;
    }

    /**
     * @return if the duration is infinite
     */
    protected boolean isInfinite() {
        return getDurationTicks() < 0;
    }

    @Override
    protected boolean onUse(@NotNull ItemStack itemStack, @NotNull Player user) {
        UUID uuid = getUniqueId(itemStack);

        BukkitRunnable currentTask = activeItems.get(uuid);
        if (currentTask != null) {
            currentTask.cancel();

            if (toggleable) {
                SoundEffect.DEACTIVATE.play(user);
                return true;
            }
        }

        ContinuousUse continuousUse = createContinuousUse(itemStack, user);

        activeItems.put(uuid, new ItemOngoingUseTaskTimer(user, itemStack, ticksPerCalculation, getDurationTicks()) {
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
        }.start());

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
        private final Player user;
        private final ItemStack item;
        private final int itemSlot;
        private final int durationTicks;
        private final int ticksPerCalculation;

        private int ticksLeft;
        private int ticksUntilCalculation;

        protected ItemOngoingUseTaskTimer(@NotNull Player user, @NotNull ItemStack item, int ticksPerCalculation,
                                          int durationTicks) {
            this.user = user;
            this.item = item;
            this.itemSlot = user.getInventory().getHeldItemSlot();
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

                if (!user.isOnline() || item.getType().equals(Material.AIR) || !isItemInSlot(item, user, itemSlot)) {
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

        /**
         * Returns if the specified item is in the specified slot of the specified player's inventory.
         *
         * @param itemStack the item
         * @param player    the player whose inventory to check
         * @param slot      the slot in the inventory to check
         * @return if the item is in the specified slot of the player's inventory
         */
        private static boolean isItemInSlot(@NotNull ItemStack itemStack, @NotNull Player player, int slot) {
            ItemStack itemInSlot = player.getInventory().getItem(slot);
            if (itemInSlot == null) return false;
            return itemStack.equals(itemInSlot);
        }
    }
}
