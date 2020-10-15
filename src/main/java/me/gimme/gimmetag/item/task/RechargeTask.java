package me.gimme.gimmetag.item.task;

import me.gimme.gimmetag.utils.Ticks;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

/**
 * Handles recharging item stacks. Consumable items with a recharge time can start this task to get the consumed items
 * back to the inventory after the delay.
 * <p>
 * Works like the League of Legends character Corki's "R" ability.
 */
public class RechargeTask extends BukkitRunnable {

    private final Plugin plugin;
    private final Player player;
    private final ItemStack itemStack;
    private final ItemStack charge;
    private final int rechargeTimeTicks;

    private int recharges;
    private int ticksUntilRecharge;
    private boolean finalCharge;

    public RechargeTask(@NotNull Plugin plugin, @NotNull Player player, @NotNull ItemStack itemStack, double rechargeTime) {
        this.plugin = plugin;
        this.player = player;
        this.itemStack = itemStack;
        this.charge = itemStack.clone();
        charge.setAmount(1);
        this.rechargeTimeTicks = Ticks.secondsToTicks(rechargeTime);

        recharges = 1;
        ticksUntilRecharge = rechargeTimeTicks;
    }

    @Override
    public void run() {
        if (--ticksUntilRecharge >= 0) return;
        ticksUntilRecharge += rechargeTimeTicks;

        if (itemStack.getType() == Material.AIR) { // Item stack does not exist anymore, for example the round has ended
            cancel();
            return;
        }

        if (!finalCharge) recharge();
        else finalCharge = false;

        if (--recharges <= 0) cancel();
    }

    private void recharge() {
        player.getInventory().addItem(charge);
    }

    public void rechargeAll() {
        if (!isCancelled()) cancel();

        for (int i = 0; i < recharges; i++) {
            recharge();
        }
    }

    public void setFinalCharge() {
        this.finalCharge = true;
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
}
