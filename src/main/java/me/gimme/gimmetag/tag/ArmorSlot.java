package me.gimme.gimmetag.tag;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public enum ArmorSlot {
    HEAD(PlayerInventory::setHelmet),
    CHEST(PlayerInventory::setChestplate),
    LEGS(PlayerInventory::setLeggings),
    FEET(PlayerInventory::setBoots);

    private final BiConsumer<@NotNull PlayerInventory, @NotNull ItemStack> equiper;

    ArmorSlot(@NotNull BiConsumer<@NotNull PlayerInventory, @NotNull ItemStack> equiper) {
        this.equiper = equiper;
    }

    public void equip(@NotNull PlayerInventory inventory, @NotNull ItemStack itemStack) {
        equiper.accept(inventory, itemStack);
    }
}
