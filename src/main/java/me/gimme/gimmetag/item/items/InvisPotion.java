package me.gimme.gimmetag.item.items;

import me.gimme.gimmetag.item.AbilityItem;
import me.gimme.gimmetag.item.CustomItem;
import me.gimme.gimmetag.utils.Ticks;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class InvisPotion extends CustomItem {

    private static final Color COLOR = Color.fromRGB(0x7f8392); // Invisibility potion color

    private final int durationTicks;

    public InvisPotion(double duration) {
        super(
                "invis_potion",
                "Potion of Invisibility",
                Material.POTION
        );

        this.durationTicks = Ticks.secondsToTicks(duration);

        setGlowing(false);
    }

    @Override
    protected void onCreate(@NotNull ItemStack itemStack, @NotNull ItemMeta itemMeta) {
        itemMeta.setDisplayName(itemMeta.getDisplayName() + AbilityItem.getFormattedDuration(durationTicks));

        PotionMeta potionMeta = (PotionMeta) itemMeta;
        potionMeta.setColor(COLOR);
        potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.INVISIBILITY, durationTicks, 0), true);
    }
}
