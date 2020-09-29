package me.gimme.gimmetag.item.items;

import me.gimme.gimmecore.util.RomanNumerals;
import me.gimme.gimmetag.item.AbilityItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class SpeedBoost extends AbilityItem {

    private int durationTicks;
    private int level;

    public SpeedBoost(String id, double cooldown, boolean consumable, double duration, int level) {
        super(
                id,
                "Speed Boost " + RomanNumerals.toRoman(level),
                Material.SUGAR,
                true,
                cooldown,
                consumable,
                null
        );

        this.durationTicks = (int) Math.round(duration * 20);
        this.level = level;
    }

    @Override
    protected void onCreate(@NotNull ItemStack itemStack, @NotNull ItemMeta itemMeta) {
        setDurationInfo(itemMeta, durationTicks);
    }

    @Override
    protected boolean onUse(@NotNull ItemStack itemStack, @NotNull Player user) {
        int amplifier = level - 1;

        PotionEffect currentSpeedEffect = user.getPotionEffect(PotionEffectType.SPEED);
        int currentEffectDuration = 0;

        if (currentSpeedEffect != null) {
            currentEffectDuration = currentSpeedEffect.getDuration();
            int currentEffectAmplifier = currentSpeedEffect.getAmplifier();
            if (currentEffectAmplifier > amplifier) return false; // Don't allow overriding higher-speed boosts
            if (currentEffectAmplifier < amplifier) currentEffectDuration = 0; // Overlap lower-speed boosts
            // Extend same-speed boosts
        }

        user.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, durationTicks + currentEffectDuration, amplifier));

        return true;
    }
}
