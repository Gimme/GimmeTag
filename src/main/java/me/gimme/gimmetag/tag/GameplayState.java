package me.gimme.gimmetag.tag;

import org.bukkit.GameMode;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a gameplay state that a player can have. This includes their name, gamemode, inventory and resource levels.
 */
class GameplayState {
    private String displayName;
    private GameMode gameMode;
    private ItemStack[] inventoryContents;
    private double health;
    private int foodLevel;
    private int xp;

    private GameplayState(@NotNull String displayName, @NotNull GameMode gameMode, @NotNull ItemStack[] inventoryContents,
                          double health, int foodLevel, int xp) {
        this.displayName = displayName;
        this.gameMode = gameMode;
        this.inventoryContents = inventoryContents;
        this.health = health;
        this.foodLevel = foodLevel;
        this.xp = xp;
    }

    /**
     * Applies this state to the specified player.
     *
     * @param player the player to apply the state to
     */
    void apply(@NotNull Player player) {
        // Clear potion effects
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        player.setDisplayName(displayName);
        player.setGameMode(gameMode);
        player.getInventory().setContents(inventoryContents);
        player.setHealth(health);
        player.setFoodLevel(foodLevel);
        player.setTotalExperience(xp);
    }

    /**
     * @return the current state of the specified player
     * @param player the player to get the current state of
     */
    static GameplayState of(@NotNull Player player) {
        return new GameplayState(player.getDisplayName(), player.getGameMode(), player.getInventory().getContents(),
                player.getHealth(), player.getFoodLevel(), player.getTotalExperience());
    }

    /**
     * Returns the default state of the specified player.
     * <p>
     * The default state is the state that the player would spawn with the first time they join the world.
     *
     * @param player the player to get the default state of
     * @param server the server to get the default state for
     * @return the default state of the specified player
     */
    static GameplayState defaultState(@NotNull Player player, @NotNull Server server) {
        return new GameplayState(player.getName(), server.getDefaultGameMode(), new ItemStack[0], 20, 20, 0);
    }
}
