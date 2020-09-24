package me.gimme.gimmetag.item.items;

import me.gimme.gimmetag.config.Config;
import me.gimme.gimmetag.item.CustomItem;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

public class InvisPotion extends CustomItem {

    private static final String NAME = "invis_potion";
    private static final Material MATERIAL = Material.POTION;
    private static final PotionEffectType POTION_EFFECT_TYPE = PotionEffectType.INVISIBILITY;
    private static final String DISPLAY_NAME = "Potion of Invisibility";
    private static final Color COLOR = Color.fromRGB(0x7f8392); // Invis potion color

    public InvisPotion() {
        this(1);
    }

    public InvisPotion(int amount) {
        super(NAME, MATERIAL, amount);

        long seconds = Math.round(Config.INVIS_POTION_DURATION.getValue().doubleValue());
        int ticks = Math.round(Config.INVIS_POTION_DURATION.getValue().floatValue() * 20);

        PotionMeta potionMeta = (PotionMeta) Objects.requireNonNull(getItemMeta());
        potionMeta.setDisplayName(DISPLAY_NAME + " " + ChatColor.GRAY + ChatColor.ITALIC + "(" + seconds + "s)");
        potionMeta.setColor(COLOR);
        potionMeta.addCustomEffect(new PotionEffect(POTION_EFFECT_TYPE, ticks, 0), true);
        setItemMeta(potionMeta);
    }
}
