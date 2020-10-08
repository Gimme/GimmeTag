package me.gimme.gimmetag.item.items;

import me.gimme.gimmetag.config.BouncyProjectileConfig;
import me.gimme.gimmetag.item.BouncyProjectileItem;
import me.gimme.gimmetag.sfx.SoundEffects;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class ImpulseGrenade extends BouncyProjectileItem {

    private static final String NAME = "Impulse Grenade";
    private static final Material MATERIAL = Material.HEART_OF_THE_SEA;

    private static final double CENTER_OF_GRAVITY_HEIGHT = 1;

    public ImpulseGrenade(@NotNull BouncyProjectileConfig config, @NotNull Plugin plugin) {
        super(NAME, MATERIAL, config, plugin);

        setDisplayItem(MATERIAL, true);
        setExplosionSound(SoundEffects.IMPULSE_EXPLOSION);
    }

    @Override
    protected void onCreate(@NotNull ItemStack itemStack, @NotNull ItemMeta itemMeta) {
    }

    @Override
    protected void onExplode(@NotNull Projectile projectile) {
        World world = projectile.getWorld();
        Location location = projectile.getLocation();
        Location offsetLocation = location.clone().add(0, -CENTER_OF_GRAVITY_HEIGHT, 0);
        double radius = getRadius();

        world.spawnParticle(Particle.END_ROD, location, 1000, 0, 0, 0, 8);
        playSphereEffect(location, radius);

        Collection<Entity> nearbyLivingEntities = world.getNearbyEntities(
                offsetLocation, radius, radius, radius,
                e -> e.getType().isAlive() && e.getLocation().distanceSquared(offsetLocation) <= radius * radius
        );

        for (Entity entity : nearbyLivingEntities) {
            LivingEntity livingEntity = (LivingEntity) entity;

            Vector direction = livingEntity.getLocation().subtract(offsetLocation).toVector().normalize();

            livingEntity.setVelocity(direction.multiply(getPower()));
        }
    }


    private static void playSphereEffect(@NotNull Location center, double radius) {
        World world = Objects.requireNonNull(center.getWorld());
        double particlesPerCircle = 16;
        double step = Math.PI / particlesPerCircle;
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.AQUA, 2f);

        double halfStep = step / 2;

        Location loc = center.clone();
        for (double i = 0; i < Math.PI + halfStep; i += step) {
            for (double j = 0; j < 2 * Math.PI - halfStep; j += step) {
                double x = radius * Math.sin(i) * Math.cos(j);
                double y = radius * Math.cos(i);
                double z = radius * Math.sin(i) * Math.sin(j);

                loc.add(x, y, z);
                world.spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0, dustOptions, true);
                loc.subtract(x, y, z);
            }
        }
    }
}
