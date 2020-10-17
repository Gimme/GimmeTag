package me.gimme.gimmetag.item;

import me.gimme.gimmetag.config.type.BouncyProjectileConfig;
import me.gimme.gimmetag.sfx.StandardSoundEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public abstract class BowProjectileItem extends BouncyProjectileItem {

    private static final Material MATERIAL = Material.BOW;
    private static final Class<? extends Projectile> PROJECTILE_CLASS = Arrow.class;

    public BowProjectileItem(@NotNull String id, @NotNull String displayName, @NotNull BouncyProjectileConfig config, @NotNull Plugin plugin) {
        super(id, displayName, MATERIAL, config, plugin);

        setUseEvent(UseEvent.SHOOT_BOW);
        setUseSound(new StandardSoundEffect(Sound.ENTITY_ARROW_SHOOT, SoundCategory.NEUTRAL));
        setProjectileClass(PROJECTILE_CLASS);
    }

    @Override
    protected void onCreate(@NotNull ItemStack itemStack, @NotNull ItemMeta itemMeta) {
        itemMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
        itemMeta.setUnbreakable(true);
    }

    void use(@NotNull ItemStack itemStack, @NotNull Player user, double force) {
        launch(user, force);

        super.use(itemStack, user);
    }

    @Override
    protected boolean onUse(@NotNull ItemStack itemStack, @NotNull Player user) {
        return true;
    }
}
