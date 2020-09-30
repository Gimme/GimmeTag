package me.gimme.gimmetag.item.items;

import me.gimme.gimmetag.item.CustomItem;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class HunterBow extends CustomItem {

    public HunterBow() {
        super(
                "Hunter Bow",
                Material.BOW
        );

        setGlowing(false);
    }

    @Override
    protected void onCreate(@NotNull ItemStack itemStack, @NotNull ItemMeta itemMeta) {
        itemMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
        itemMeta.addEnchant(Enchantment.DURABILITY, 10, true);
    }
}
