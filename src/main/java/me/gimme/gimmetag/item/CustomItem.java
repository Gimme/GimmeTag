package me.gimme.gimmetag.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class CustomItem extends ItemStack {
    private String name;

    public CustomItem(@NotNull String name, @NotNull Material type) {
        this(name, type, 1);
    }

    public CustomItem(@NotNull String name, @NotNull Material type, int amount) {
        super(type, amount);
        this.name = name;
    }

    @NotNull
    public String getName() {
        return name;
    }
}
