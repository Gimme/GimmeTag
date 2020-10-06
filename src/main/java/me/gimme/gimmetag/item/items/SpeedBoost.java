package me.gimme.gimmetag.item.items;

import me.gimme.gimmetag.config.AbilityItemConfig;
import me.gimme.gimmetag.item.AbilityItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class SpeedBoost extends AbilityItem {

    private static final String DISPLAY_NAME = "Speed Boost";
    private static final Material MATERIAL = Material.SUGAR;

    public SpeedBoost(@NotNull String id, @NotNull AbilityItemConfig config) {
        super(id, DISPLAY_NAME, MATERIAL, config);
    }

    @Override
    protected void onCreate(@NotNull ItemStack itemStack, @NotNull ItemMeta itemMeta) {
    }

    @Override
    protected boolean onUse(@NotNull ItemStack itemStack, @NotNull Player user) {
        int amplifier = getAmplifier();

        PotionEffect currentSpeedEffect = user.getPotionEffect(PotionEffectType.SPEED);
        int currentEffectDuration = 0;

        if (currentSpeedEffect != null) {
            currentEffectDuration = currentSpeedEffect.getDuration();
            int currentEffectAmplifier = currentSpeedEffect.getAmplifier();
            if (currentEffectAmplifier > amplifier) return false; // Don't allow overriding higher-speed boosts
            if (currentEffectAmplifier < amplifier) currentEffectDuration = 0; // Overlap lower-speed boosts
            // Extend same-speed boosts
        }

        user.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, getDurationTicks() + currentEffectDuration, amplifier));

        return true;
    }
}
