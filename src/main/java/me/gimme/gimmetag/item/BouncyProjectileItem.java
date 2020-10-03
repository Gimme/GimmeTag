package me.gimme.gimmetag.item;

import me.gimme.gimmetag.item.entities.BouncyProjectile;
import me.gimme.gimmetag.sfx.SoundEffect;
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

    public BouncyProjectileItem(@NotNull String name, @NotNull Material type, double cooldown, boolean consumable,
                                double speed, double gravity, double maxExplosionTimer, double groundExplosionTimer,
                                @NotNull Plugin plugin) {
        super(name, type, cooldown, consumable);

        this.plugin = plugin;
        this.speed = speed;
        this.gravity = gravity;
        this.maxExplosionTimerTicks = (int) Math.round(maxExplosionTimer * 20);
        this.groundExplosionTimerTicks = (int) Math.round(groundExplosionTimer * 20);

        mute();
    }

    protected abstract void onExplode(@NotNull Projectile projectile);

    @Override
    protected boolean onUse(@NotNull ItemStack itemStack, @NotNull Player user) {
        BouncyProjectile bouncyProjectile = BouncyProjectile.launch(plugin, user, speed, maxExplosionTimerTicks, PROJECTILE_CLASS);

        bouncyProjectile.setOnExplode(this::onExplode);
        bouncyProjectile.setGroundExplosionTimerTicks(groundExplosionTimerTicks);
        bouncyProjectile.setGravity(gravity);
        bouncyProjectile.setTrail(true);
        bouncyProjectile.setBounceMarks(true);
        bouncyProjectile.setGlowing(true);

        SoundEffect.THROW.play(user);
        return true;
    }
}
