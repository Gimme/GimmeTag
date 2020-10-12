package me.gimme.gimmetag.tag;

import me.gimme.gimmetag.config.Config;
import me.gimme.gimmetag.extension.BindingCurse;
import me.gimme.gimmetag.item.CustomItem;
import me.gimme.gimmetag.item.ItemManager;
import me.gimme.gimmetag.roleclass.RoleClass;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InventorySupplier {

    private final ItemManager itemManager;

    public InventorySupplier(@NotNull ItemManager itemManager) {
        this.itemManager = itemManager;
    }

    /**
     * Sets the given inventory to the starting state of the specified role class.
     *
     * @param inventory      the inventory to set the state of
     * @param roleClass      the role class to get the starting inventory state of
     * @param clearCooldowns if the cooldowns of the added items should be cleared
     */
    void setInventory(@NotNull PlayerInventory inventory, @Nullable RoleClass roleClass, boolean clearCooldowns) {
        inventory.clear();

        if (roleClass == null) return;
        addItems(inventory, roleClass.getItemMap(), clearCooldowns);

        for (ArmorSlot armorSlot : ArmorSlot.values()) {
            Color color = roleClass.getColor(armorSlot);
            if (color == null) continue;

            armorSlot.equip(inventory, getColoredLeatherArmor(armorSlot, color));
        }
    }

    private void addItems(@NotNull PlayerInventory inventory, Map<String, Integer> items, boolean clearCooldowns) {
        HumanEntity holder = inventory.getHolder();

        for (ItemStack item : getItems(items)) {
            if (Config.SOULBOUND_ITEMS.getValue() && CustomItem.isCustomItem(item)) CustomItem.soulbind(item, holder);
            if (clearCooldowns && holder != null) holder.setCooldown(item.getType(), 0);
            inventory.addItem(item);
        }
    }

    public List<ItemStack> getItems(@NotNull Map<String, Integer> items) {
        List<ItemStack> contents = new ArrayList<>();

        items.forEach((itemId, amount) -> {

            // Check if valid custom item
            ItemStack item = itemManager.createItemStack(itemId, amount);

            // Check if valid normal item
            if (item == null) {
                Material material = Material.matchMaterial(itemId);
                if (material != null) item = new ItemStack(material, amount);
            }

            // Add to contents if valid item
            if (item == null) return;
            contents.add(item);
        });

        return contents;
    }

    private static ItemStack getColoredLeatherArmor(@NotNull ArmorSlot armorSlot, @NotNull Color color) {
        Material material;
        switch (armorSlot) {
            case HEAD:
                material = Material.LEATHER_HELMET;
                break;
            case CHEST:
                material = Material.LEATHER_CHESTPLATE;
                break;
            case LEGS:
                material = Material.LEATHER_LEGGINGS;
                break;
            case FEET:
                material = Material.LEATHER_BOOTS;
                break;
            default:
                throw new IllegalStateException("There should only be four armor slots");
        }

        ItemStack armor = new ItemStack(material);
        LeatherArmorMeta meta = Objects.requireNonNull((LeatherArmorMeta) armor.getItemMeta());

        meta.setColor(color);
        meta.setUnbreakable(true);

        armor.setItemMeta(meta);

        BindingCurse.setBindingCurse(armor, true);

        return armor;
    }
}
