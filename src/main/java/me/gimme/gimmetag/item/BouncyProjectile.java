package me.gimme.gimmetag.item;

import me.gimme.gimmetag.utils.OutlineEffect;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Represents a projectile that bounces when it hits a surface.
 * <p>
 * It only disappears when it "explodes", which happens, at the latest, after a set maximum amount of time.
 */
public class BouncyProjectile implements Listener {
    private static final Set<BouncyProjectile> activeProjectiles = new HashSet<>();

    private static final int TRAIL_FREQUENCY_TICKS = 1;                 // Ticks between each trail update
    private static final double BOUNCE_FRICTION_MULTIPLIER = 0.45;      // Applied every bounce
    private static final double SURFACE_FRICTION_MULTIPLIER = 0.8;      // Applied every tick when grounded
    private static final double Y_VELOCITY_CONSIDERED_GROUNDED = 0.15;  // The y-velocity when it should stop bouncing
    private static final double RADIUS = 0.07;                          // Radius of the projectile
    private static final double DEFAULT_GRAVITY = 0.028;                // ~0.028 is the standard gravity for a snowball

    private final UUID sourceProjectileId; // The unique ID of the initial launched projectile
    private final BukkitRunnable explosionTimerTask;
    private final BukkitRunnable groundFrictionTask;
    private final BukkitRunnable trailTask;
    private final BukkitRunnable gravityTask;
    private final OutlineEffect outlineEffect;

    private Consumer<Projectile> onExplode = null;
    private int groundExplosionTimerTicks = -1;
    private boolean showTrail = false;
    private boolean showBounceMarks = false;
    private boolean manualGravity = false;
    private double gravity = DEFAULT_GRAVITY;
    private boolean grounded = false;

    private Projectile currentProjectile;

    /**
     * Launches a bouncy projectile from the given source player with the specified initial speed. After the specified
     * amount of max ticks, the projectile disappears from the world.
     *
     * @param plugin          The plugin to schedule tasks from
     * @param source          The player to launch the projectile
     * @param speed           The initial speed of the launched projectile
     * @param maxTicks        Max amount of ticks for the projectile to live
     * @param projectileClass The type of projectile to launch
     * @return the launched bouncy projectile
     */
    public static BouncyProjectile launch(@NotNull Plugin plugin, @NotNull Player source, double speed, int maxTicks,
                                          Class<? extends Projectile> projectileClass) {
        Projectile projectile = source.launchProjectile(projectileClass);
        projectile.setShooter(source);
        projectile.setVelocity(projectile.getVelocity().multiply(speed));

        return new BouncyProjectile(plugin, projectile, source, maxTicks);
    }

    private BouncyProjectile(@NotNull Plugin plugin, @NotNull Projectile projectile, @NotNull Entity source, int maxTicks) {
        this.sourceProjectileId = projectile.getUniqueId();
        this.currentProjectile = projectile;

        activeProjectiles.add(this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        explosionTimerTask = new BukkitRunnable() {
            @Override
            public void run() {
                explode();
            }
        };
        explosionTimerTask.runTaskLater(plugin, maxTicks);

        gravityTask = new BukkitRunnable() {
            @Override
            public void run() {
                Projectile p = getCurrentProjectile();

                p.setGravity(!isGrounded() && !manualGravity);
                if (isGrounded() || !manualGravity) return;

                Vector velocity = currentProjectile.getVelocity();
                velocity.setY(velocity.getY() - gravity);
                currentProjectile.setVelocity(velocity);
            }
        };
        gravityTask.runTaskTimer(plugin, 0, 1);

        groundFrictionTask = new BukkitRunnable() {
            int groundedTicks = 0; // Amount of ticks the projectile has been rolling on the ground

            @Override
            public void run() {
                if (!isGrounded()) {
                    groundedTicks = 0;
                    return;
                }

                Projectile p = getCurrentProjectile();

                // Check if still on a solid block (could have rolled off)
                Block block = p.getLocation().getBlock();
                Material inBlockType = block.getType();
                Material onBlockType = block.getRelative(gravity >= 0 ? BlockFace.DOWN : BlockFace.UP).getType();
                if (!inBlockType.isSolid() && !onBlockType.isSolid()) {
                    grounded = false;
                    return;
                }

                // Stop completely if the velocity is very low
                if (isStill()) p.setVelocity(new Vector(0, 0, 0));

                // Apply surface friction
                p.setVelocity(p.getVelocity().multiply(SURFACE_FRICTION_MULTIPLIER));

                // Explode if grounded for too long
                if (groundExplosionTimerTicks >= 0 && groundedTicks++ >= groundExplosionTimerTicks)
                    BouncyProjectile.this.explode();
            }
        };
        groundFrictionTask.runTaskTimer(plugin, 0, 1);

        trailTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!showTrail) return;
                if (isStill()) return;

                Projectile p = getCurrentProjectile();

                p.getWorld().spawnParticle(Particle.END_ROD,
                        p.getLocation() // Align the trail to fit the actual path better
                                .add(p.getVelocity().multiply(-1).multiply(0.3)),
                        1, 0, 0, 0, 0);
            }
        };
        trailTask.runTaskTimer(plugin, TRAIL_FREQUENCY_TICKS, TRAIL_FREQUENCY_TICKS);

        outlineEffect = new OutlineEffect(plugin, (player, entityId) ->
                player.getUniqueId().equals(source.getUniqueId()) && entityId == getCurrentProjectile().getEntityId()
        );
    }

    /**
     * Makes the projectile explode (figuratively) and disappear from the world.
     */
    public void explode() {
        if (onExplode != null) onExplode.accept(getCurrentProjectile());
        remove();
    }

    /**
     * Removes this projectile from the world without exploding.
     */
    public void remove() {
        getCurrentProjectile().remove();

        explosionTimerTask.cancel();
        groundFrictionTask.cancel();
        trailTask.cancel();
        gravityTask.cancel();
        outlineEffect.hide();

        HandlerList.unregisterAll(this);
        activeProjectiles.remove(this);
    }

    /**
     * @return a hash code value for this {@link BouncyProjectile}
     */
    @Override
    public int hashCode() {
        return sourceProjectileId.hashCode();
    }

    /**
     * Sets a consumer to define what happens when the projectile explodes.
     *
     * @param onExplode the consumer to set
     */
    public void setOnExplode(Consumer<Projectile> onExplode) {
        this.onExplode = onExplode;
    }

    /**
     * Sets the amount of ticks spent on the ground before exploding prematurely.
     *
     * @param ticks The amount of ticks spent on the ground before exploding prematurely
     */
    public void setGroundExplosionTimerTicks(int ticks) {
        this.groundExplosionTimerTicks = ticks;
    }

    /**
     * Sets the strength of gravity to affect this projectile.
     *
     * @param gravity The strength of the gravity
     */
    public void setGravity(double gravity) {
        // If the gravity is close to the default gravity, it is better to use Minecraft's built in gravity system
        // for better client-side prediction. With manual gravity the projectile is noticeably laggy.
        if (Math.abs(gravity - DEFAULT_GRAVITY) < 0.01) {
            this.gravity = DEFAULT_GRAVITY;
            this.manualGravity = false;
        } else {
            this.gravity = gravity;
            this.manualGravity = true;
        }
    }

    /**
     * Sets if the projectile should leave behind a trail effect.
     *
     * @param showTrail If the projectile should leave behind a trail effect
     */
    public void setTrail(boolean showTrail) {
        this.showTrail = showTrail;
    }

    /**
     * Sets if each bounce should be marked in the world for a short duration.
     *
     * @param showBounceMarks If the bounces should be marked
     */
    public void setBounceMarks(boolean showBounceMarks) {
        this.showBounceMarks = showBounceMarks;
    }

    /**
     * Sets if the projectile should be glowing (outline effect seen through walls).
     *
     * @param glowing If the projectile should be glowing
     */
    public void setGlowing(boolean glowing) {
        if (glowing) {
            if (outlineEffect.show()) OutlineEffect.refresh(getCurrentProjectile());
        } else {
            if (outlineEffect.hide()) OutlineEffect.refresh(getCurrentProjectile());
        }
    }

    /**
     * @return if the projectile is currently rolling on the ground
     */
    public boolean isGrounded() {
        return grounded;
    }

    /**
     * @return if the projectile is still on the ground
     */
    public boolean isStill() {
        return getCurrentProjectile().getVelocity().lengthSquared() < 0.0001;
    }

    /**
     * @return the current Projectile object that lives in the world until the next bounce
     */
    @NotNull
    public Projectile getCurrentProjectile() {
        return currentProjectile;
    }

    /**
     * Handles the event of the projectile hitting a surface (like a block or an entity).
     * <p>
     * Controls all of the logic surrounding bounces.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onProjectileHit(ProjectileHitEvent event) {
        Projectile oldProjectile = event.getEntity();
        if (!oldProjectile.getUniqueId().equals(currentProjectile.getUniqueId())) return;

        Vector velocity = oldProjectile.getVelocity().clone();
        Block hitBlock = event.getHitBlock();
        BlockFace hitBlockFace = event.getHitBlockFace();
        World world = oldProjectile.getWorld();

        // Check if the bounce was on the ground (or the roof if gravity is inverted)
        boolean groundBounce = hitBlockFace != null && ((gravity > 0 && hitBlockFace.getModY() > 0) || (gravity < 0 && hitBlockFace.getModY() < 0));

        Location hitLocation = oldProjectile.getLocation().clone();
        improveHitLocation(hitLocation, velocity, hitBlock, hitBlockFace);

        // Bounce hit mark
        if (showBounceMarks) world.spawnParticle(Particle.DRAGON_BREATH, hitLocation,
                20, 0.02, 0.02, 0.02, 0);

        if (groundBounce && Math.abs(velocity.getY()) <= Y_VELOCITY_CONSIDERED_GROUNDED) {
            // Set grounded
            grounded = true;
            velocity.setY(0);
            currentProjectile.setGravity(false);
        } else {
            // Apply bounce physics
            bounce(velocity, hitBlockFace);
        }

        currentProjectile = world.spawn(hitLocation, Snowball.class);
        currentProjectile.setVelocity(velocity);
        currentProjectile.setGravity(oldProjectile.hasGravity());
        currentProjectile.setShooter(oldProjectile.getShooter());
        currentProjectile.setFireTicks(oldProjectile.getFireTicks());
    }


    /**
     * Improves the given hit location by simulating projectile movement between ticks.
     * <p>
     * The projectile's location at the moment of the hit is always slightly in front since the real hit would be
     * between ticks.
     * <p>
     * The hit location can be made more accurate by simulating the extension of the travel path as if the
     * projectile continued to fly in the same direction. This can only be done easily if the block is full
     * (i.e., not slabs, fence, etc.), so we first check if the projectile's location is already "inside" the block
     * that was hit, in which case the hit location cannot be improved.
     *
     * @param hitLocation  The last known location of the projectile before the hit, to be improved
     * @param velocity     The velocity of the projectile before the hit
     * @param hitBlock     The block that was hit
     * @param hitBlockFace The block face of the block that was hit
     */
    private static void improveHitLocation(@NotNull Location hitLocation, @NotNull Vector velocity,
                                           @Nullable Block hitBlock, @Nullable BlockFace hitBlockFace) {
        if (hitBlock != null && hitBlockFace != null) {
            Block projectileBlock = hitLocation.getBlock();
            boolean x = hitBlockFace.getModX() == 0 || projectileBlock.getX() == hitBlock.getX();
            boolean y = hitBlockFace.getModY() == 0 || projectileBlock.getY() == hitBlock.getY();
            boolean z = hitBlockFace.getModZ() == 0 || projectileBlock.getZ() == hitBlock.getZ();
            if (!(x && y && z)) {
                Location betterLocation = getExactHitLocation(hitLocation, velocity, hitBlock, hitBlockFace);
                hitLocation.setX(betterLocation.getX());
                hitLocation.setY(betterLocation.getY());
                hitLocation.setZ(betterLocation.getZ());
            }
        }
    }

    /**
     * Returns the most accurate hit location based on the given last location and velocity of a projectile
     * that hit the given block face.
     * <p>
     * The resulting location is the center of the projectile at the moment of impact (one radius' distance from the surface).
     *
     * @param lastLocation The last known location of the projectile before the hit
     * @param velocity     The velocity of the projectile before the hit
     * @param hitBlock     The block that was hit
     * @param hitBlockFace The block face of the block that was hit
     * @return the most accurate hit location
     */
    private static Location getExactHitLocation(@NotNull Location lastLocation, @NotNull Vector velocity,
                                                @NotNull Block hitBlock, @NotNull BlockFace hitBlockFace) {
        // Direction that the projectile is moving
        Vector projectileDirection = velocity.clone().normalize();
        // Direction to the nearest point of the surface
        Vector surfaceDirection = hitBlockFace.getDirection().multiply(-1);
        // A location somewhere on the edge of the block that is the closest to the projectile
        Location closestEdge = hitBlock.getLocation().clone()
                .add(0.5, 0.5, 0.5)
                .add(hitBlockFace.getModX() / 2d, hitBlockFace.getModY() / 2d, hitBlockFace.getModZ() / 2d);

        // Distance to the nearest point of the surface
        double distanceToSurface = closestEdge.clone().subtract(lastLocation).toVector().dot(surfaceDirection);
        // The velocity toward the surface
        double projectedVelocity = velocity.dot(surfaceDirection);

        // Impact angle in radians
        double impactAngle = Math.toRadians(90) - surfaceDirection.angle(projectileDirection);
        // Desired offset amount from the point of the surface that the velocity points to
        double offset = RADIUS / Math.sin(impactAngle);

        // The center of the projectile at the moment of impact
        return lastLocation.clone().add(velocity.clone().multiply(distanceToSurface / projectedVelocity).subtract(projectileDirection.clone().multiply(offset)));
    }

    /**
     * Applies bounce physics on the given velocity vector according to the block face that was hit.
     * <p>
     * If no block face was hit, the bounce simply goes the opposite direction.
     *
     * @param velocity     The velocity to apply the bounce physics on
     * @param hitBlockFace The block face that was hit, or null if no block face was hit
     */
    private static void bounce(@NotNull Vector velocity, @Nullable BlockFace hitBlockFace) {
        if (hitBlockFace != null && hitBlockFace.getDirection().lengthSquared() != 0) {
            if (hitBlockFace.getModX() != 0) {
                velocity.setX(Math.abs(velocity.getX()) * hitBlockFace.getModX());
                velocity.setX(velocity.getX() * BOUNCE_FRICTION_MULTIPLIER);
            } else {
                velocity.setX(velocity.getX() * SURFACE_FRICTION_MULTIPLIER);
            }
            if (hitBlockFace.getModY() != 0) {
                velocity.setY(Math.abs(velocity.getY()) * hitBlockFace.getModY());
                velocity.setY(velocity.getY() * BOUNCE_FRICTION_MULTIPLIER);
            } else {
                velocity.setY(velocity.getY() * SURFACE_FRICTION_MULTIPLIER);
            }
            if (hitBlockFace.getModZ() != 0) {
                velocity.setZ(Math.abs(velocity.getZ()) * hitBlockFace.getModZ());
                velocity.setZ(velocity.getZ() * BOUNCE_FRICTION_MULTIPLIER);
            } else {
                velocity.setZ(velocity.getZ() * SURFACE_FRICTION_MULTIPLIER);
            }
        } else {
            velocity.multiply(-1);
            velocity.multiply(BOUNCE_FRICTION_MULTIPLIER);
        }
    }

    /**
     * Removes any active projectile from the world.
     * <p>
     * If this is not done when the plugin gets disabled and the projectile is not moving, it will never disappear
     * since it has gravity turned off and will never hit anything.
     */
    public static void onDisable() {
        activeProjectiles.forEach(BouncyProjectile::remove);
    }
}
