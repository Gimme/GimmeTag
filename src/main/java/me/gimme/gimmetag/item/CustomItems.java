package me.gimme.gimmetag.item;

import me.gimme.gimmetag.item.items.*;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public abstract class CustomItems {
    private static final List<CustomItem> CUSTOM_ITEMS = Arrays.asList(
            new HunterCompass(),
            new HunterBow(),
            new SwapperBall(),
            new InvisPotion(),
            new SpeedBoost()
    );

    private static ItemStack create(@NotNull CustomItem model, int amount) {
        ItemStack clone = model.clone();
        clone.setAmount(amount);
        return clone;
    }

    @Nullable
    public static ItemStack getCustomItem(@NotNull String customItem, int amount) {
        for (CustomItem item : CUSTOM_ITEMS) {
            if (item.getName().equalsIgnoreCase(customItem)) return create(item, amount);
        }
        return null;
    }
}
