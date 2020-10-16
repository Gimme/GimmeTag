package me.gimme.gimmetag.item.items;

import me.gimme.gimmetag.config.type.BouncyProjectileConfig;
import me.gimme.gimmetag.item.BouncyProjectileItem;
import me.gimme.gimmetag.sfx.StandardSoundEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * A normal egg item but with optional bouncy projectile properties, no chicken spawning and built-in knockback.
 */
public class CookedEgg extends BouncyProjectileItem {

    private static final String NAME = "Cooked Egg";
    private static final Material MATERIAL = Material.EGG;

    public CookedEgg(@NotNull String id, @NotNull BouncyProjectileConfig config, @NotNull Plugin plugin) {
        super(id, NAME, MATERIAL, config, plugin);

        setGlowing(false);
        setDisplayItem(MATERIAL, false);
        setUseSound(new StandardSoundEffect(Sound.ENTITY_EGG_THROW, SoundCategory.NEUTRAL));
    }

    @Override
    protected void onCreate(@NotNull ItemStack itemStack, @NotNull ItemMeta itemMeta) {
    }

    @Override
    protected void onExplode(@NotNull Projectile projectile, @NotNull Collection<@NotNull Entity> livingEntities) {

    }

    @Override
    protected void onHitEntity(@NotNull Projectile projectile, @NotNull Entity entity) {
    }
}
