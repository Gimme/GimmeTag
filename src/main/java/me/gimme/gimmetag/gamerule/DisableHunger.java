package me.gimme.gimmetag.gamerule;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.jetbrains.annotations.NotNull;

public class DisableHunger implements Listener {

    private Condition condition;

    public DisableHunger(@NotNull Condition condition) {
        this.condition = condition;
    }

    @EventHandler
    private void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.isCancelled()) return;
        if (!condition.condition()) return;

        if (!event.getEntity().getType().equals(EntityType.PLAYER)) return;
        Player player = (Player) event.getEntity();
        if (player.getFoodLevel() <= event.getFoodLevel()) return;

        event.setCancelled(true);
    }
}
