package me.gimme.gimmetag.item;

import me.gimme.gimmetag.config.BouncyProjectileConfig;
import me.gimme.gimmetag.item.entities.BouncyProjectile;
import me.gimme.gimmetag.sfx.SoundEffect;
import me.gimme.gimmetag.utils.Ticks;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public abstract class BouncyProjectileItem extends AbilityItem {

    private static final Class<? extends Projectile> PROJECTILE_CLASS = Snowball.class;

    private final Plugin plugin;
    private final double speed;
    private final double gravity;
    private final int maxExplosionTimerTicks;
    private final int groundExplosionTimerTicks;
    private final double restitutionFactor;
    private final double frictionFactor;

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

        mute();
    }

    protected abstract void onExplode(@NotNull Projectile projectile);

    @Override
    protected boolean onUse(@NotNull ItemStack itemStack, @NotNull Player user) {
        BouncyProjectile bouncyProjectile = BouncyProjectile.launch(plugin, user, speed, maxExplosionTimerTicks, PROJECTILE_CLASS);

        bouncyProjectile.setOnExplode(this::onExplode);
        bouncyProjectile.setGroundExplosionTimerTicks(groundExplosionTimerTicks);
        bouncyProjectile.setGravity(gravity);
        bouncyProjectile.setRestitutionFactor(restitutionFactor);
        bouncyProjectile.setFrictionFactor(frictionFactor);
        bouncyProjectile.setTrail(true);
        bouncyProjectile.setBounceMarks(true);
        bouncyProjectile.setGlowing(true);

        SoundEffect.THROW.play(user);
        return true;
    }
}
