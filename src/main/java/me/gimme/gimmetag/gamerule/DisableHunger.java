package me.gimme.gimmetag.gamerule;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

/**
 * Prevents food level from going down.
 */
public class DisableHunger implements Listener {

    private final BooleanSupplier condition;

    public DisableHunger(@NotNull BooleanSupplier condition) {
        this.condition = condition;
    }

    /**
     * Prevents food level from going down.
     */
    @EventHandler(priority = EventPriority.LOW)
    private void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.isCancelled()) return;
        if (!condition.getAsBoolean()) return;

        if (event.getEntity().getType() != EntityType.PLAYER) return;
        Player player = (Player) event.getEntity();
        if (player.getFoodLevel() <= event.getFoodLevel()) return;

        event.setCancelled(true);
    }
}
