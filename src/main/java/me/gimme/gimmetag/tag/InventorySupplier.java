package me.gimme.gimmetag.tag;

import me.gimme.gimmetag.config.Config;
import me.gimme.gimmetag.item.CustomItems;
import me.gimme.gimmetag.item.items.HunterCompass;
import me.gimme.gimmetag.item.items.InvisPotion;
import me.gimme.gimmetag.item.items.SwapperBall;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jetbrains.annotations.NotNull;
import java.util.Map;
import java.util.Objects;

public abstract class InventorySupplier {
    static void setInventory(@NotNull Player player, @NotNull Role role) {
        player.getInventory().clear();
        if (Role.HUNTER.equals(role)) setHunterInventory(player.getInventory());
        if (Role.RUNNER.equals(role)) setRunnerInventory(player.getInventory());
    }

    private static void setHunterInventory(@NotNull PlayerInventory inventory) {
        addItems(inventory, Config.HUNTER_ITEMS.getValue());

        Color c = Color.fromRGB(Config.HUNTER_LEATHER_COLOR.getValue());
        inventory.setHelmet(getColoredLeatherArmor(ArmorSlot.HEAD, c));
        inventory.setChestplate(getColoredLeatherArmor(ArmorSlot.CHEST, c));
        inventory.setLeggings(getColoredLeatherArmor(ArmorSlot.LEGS, c));
        inventory.setBoots(getColoredLeatherArmor(ArmorSlot.FEET, c));
    }

    private static void setRunnerInventory(@NotNull PlayerInventory inventory) {
        addItems(inventory, Config.RUNNER_ITEMS.getValue());
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
        LeatherArmorMeta meta = (LeatherArmorMeta) armor.getItemMeta();
        Objects.requireNonNull(meta).setColor(color);
        armor.setItemMeta(meta);
        return armor;
    }

    private static void addItems(@NotNull PlayerInventory inventory, Map<String, Integer> items) {
        items.forEach((k, v) -> {
            int amount = v;
            // First, check if valid custom item
            ItemStack item = CustomItems.getCustomItem(k, amount); //TODO
            if (item == null) {
                // Then, check if valid normal item
                Material material = Material.matchMaterial(k);
                if (material != null) item = new ItemStack(material, amount);
            }
            // Add to inventory if valid item
            if (item != null) inventory.addItem(item);
        });
    }


    private enum ArmorSlot {
        HEAD,
        CHEST,
        LEGS,
        FEET
    }
}
