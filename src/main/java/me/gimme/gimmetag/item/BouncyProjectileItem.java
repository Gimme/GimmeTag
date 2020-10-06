package me.gimme.gimmetag.item;

import me.gimme.gimmetag.config.BouncyProjectileConfig;
import me.gimme.gimmetag.item.entities.BouncyProjectile;
import me.gimme.gimmetag.sfx.PlayableSound;
import me.gimme.gimmetag.sfx.SoundEffects;
import me.gimme.gimmetag.utils.Ticks;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BouncyProjectileItem extends AbilityItem {

    private final Plugin plugin;
    private final double speed;
    private final double gravity;
    private final int maxExplosionTimerTicks;
    private final int groundExplosionTimerTicks;
    private final double restitutionFactor;
    private final double frictionFactor;
    private final boolean trail;
    private final boolean bounceMarks;
    private final boolean glowing;

    @Nullable
    private ItemStack displayItem;
    @Nullable
    private PlayableSound explosionSound;

    public BouncyProjectileItem(@NotNull String name, @NotNull Material type, @NotNull BouncyProjectileConfig config,
                                @NotNull Plugin plugin) {
        super(name, type, config);

        this.plugin = plugin;
        this.speed = config.getSpeed();
        this.gravity = config.getGravity();
        this.maxExplosionTimerTicks = Ticks.secondsToTicks(config.getMaxExplosionTimer());
        this.groundExplosionTimerTicks = Ticks.secondsToTicks(config.getGroundExplosionTimer());
        this.restitutionFactor = config.getRestitutionFactor();
        this.frictionFactor = config.getFrictionFactor();
        this.trail = config.getTrail();
        this.bounceMarks = config.getBounceMarks();
        this.glowing = config.getGlowing();

        mute();
    }

    protected abstract void onExplode(@NotNull Projectile projectile);

    @Override
    protected boolean onUse(@NotNull ItemStack itemStack, @NotNull Player user) {
        BouncyProjectile bouncyProjectile = BouncyProjectile.launch(plugin, user, speed, maxExplosionTimerTicks, displayItem);

        bouncyProjectile.setOnExplode((projectile) -> {
            if (explosionSound != null) explosionSound.play(projectile.getLocation());
            onExplode(projectile);
        });
        bouncyProjectile.setGroundExplosionTimerTicks(groundExplosionTimerTicks);
        bouncyProjectile.setGravity(gravity);
        bouncyProjectile.setRestitutionFactor(restitutionFactor);
        bouncyProjectile.setFrictionFactor(frictionFactor);
        bouncyProjectile.setTrail(trail);
        bouncyProjectile.setBounceMarks(bounceMarks);
        bouncyProjectile.setGlowing(glowing);

        SoundEffects.THROW.play(user);
        return true;
    }

    protected void setDisplayItem(@NotNull ItemStack displayItem) {
        this.displayItem = displayItem.clone();
    }

    protected void setExplosionSound(@NotNull PlayableSound explosionSound) {
        this.explosionSound = explosionSound;
    }
}
