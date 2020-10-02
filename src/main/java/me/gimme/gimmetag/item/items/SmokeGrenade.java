package me.gimme.gimmetag.item.items;

import me.gimme.gimmetag.GimmeTag;
import me.gimme.gimmetag.item.AbilityItem;
import me.gimme.gimmetag.sfx.SoundEffect;
import me.gimme.gimmetag.utils.OutlineEffect;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SmokeGrenade extends AbilityItem {
    private static final Particle PARTICLE = Particle.REDSTONE;
    private static final Object DUST_DATA = new Particle.DustOptions(Color.BLUE, 4.0f);
    private static final double RADIUS = 2;
    private static final int PARTICAL_COUNT = 1000;
    private static final int INTERVAL_TICKS = 5;

    private Plugin plugin;
    private int durationTicks;
    private int maxExplosionTimerTicks;
    private int stillExplosionTimerTicks;
    private double initialVelocity;

    public SmokeGrenade(double cooldown, boolean consumable, double duration, double maxExplosionTimer,
                        double stillExplosionTimer, double initialVelocity, @NotNull Plugin plugin) {
        super("Smoke Grenade", Material.CLAY_BALL, cooldown, consumable);

        this.plugin = plugin;
        this.durationTicks = (int) Math.round(duration * 20);
        this.maxExplosionTimerTicks = (int) Math.round(maxExplosionTimer * 20);
        this.stillExplosionTimerTicks = (int) Math.round(stillExplosionTimer * 20);
        this.initialVelocity = initialVelocity;

        showDuration(durationTicks);
        mute();
    }

    @Override
    protected void onCreate(@NotNull ItemStack itemStack, @NotNull ItemMeta itemMeta) {
    }

    @Override
    protected boolean onUse(@NotNull ItemStack itemStack, @NotNull Player user) {
        Projectile projectile = user.launchProjectile(Snowball.class);
        projectile.setShooter(user);
        projectile.setVelocity(projectile.getVelocity().multiply(initialVelocity));

        BouncerTask bouncerTask = new BouncerTask(projectile, user);
        plugin.getServer().getPluginManager().registerEvents(bouncerTask, plugin);
        bouncerTask.runTaskLater(plugin, maxExplosionTimerTicks);

        SoundEffect.THROW.play(user);
        return true;
    }

    private void explode(@NotNull Projectile projectile) {
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


    private class BouncerTask extends BukkitRunnable implements Listener {
        private int trailFrequencyTicks = 1;
        private double bounceFrictionMultiplier = 0.45;
        private double groundFrictionMultiplier = 0.85;
        private double yVelocityConsideredGrounded = 0.09 * bounceFrictionMultiplier;

        private Projectile currentProjectile;
        private BukkitRunnable trailTask;
        private BukkitRunnable groundFrictionTask = null;
        private OutlineEffect outlineEffect;

        private BouncerTask(@NotNull Projectile projectile, @NotNull Player thrower) {
            this.currentProjectile = projectile;

            trailTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (isGrounded()) return;

                    currentProjectile.getWorld().spawnParticle(Particle.END_ROD,
                            currentProjectile.getLocation() // Align the trail to fit the actual path better
                                    .add(0, 0.2, 0)
                                    .add(currentProjectile.getVelocity().multiply(-1).multiply(0.3)),
                            1, 0, 0, 0, 0);
                }
            };
            trailTask.runTaskTimer(plugin, trailFrequencyTicks, trailFrequencyTicks);

            outlineEffect = new OutlineEffect(plugin, (player, entityId) ->
                    player.getUniqueId().equals(thrower.getUniqueId()) && entityId == getCurrentProjectile().getEntityId()
            );
            outlineEffect.show();
            OutlineEffect.refresh(currentProjectile);
        }

        @EventHandler(priority = EventPriority.MONITOR)
        private void onProjectileHit(ProjectileHitEvent event) {
            Bukkit.getLogger().info("velocity: " + event.getEntity().getVelocity() + "     |     Location: " + event.getEntity().getLocation().toVector());

            Projectile p = event.getEntity();
            if (!p.getUniqueId().equals(currentProjectile.getUniqueId())) return;



            // TODO: fix wall bounces
            Vector velocity = p.getVelocity();
            Location location = p.getLocation();

            double x = location.getX();
            double y = location.getY();
            double z = location.getZ();

            double vx = velocity.getX();
            double vy = velocity.getY();
            double vz = velocity.getZ();

            int intX = (int) (vx < 0 ? Math.floor(x) : Math.ceil(x));
            int intY = (int) (vy < 0 ? Math.floor(y) : Math.ceil(y));
            int intZ = (int) (vz < 0 ? Math.floor(z) : Math.ceil(z));

            double partX = (intX - x) / vx;
            double partY = (intY - y) / vy;
            double partZ = (intZ - z) / vz;

            boolean yBounce = partY <= partX && partY <= partZ;

            if (yBounce) {
                velocity.setY(-velocity.getY());
                Bukkit.getLogger().info("Bounce: Y");
            } else {
                if (partX < partZ) {
                    velocity.setX(-velocity.getX());
                    Bukkit.getLogger().info("Bounce: X");
                } else {
                    velocity.setZ(-velocity.getZ());
                    Bukkit.getLogger().info("Bounce: Z");
                }
            }

            velocity.multiply(bounceFrictionMultiplier);




            Projectile newProjectile = p.getWorld().spawn(p.getLocation(), Snowball.class);
            newProjectile.setShooter(p.getShooter());
            if (yBounce) {
                if (velocity.getY() <= yVelocityConsideredGrounded) {
                    Bukkit.getLogger().info("Grounded: " + velocity.getY());
                    velocity.setY(0);
                    newProjectile.setGravity(false);

                    if (groundFrictionTask != null) groundFrictionTask.cancel();
                    groundFrictionTask = new BukkitRunnable() {
                        int stillTicks = 0; // Amount of ticks the projectile has been still on the ground

                        @Override
                        public void run() {
                            if (newProjectile.getVelocity().lengthSquared() < 0.0001) {
                                newProjectile.setVelocity(new Vector(0, 0, 0));
                                if (stillTicks++ >= stillExplosionTimerTicks) BouncerTask.this.run();
                            } else {
                                newProjectile.setVelocity(newProjectile.getVelocity().multiply(groundFrictionMultiplier));
                            }
                        }
                    };
                    groundFrictionTask.runTaskTimer(plugin, 0, 1);
                } else if (velocity.getY() <= 0.1) {
                    velocity.setY(velocity.getY() * bounceFrictionMultiplier);
                }
            }
            newProjectile.setVelocity(velocity);

            currentProjectile = newProjectile;
        }

        private boolean isGrounded() {
            return groundFrictionTask != null;
        }

        @NotNull
        private Projectile getCurrentProjectile() {
            return currentProjectile;
        }

        @Override
        public void run() {
            explode(currentProjectile);

            currentProjectile.remove();

            trailTask.cancel();
            if (groundFrictionTask != null) groundFrictionTask.cancel();
            outlineEffect.hide();

            HandlerList.unregisterAll(this);
            cancel();
        }
    }
}
