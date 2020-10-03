package me.gimme.gimmetag.item.items;

import me.gimme.gimmetag.GimmeTag;
import me.gimme.gimmetag.item.AbilityItem;
import me.gimme.gimmetag.item.entities.BouncyProjectile;
import me.gimme.gimmetag.sfx.SoundEffect;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SmokeGrenade extends AbilityItem {
    private static final Particle PARTICLE = Particle.REDSTONE;
    private static final Object DUST_DATA = new Particle.DustOptions(Color.BLUE, 4.0f);
    private static final double RADIUS = 2;
    private static final int PARTICAL_COUNT = 1000;
    private static final int INTERVAL_TICKS = 5;

    private static final Class<? extends Projectile> PROJECTILE_CLASS = Snowball.class;

    private final Plugin plugin;
    private final int durationTicks;
    private final int maxExplosionTimerTicks;
    private final int groundExplosionTimerTicks;
    private final double speed;
    private final double gravity;

    public SmokeGrenade(double cooldown, boolean consumable, double duration, double speed, double gravity,
                        double maxExplosionTimer, double groundExplosionTimer, @NotNull Plugin plugin) {
        super("Smoke Grenade", Material.CLAY_BALL, cooldown, consumable);

        this.plugin = plugin;
        this.durationTicks = (int) Math.round(duration * 20);
        this.maxExplosionTimerTicks = (int) Math.round(maxExplosionTimer * 20);
        this.groundExplosionTimerTicks = (int) Math.round(groundExplosionTimer * 20);
        this.speed = speed;
        this.gravity = gravity;

        showDuration(durationTicks);
        mute();
    }

    @Override
    protected void onCreate(@NotNull ItemStack itemStack, @NotNull ItemMeta itemMeta) {
    }

    @Override
    protected boolean onUse(@NotNull ItemStack itemStack, @NotNull Player user) {

        BouncyProjectile bouncyProjectile = BouncyProjectile.launch(plugin, user, speed, maxExplosionTimerTicks, PROJECTILE_CLASS);

        bouncyProjectile.setOnExplode(projectile -> {
            Location location = projectile.getLocation();

            for (int i = 0; i <= durationTicks; i += INTERVAL_TICKS) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        spawnParticles(location);
                    }
                }.runTaskLater(GimmeTag.getPlugin(), i);
            }
        });
        bouncyProjectile.setGroundExplosionTimerTicks(groundExplosionTimerTicks);
        bouncyProjectile.setGravity(gravity);
        bouncyProjectile.setTrail(true);
        bouncyProjectile.setBounceMarks(true);
        bouncyProjectile.setGlowing(true);

        SoundEffect.THROW.play(user);
        return true;
    }

    private void spawnParticles(@NotNull Location location) {
        World world = Objects.requireNonNull(location.getWorld());
        world.spawnParticle(PARTICLE, location, PARTICAL_COUNT, RADIUS, RADIUS, RADIUS,
                0, DUST_DATA, true);
    }


}
