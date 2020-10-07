package me.gimme.gimmetag.item;

import me.gimme.gimmetag.GimmeTag;
import me.gimme.gimmetag.config.AbilityItemConfig;
import me.gimme.gimmetag.sfx.SoundEffects;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Represents a custom item that can be used to create item stacks with continuous abilities tied to them.
 * <p>
 * A continuous ability is an ability that, when used, stays active for a certain duration. The duration can be anywhere
 * between 0 and infinity. With a duration of 0, it works the same as a normal ability item.
 */
public abstract class ContinuousAbilityItem extends AbilityItem {

    private final Map<UUID, BukkitRunnable> activeItems = new HashMap<>();

    private int ticksPerCalculation = 10;
    private boolean toggleable;
    private boolean glowWhenActive;

    /**
     * Creates a new continuous ability item with the specified name, item type and configuration.
     *
     * @param name   a unique name and the display name of this item. The name can contain ChatColors and spaces, which
     *               will be stripped from the name and only be used for the display name.
     * @param type   the item type of the generated item stacks
     * @param config a config containing values to be applied to this ability item's variables
     */
    public ContinuousAbilityItem(@NotNull String name, @NotNull Material type, @NotNull AbilityItemConfig config) {
        super(name, type, config);

        if (isInfinite()) {
            showDuration(false);
            showCooldown(false);
            setToggleable(true);
            setUseSound(SoundEffects.ACTIVATE);
        }

        if (getCooldownTicks() <= 0) setCooldown(0.5d);

        this.glowWhenActive = hasDuration();
        if (glowWhenActive) disableGlow();
    }

    @NotNull
    protected abstract ContinuousUse createContinuousUse(@NotNull ItemStack itemStack, @NotNull Player user);

    @Override
    protected boolean onUse(@NotNull ItemStack itemStack, @NotNull Player user) {
        UUID uuid = getUniqueId(itemStack);

        BukkitRunnable currentTask = activeItems.get(uuid);
        if (toggleable && currentTask != null) {
            currentTask.cancel();

            if (glowWhenActive) setGlowing(itemStack, false);

            SoundEffects.DEACTIVATE.play(user);
            return true;
        }

        if (glowWhenActive) setGlowing(itemStack, true);

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
                activeItems.remove(uuid);

                if (glowWhenActive) setGlowing(itemStack, false);

                continuousUse.onFinish();
            }
        }.start());

        return true;
    }

    protected void setTicksPerCalculation(int ticks) {
        this.ticksPerCalculation = ticks;
    }

    protected void setToggleable(boolean toggleable) {
        this.toggleable = toggleable;
    }

    protected boolean isToggleable() {
        return toggleable;
    }

    /**
     * @return if the duration is infinite
     */
    protected boolean isInfinite() {
        return getDurationTicks() < 0;
    }

    protected void setGlowWhenActive(boolean glowWhenActive) {
        this.glowWhenActive = glowWhenActive;
    }

    /**
     * @return if the duration is more than 0
     */
    protected boolean hasDuration() {
        return getDurationTicks() != 0;
    }

    /**
     * Returns if the given item's ability is active or not.
     *
     * @param itemStack the item stack with the ability to check the activation state of
     * @return if the given item's ability is active
     */
    private boolean isActive(@NotNull ItemStack itemStack) {
        return activeItems.containsKey(getUniqueId(itemStack));
    }


    /**
     * Sets the glow state of the given item stack.
     * <p>
     * When an item is glowing, it looks like it is enchanted.
     *
     * @param itemStack the item stack to change the glow state of
     * @param glowing   if the item should be glowing or not
     */
    private static void setGlowing(@NotNull ItemStack itemStack, boolean glowing) {
        ItemMeta itemMeta = Objects.requireNonNull(itemStack.getItemMeta());
        setGlowing(itemMeta, glowing);
        itemStack.setItemMeta(itemMeta);
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

    private abstract static class ItemOngoingUseTaskTimer extends BukkitRunnable {
        private final Player user;
        private final ItemStack item;
        private final int itemSlot;
        private final int durationTicks;
        private final int ticksPerCalculation;

        private int ticksLeft;
        private int ticksUntilCalculation;

        private ItemOngoingUseTaskTimer(@NotNull Player user, @NotNull ItemStack item, int ticksPerCalculation,
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

                if (!user.isOnline() || item.getType() == Material.AIR || !isItemInSlot(item, user, itemSlot)) {
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
        ContinuousAbilityItem.ItemOngoingUseTaskTimer start() {
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
