package me.gimme.gimmetag.item.items;

import me.gimme.gimmetag.config.BouncyProjectileConfig;
import me.gimme.gimmetag.item.BouncyProjectileItem;
import me.gimme.gimmetag.sfx.SoundEffects;
import org.bukkit.Material;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class ImpulseGrenade extends BouncyProjectileItem {

    private static final String NAME = "Impulse Grenade";
    private static final Material MATERIAL = Material.HEART_OF_THE_SEA;

    public ImpulseGrenade(@NotNull BouncyProjectileConfig config, @NotNull Plugin plugin) {
        super(NAME, MATERIAL, config, plugin);

        setExplosionSound(SoundEffects.IMPULSE_EXPLOSION);
    }

    @Override
    protected void onCreate(@NotNull ItemStack itemStack, @NotNull ItemMeta itemMeta) {
        setDisplayItem(itemStack);
    }

    @Override
    protected void onExplode(@NotNull Projectile projectile) {

    }
}
