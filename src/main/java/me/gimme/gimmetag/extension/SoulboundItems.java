package me.gimme.gimmetag.extension;

import me.gimme.gimmetag.item.CustomItem;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Prevents soulbound items from being picked up by anyone other than their owners, except if they don't have an owner
 * in which case the one who tried to pick it up becomes the owner instead.
 */
public class SoulboundItems implements Listener {

    /**
     * Handles picking up soulbound items from the ground.
     */
    @EventHandler
    private void onEntityPickupItem(EntityPickupItemEvent event) {
        Item item = event.getItem();
        ItemStack itemStack = item.getItemStack();
        Entity entity = event.getEntity();

        if (checkSoulbound(itemStack, entity)) return;

        item.remove();
        event.setCancelled(true);
    }

    /**
     * Handles any type of click on a soulbound item, covering every case of trying to acquire it from another
     * inventory.
     */
    @EventHandler
    private void onInventoryClick(InventoryClickEvent event) {
        ItemStack itemStack = event.getCurrentItem();
        Entity entity = event.getWhoClicked();

        if (itemStack == null) return;
        if (checkSoulbound(itemStack, entity)) return;

        itemStack.setAmount(0);
        if (entity instanceof Player) ((Player) entity).updateInventory();
    }

    /**
     * Checks if the given item stack is soulbound to someone other than the specified entity. If it has a soulbound tag
     * but no owner, the given entity becomes the owner.
     *
     * @param itemStack the item stack to check and update the soulbound status of
     * @param entity    the entity to check if the item is not already soulbound to
     * @return if the given item stack is soul bound to someone other than the specified entity
     */
    private static boolean checkSoulbound(@NotNull ItemStack itemStack, @NotNull Entity entity) {
        if (!CustomItem.isSoulbound(itemStack)) return true;

        UUID soulboundTo = CustomItem.getSoulboundOwner(itemStack);
        if (entity.getUniqueId().equals(soulboundTo)) return true;
        if (soulboundTo == null) {
            // The item has the soulbound tag but doesn't have an owner yet, so this entity becomes the owner.
            CustomItem.soulbind(itemStack, entity);
            return true;
        }
        return false;
    }
}
