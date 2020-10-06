package me.gimme.gimmetag.item.entities;

import me.gimme.gimmetag.utils.OutlineEffect;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Represents a projectile that bounces when it hits a surface.
 * <p>
 * It only disappears when it "explodes", which happens, at the latest, after a set maximum amount of time.
 */
public class BouncyProjectile implements Listener {

    private static final Class<? extends ThrowableProjectile> PROJECTILE_CLASS = Snowball.class;
    private static final Particle TRAIL_PARTICLE = Particle.END_ROD;
    private static final int TRAIL_FREQUENCY_TICKS = 1;                 // Ticks between each trail update
    private static final double Y_VELOCITY_CONSIDERED_GROUNDED = 0.15;  // The y-velocity when the bouncing should stop
    private static final double RADIUS = 0.07;                          // Radius of the projectile
    private static final double DEFAULT_GRAVITY = 0.028;                // ~0.028 is the standard gravity for a snowball

    private final UUID uuid;
    @Nullable
    private final ItemStack displayItem;
    private final BukkitRunnable updateTask;
    private final BukkitRunnable explosionTimerTask;
    private final BukkitRunnable trailTask;
    private final OutlineEffect outlineEffect;

    private Consumer<Projectile> onExplode = null;
    private int groundExplosionTimerTicks = -1;
    private boolean manualGravity = false;
    private double gravity = DEFAULT_GRAVITY;
    private double restitutionFactor = 0.45;
    private double frictionFactor = 0.8;
    private boolean showTrail = false;
    private boolean showBounceMarks = false;
    private boolean grounded = false;
    private int groundedTicks = 0; // Amount of ticks the projectile has been rolling on the ground

    private Projectile currentProjectile;

    /**
     * Launches a bouncy projectile from the given source player with the specified initial speed. After the specified
     * amount of max ticks, the projectile disappears from the world.
     *
     * @param plugin      The plugin to schedule tasks from
     * @param source      The player to launch the projectile
     * @param speed       The initial speed of the launched projectile
     * @param maxTicks    Max amount of ticks for the projectile to live
     * @param displayItem The display ItemStack for the thrown projectile, or null for the default
     * @return the launched bouncy projectile
     */
    public static BouncyProjectile launch(@NotNull Plugin plugin, @NotNull Player source, double speed, int maxTicks,
                                          @Nullable ItemStack displayItem) {
        ThrowableProjectile projectile = source.launchProjectile(PROJECTILE_CLASS);
        if (displayItem != null) projectile.setItem(displayItem);
        projectile.setShooter(source);
        projectile.setVelocity(projectile.getVelocity().multiply(speed));

        return new BouncyProjectile(plugin, projectile, source, maxTicks, displayItem);
    }

    private BouncyProjectile(@NotNull Plugin plugin, @NotNull Projectile projectile, @NotNull Entity source, int maxTicks,
                             @Nullable ItemStack displayItem) {

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        this.uuid = UUID.randomUUID();
        this.displayItem = displayItem;
        this.currentProjectile = projectile;

        // Remove the entity when the server stops
        //noinspection deprecation
        currentProjectile.setPersistent(false);


        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                update();
            }
        };
        updateTask.runTaskTimer(plugin, 0, 1);

        explosionTimerTask = new BukkitRunnable() {
            @Override
            public void run() {
                explode();
            }
        };
        explosionTimerTask.runTaskLater(plugin, maxTicks);

        trailTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!showTrail) return;
                if (isGrounded() && isStill()) return;

                Projectile p = getCurrentProjectile();

                p.getWorld().spawnParticle(TRAIL_PARTICLE,
                        p.getLocation() // Align the trail to fit the actual path better
                                .add(p.getVelocity().multiply(-1).multiply(0.3))
                                .add(0, RADIUS, 0),
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

        updateTask.cancel();
        explosionTimerTask.cancel();
        trailTask.cancel();
        outlineEffect.hide();

        HandlerList.unregisterAll(this);
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
     * Sets the restitution factor, where 1 means lossless bounces and 0 means no bounciness.
     *
     * @param restitutionFactor The restitution factor
     */
    public void setRestitutionFactor(double restitutionFactor) {
        this.restitutionFactor = restitutionFactor;
    }

    /**
     * Sets the friction factor, where 1 is slippery and 0 is sticky.
     *
     * @param frictionFactor The friction factor
     */
    public void setFrictionFactor(double frictionFactor) {
        this.frictionFactor = frictionFactor;
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
     * @return the strength of gravity affecting this projectile
     */
    public double getGravity() {
        return gravity;
    }

    /**
     * @return the restitution factor, where 1 means lossless bounces and 0 means no bounciness.
     */
    public double getRestitutionFactor() {
        return restitutionFactor;
    }

    /**
     * @return the friction factor, where 1 is slippery and 0 is sticky.
     */
    public double getFrictionFactor() {
        return frictionFactor;
    }

    /**
     * @return if the projectile leaves behind a trail effect
     */
    public boolean showTrail() {
        return showTrail;
    }

    /**
     * @return if each bounce is marked in the world for a short duration
     */
    public boolean showBounceMarks() {
        return showBounceMarks;
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

    private boolean isSticky() {
        return getFrictionFactor() == 0 && getRestitutionFactor() == 0;
    }

    /**
     * @return the current Projectile object that lives in the world until the next bounce
     */
    @NotNull
    public Projectile getCurrentProjectile() {
        return currentProjectile;
    }

    /**
     * @return a hash code value for this {@link BouncyProjectile}
     */
    @Override
    public int hashCode() {
        return uuid.hashCode();
    }


    /**
     * Main update method, run every tick.
     */
    private void update() {
        // If the underlying projectile has been removed for some external reason, remove completely.
        if (getCurrentProjectile().isDead()) remove();

        applyGravity();
        moveGrounded();
    }

    /**
     * Manually applies gravity (if enabled) on the projectile.
     */
    private void applyGravity() {
        Projectile p = getCurrentProjectile();

        p.setGravity(!isGrounded() && !manualGravity);
        if (isGrounded() || !manualGravity) return;

        Vector velocity = currentProjectile.getVelocity();
        velocity.setY(velocity.getY() - gravity);
        currentProjectile.setVelocity(velocity);
    }

    /**
     * Handles the logic for the projectile when rolling on the ground.
     */
    private void moveGrounded() {
        if (!isGrounded()) {
            // Reset grounded timer
            groundedTicks = 0;
            return;
        }

        Projectile p = getCurrentProjectile();

        // Check if still on a solid block (could have rolled off)
        Block block = p.getLocation().getBlock();
        Material inBlockType = block.getType();
        Material onBlockType = block.getRelative(gravity >= 0 ? BlockFace.DOWN : BlockFace.UP).getType();
        if (!inBlockType.isSolid() && !onBlockType.isSolid() && !isSticky()) {
            grounded = false;
            return;
        }

        // Stop completely if the velocity is very low
        if (isStill()) p.setVelocity(new Vector(0, 0, 0));

        // Apply surface friction
        p.setVelocity(p.getVelocity().multiply(getFrictionFactor()));

        // Explode if grounded for too long
        if (groundExplosionTimerTicks >= 0 && groundedTicks++ >= groundExplosionTimerTicks) explode();
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

        // Bounce sound
        double bounceMagnitude = hitBlockFace != null ? Math.abs(velocity.dot(hitBlockFace.getDirection())) : velocity.length();
        float volume = (float) bounceMagnitude;
        float pitch = (float) (1.8f / (bounceMagnitude + 1f));
        world.playSound(hitLocation, Sound.BLOCK_ANVIL_FALL, SoundCategory.NEUTRAL, volume, pitch);

        if ((groundBounce && Math.abs(velocity.getY()) <= Y_VELOCITY_CONSIDERED_GROUNDED) || isSticky()) {
            // Set grounded
            grounded = true;
            velocity.setY(0);
            oldProjectile.setGravity(false);

            // If sticky, stop completely
            if (isSticky()) velocity.multiply(0);
        } else {
            // Apply bounce physics
            bounce(velocity, hitBlockFace);
        }

        currentProjectile = world.spawn(hitLocation, PROJECTILE_CLASS, e -> {
            if (displayItem != null) e.setItem(displayItem);
            e.setVelocity(velocity);
            e.setGravity(oldProjectile.hasGravity());
            e.setShooter(oldProjectile.getShooter());
            e.setFireTicks(oldProjectile.getFireTicks());
            //noinspection deprecation
            e.setPersistent(oldProjectile.isPersistent());
        });
    }

    /**
     * Applies bounce physics on the given velocity vector according to the block face that was hit.
     * <p>
     * If no block face was hit, the bounce simply goes the opposite direction.
     *
     * @param velocity     The velocity to apply the bounce physics on
     * @param hitBlockFace The block face that was hit, or null if no block face was hit
     */
    private void bounce(@NotNull Vector velocity, @Nullable BlockFace hitBlockFace) {
        if (hitBlockFace != null && hitBlockFace.getDirection().lengthSquared() != 0) {
            if (hitBlockFace.getModX() != 0) {
                velocity.setX(Math.abs(velocity.getX()) * hitBlockFace.getModX());
                velocity.setX(velocity.getX() * getRestitutionFactor());
            } else {
                velocity.setX(velocity.getX() * getFrictionFactor());
            }
            if (hitBlockFace.getModY() != 0) {
                velocity.setY(Math.abs(velocity.getY()) * hitBlockFace.getModY());
                velocity.setY(velocity.getY() * getRestitutionFactor());
            } else {
                velocity.setY(velocity.getY() * getFrictionFactor());
            }
            if (hitBlockFace.getModZ() != 0) {
                velocity.setZ(Math.abs(velocity.getZ()) * hitBlockFace.getModZ());
                velocity.setZ(velocity.getZ() * getRestitutionFactor());
            } else {
                velocity.setZ(velocity.getZ() * getFrictionFactor());
            }
        } else {
            velocity.multiply(-1);
            velocity.multiply(getRestitutionFactor());
        }
    }


    /**
     * Improves the given hit location by simulating projectile movement between ticks.
     * <p>
     * The projectile's location at the moment of the hit is always slightly in front since the real hit would be
     * between ticks.
     * <p>
     * The hit location can be made more accurate by simulating the extension of the travel path as if the projectile
     * continued to fly in the same direction. This can only be done easily if the block is full (i.e., not slabs,
     * fence, etc.), so we first check if the projectile's location is already "inside" the block that was hit, in which
     * case the hit location cannot be improved.
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
     * Returns the most accurate hit location based on the given last location and velocity of a projectile that hit the
     * given block face.
     * <p>
     * The resulting location is the center of the projectile at the moment of impact (one radius' distance from the
     * surface).
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
}
