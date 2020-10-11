package me.gimme.gimmetag.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemView {
    public static final ItemView EMPTY = new ItemView("", Material.AIR, null);

    private final ItemStack item;

    ItemView(@NotNull String title, @NotNull Material icon, @Nullable List<String> description) {
        this.item = new ItemStack(icon);

        if (icon == Material.AIR) return;
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;

        itemMeta.setDisplayName(title);
        itemMeta.setLore(description);

        item.setItemMeta(itemMeta);
    }

    ItemView(@NotNull ItemStack itemStack) {
        this.item = itemStack;
    }

    @NotNull ItemStack getItem() {
        return item;
    }

    void click(@NotNull Player clicker) {
    }
}
