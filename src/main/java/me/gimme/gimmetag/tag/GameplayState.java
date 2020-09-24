package me.gimme.gimmetag.tag;

import org.bukkit.GameMode;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

class GameplayState {
    private String displayName;
    private GameMode gameMode;
    private ItemStack[] inventoryContents;
    private int foodLevel;
    private int xp;

    private GameplayState(@NotNull String displayName, @NotNull GameMode gameMode, @NotNull ItemStack[] inventoryContents,
                          int foodLevel, int xp) {
        this.displayName = displayName;
        this.gameMode = gameMode;
        this.inventoryContents = inventoryContents;
        this.foodLevel = foodLevel;
        this.xp = xp;
    }

    void apply(@NotNull Player player) {
        // Clear potion effects
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        player.setDisplayName(displayName);
        player.setGameMode(gameMode);
        player.getInventory().setContents(inventoryContents);
        player.setFoodLevel(foodLevel);
        player.setTotalExperience(xp);
    }

    static GameplayState of(@NotNull Player player) {
        return new GameplayState(player.getDisplayName(), player.getGameMode(), player.getInventory().getContents(),
                player.getFoodLevel(), player.getTotalExperience());
    }

    static GameplayState defaultState(@NotNull Player player, @NotNull Server server) {
        return new GameplayState(player.getName(), server.getDefaultGameMode(), new ItemStack[0], 20, 0);
    }
}
