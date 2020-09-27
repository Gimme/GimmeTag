package me.gimme.gimmetag.item;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ItemManager {

    private Map<String, CustomItem> customItemById = new HashMap<>();
    private Map<String, AbilityItem> abilityItemById = new HashMap<>();

    public ItemManager(@NotNull Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new OnUseListener(), plugin);
    }

    public void registerItem(@NotNull CustomItem customItem) {
        customItemById.put(customItem.getId(), customItem);
    }

    public void registerItem(@NotNull AbilityItem abilityItem) {
        registerItem((CustomItem) abilityItem);
        abilityItemById.put(abilityItem.getId(), abilityItem);
    }

    @Nullable
    public ItemStack createItemStack(@NotNull String customItemId) {
        return createItemStack(customItemId, 1);
    }

    @Nullable
    public ItemStack createItemStack(@NotNull String customItemId, int amount) {
        CustomItem customItem = customItemById.get(customItemId);
        if (customItem == null) return null;

        return customItem.createItemStack(amount);
    }


    private class OnUseListener implements Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        private void onInteract(PlayerInteractEvent event) {
            ItemStack item = event.getItem();
            if (item == null) return;
            if (!(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)))
                return;

            // Don't use if clicked an interactable block
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && clickedBlock.getType().isInteractable()) return;

            // Check if custom item
            String itemId = CustomItem.getCustomItemId(item);
            if (itemId == null) return;

            AbilityItem abilityItem = abilityItemById.get(itemId);
            if (abilityItem == null) return;

            abilityItem.use(item, event.getPlayer());
            event.setCancelled(true);
        }
    }
}
