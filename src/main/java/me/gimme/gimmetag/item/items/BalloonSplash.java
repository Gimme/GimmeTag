package me.gimme.gimmetag.item.items;

import me.gimme.gimmetag.item.AbilityItem;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class BalloonSplash extends AbilityItem {

    private static final Color COLOR = Color.fromRGB(137, 208, 229);
    private static final Sound LAUNCH_SOUND = Sound.ENTITY_WITCH_THROW;

    private int durationTicks;
    private int level;

    public BalloonSplash(double cooldown, boolean consumable, double duration, int level) {
        super(
                "Balloon Splash",
                Material.SPLASH_POTION,
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
        mute();
        PotionEffect potionEffect = new PotionEffect(PotionEffectType.LEVITATION, durationTicks, level - 1);

        PotionMeta potionMeta = (PotionMeta) itemMeta;
        potionMeta.setColor(COLOR);
        potionMeta.addCustomEffect(potionEffect, true);
    }

    @Override
    protected boolean onUse(@NotNull ItemStack itemStack, @NotNull Player user) {
        ThrownPotion thrownPotion = user.launchProjectile(ThrownPotion.class);
        thrownPotion.setItem(itemStack);

        user.playSound(user.getLocation(), LAUNCH_SOUND, 1f, 1f);
        return true;
    }
}
