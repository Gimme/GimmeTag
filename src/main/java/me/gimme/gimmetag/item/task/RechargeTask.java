package me.gimme.gimmetag.item.task;

import me.gimme.gimmetag.utils.Ticks;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handles recharging item stacks. Consumable items with a recharge time can start this task to get the consumed items
 * back to the inventory after the delay.
 * <p>
 * Works like the League of Legends character Corki's "R" ability.
 */
public class RechargeTask extends BukkitRunnable {

    private final Plugin plugin;
    private final PlayerInventory inventory;
    private final ItemStack itemStackSnapshot;
    private final int rechargeTimeTicks;

    private int slot = -1;
    private int recharges;
    private int ticksUntilRecharge;
    private boolean finalCharge;

    public RechargeTask(@NotNull Plugin plugin, @NotNull Player player, @NotNull ItemStack itemStack, double rechargeTime) {
        this.plugin = plugin;
        this.inventory = player.getInventory();
        this.itemStackSnapshot = itemStack.clone();
        this.rechargeTimeTicks = Ticks.secondsToTicks(rechargeTime);

        recharges = 1;
        ticksUntilRecharge = rechargeTimeTicks;
    }

    @Override
    public void run() {
        if (--ticksUntilRecharge >= 0) return;
        ticksUntilRecharge += rechargeTimeTicks;

        if (!contains(inventory, itemStackSnapshot)) { // Item stack does not exist anymore, for example round has ended or item was dropped
            cancel();
            return;
        }

        if (!finalCharge) recharge();
        else finalCharge = false;

        if (--recharges <= 0) cancel();
    }

    /**
     * Attempts to find an item stack similar to the one being recharged.
     *
     * @return the first found similar item stack
     */
    @Nullable
    private ItemStack findItemStack() {
        if (slot != -1) {
            ItemStack slotItemStack = inventory.getItem(slot);
            if (slotItemStack != null && itemStackSnapshot.isSimilar(slotItemStack)) return slotItemStack;
        }

        slot = firstSimilar(inventory, itemStackSnapshot);
        if (slot != -1) return inventory.getItem(slot);

        return null;
    }

    public void recharge() {
        recharge(1);
    }

    private void recharge(int amount) {
        if (amount == 0) return;

        ItemStack is = findItemStack();
        if (is != null) {
            is.setAmount(is.getAmount() + amount);
            return;
        }

        ItemStack charge = itemStackSnapshot.clone();
        charge.setAmount(amount);
        if (slot != -1 && inventory.getItem(slot) == null) {
            inventory.setItem(slot, charge);
        } else {
            inventory.addItem(charge);
        }
    }

    public void rechargeAll() {
        if (!isCancelled()) cancel();
        recharge(recharges);
    }

    /**
     * Marks this as the final charge if the given item stack is the only remaining stack of its type in the inventory.
     *
     * @param itemStack the item stack to check if final charge
     * @return if it is the final charge
     */
    public boolean setFinalCharge(@NotNull ItemStack itemStack) {
        if (isOnlyStack(inventory, itemStack)) this.finalCharge = true;
        return finalCharge;
    }

    public void incrementRecharges() {
        this.recharges++;
    }

    int getRecharges() {
        return recharges;
    }

    public int getTicksUntilRecharge() {
        return ticksUntilRecharge;
    }

    public boolean isFinalCharge() {
        return finalCharge;
    }

    @NotNull
    public RechargeTask start() {
        runTaskTimer(plugin, 0, 1);
        return this;
    }


    /**
     * Returns the slot of the first found item stack similar to the specified item stack in the given inventory.
     *
     * @param inventory the inventory to look in
     * @param itemStack the item stack to compare to
     * @return slot of the first similar item found in the inventory or -1
     */
    private static int firstSimilar(@NotNull Inventory inventory, @NotNull ItemStack itemStack) {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (itemStack.isSimilar(inventory.getItem(i))) return i;
        }
        return -1;
    }

    /**
     * Returns if the given inventory contains at least one item stack similar to the specified item stack.
     *
     * @param inventory the inventory to look in
     * @param itemStack the item stack to compare to
     * @return if the inventory contains a similar item stack
     */
    private static boolean contains(@NotNull Inventory inventory, @NotNull ItemStack itemStack) {
        return firstSimilar(inventory, itemStack) != -1;
    }

    /**
     * Returns if the the given item stack is the final stack of its type in the inventory.
     *
     * @param inventory the inventory to look in
     * @param itemStack the item stack to check if the last one
     * @return if the the given item stack is the final stack of its type
     */
    private static boolean isOnlyStack(@NotNull Inventory inventory, @NotNull ItemStack itemStack) {
        int foundStacks = 0;
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack is = inventory.getItem(i);
            if (itemStack.isSimilar(is)) foundStacks++;
        }
        return foundStacks == 1;
    }
}
