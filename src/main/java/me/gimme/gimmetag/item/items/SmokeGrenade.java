package me.gimme.gimmetag.item.items;

import me.gimme.gimmetag.GimmeTag;
import me.gimme.gimmetag.config.BouncyProjectileConfig;
import me.gimme.gimmetag.item.BouncyProjectileItem;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SmokeGrenade extends BouncyProjectileItem {

    private static final Particle PARTICLE = Particle.REDSTONE;
    private static final Object DUST_DATA = new Particle.DustOptions(Color.BLUE, 4.0f);
    private static final double RADIUS = 2;
    private static final int PARTICAL_COUNT = 1000;
    private static final int INTERVAL_TICKS = 5;

    private final int durationTicks;

    public SmokeGrenade(@NotNull BouncyProjectileConfig config, @NotNull Plugin plugin) {
        super(
                "Smoke Grenade",
                Material.CLAY_BALL,
                config, plugin
        );

        this.durationTicks = (int) Math.round(config.getDuration() * 20);
    }

    @Override
    protected void onCreate(@NotNull ItemStack itemStack, @NotNull ItemMeta itemMeta) {
    }

    @Override
    protected void onExplode(@NotNull Projectile projectile) {
        Location location = projectile.getLocation();

        for (int i = 0; i <= durationTicks; i += INTERVAL_TICKS) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    spawnParticles(location);
                }
            }.runTaskLater(GimmeTag.getPlugin(), i);
        }
    }

    private void spawnParticles(@NotNull Location location) {
        World world = Objects.requireNonNull(location.getWorld());
        world.spawnParticle(PARTICLE, location, PARTICAL_COUNT, RADIUS, RADIUS, RADIUS,
                0, DUST_DATA, true);
    }
}
