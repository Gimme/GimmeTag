package me.gimme.gimmetag.item;

import me.gimme.gimmetag.utils.Materials;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ItemManager {

    private final Map<String, CustomItem> customItemsById = new HashMap<>();
    private final Map<String, AbilityItem> abilityItemsById = new HashMap<>();

    public ItemManager(@NotNull Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new OnUseListener(), plugin);
    }

    public void registerItem(@NotNull CustomItem customItem) {
        if (customItemsById.containsKey(customItem.getId())) {
            Bukkit.getLogger().warning("An item with the id \"" + customItem.getId() + "\" has already been registered. Skipping this one.");
            return;
        }
        customItemsById.put(customItem.getId(), customItem);
        if (customItem instanceof AbilityItem) abilityItemsById.put(customItem.getId(), (AbilityItem) customItem);
    }

    @Nullable
    public ItemStack createItemStack(@NotNull String customItemId) {
        return createItemStack(customItemId, 1);
    }

    @Nullable
    public ItemStack createItemStack(@NotNull String customItemId, int amount) {
        CustomItem customItem = customItemsById.get(customItemId);
        if (customItem == null) return null;

        return customItem.createItemStack(amount);
    }

    public Map<String, CustomItem> getCustomItems() {
        return customItemsById;
    }

    public void onDisable() {
        abilityItemsById.values().forEach(AbilityItem::onDisable);
    }


    private class OnUseListener implements Listener {
        @EventHandler(priority = EventPriority.HIGH)
        private void onInteract(PlayerInteractEvent event) {
            if (event.useItemInHand() == Event.Result.DENY) return;

            ItemStack itemStack = event.getItem();
            if (itemStack == null) return;

            // Check if custom item
            String itemId = CustomItem.getCustomItemId(itemStack);
            if (itemId == null) return;

            // Check if ability item
            AbilityItem abilityItem = abilityItemsById.get(itemId);
            if (abilityItem == null) return;
            if (abilityItem.getUseEvent() != AbilityItem.UseEvent.INTERACT) return;

            // Deny the use of the regular item behind the ability
            event.setUseItemInHand(Event.Result.DENY);

            // Check if right click
            if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK))
                return;

            // Don't use if clicked an interactable block while not sneaking
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && Materials.isInteractable(clickedBlock.getType()) && !event.getPlayer().isSneaking())
                return;

            // Use the ability
            if (abilityItem.use(itemStack, event.getPlayer())) event.setUseInteractedBlock(Event.Result.DENY);
        }

        @EventHandler(priority = EventPriority.HIGH)
        private void onShootBow(EntityShootBowEvent event) {
            if (event.isCancelled()) return;

            Entity entity = event.getEntity();

            if (entity.getType() != EntityType.PLAYER) return;
            Player shooter = (Player) entity;

            ItemStack bow = event.getBow();
            if (bow == null) return;

            // Check if custom item
            String itemId = CustomItem.getCustomItemId(bow);
            if (itemId == null) return;

            // Check if ability item
            AbilityItem abilityItem = abilityItemsById.get(itemId);
            if (abilityItem == null) return;
            if (abilityItem.getUseEvent() != AbilityItem.UseEvent.SHOOT_BOW) return;
            if (!(abilityItem instanceof BowProjectileItem)) return;
            BowProjectileItem bowItem = (BowProjectileItem) abilityItem;

            // Deny the use of the regular item behind the ability
            event.setCancelled(true);

            // Use the ability
            bowItem.use(bow, shooter, event.getForce());
        }
    }
}
