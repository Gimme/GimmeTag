package me.gimme.gimmetag.item.items;

import me.gimme.gimmetag.item.CustomItem;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

public class HunterBow extends CustomItem {

    private static final String NAME = "hunter_bow";
    private static final Material MATERIAL = Material.BOW;
    private static final String DISPLAY_NAME = "Hunter Bow";

    public HunterBow() {
        this(1);
    }

    public HunterBow(int amount) {
        super(NAME, MATERIAL, amount);

        ItemMeta meta = Objects.requireNonNull(getItemMeta());
        meta.setDisplayName(DISPLAY_NAME);
        meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
        meta.addEnchant(Enchantment.DURABILITY, 10, true);
        setItemMeta(meta);
    }
}
