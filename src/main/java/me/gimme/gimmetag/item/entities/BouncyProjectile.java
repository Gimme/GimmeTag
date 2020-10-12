package me.gimme.gimmetag.item.entities;

import me.gimme.gimmetag.sfx.PlayableSound;
import me.gimme.gimmetag.utils.outline.OutlineEffect;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * Represents a projectile that bounces when it hits a surface.
 * <p>
 * It only disappears when it "explodes", which happens, at the latest, after a set maximum amount of time.
 */
public class BouncyProjectile implements Listener {

    private static final Class<? extends ThrowableProjectile> PROJECTILE_CLASS = Snowball.class;
    private static final int TRAIL_FREQUENCY_TICKS = 1;                 // Ticks between each trail update
    private static final double Y_VELOCITY_CONSIDERED_GROUNDED = 0.15;  // The y-velocity when the bouncing should stop
    private static final double RADIUS = 0.07;                          // Radius of the projectile
    private static final double DEFAULT_GRAVITY = 0.028;                // ~0.028 is the standard gravity for a snowball

    private final UUID uuid;
    private final LivingEntity source;
    private final boolean sourceIsPlayer;
    private final Class<? extends Projectile> projectileClass;
    private final boolean isArrow;
    private final BukkitRunnable updateTask;
    private final BukkitRunnable explosionTimerTask;
    private final BukkitRunnable trailTask;
    private final OutlineEffect outlineEffect;

    @Nullable
    private BiConsumer<@NotNull Projectile, @NotNull Collection<@NotNull Entity>> onExplode;
    @Nullable
    private BiConsumer<@NotNull Projectile, @NotNull Entity> onHitEntity;
    private int groundExplosionTimerTicks = -1;
    private boolean manualGravity;
    private double gravity = DEFAULT_GRAVITY;
    private double restitutionFactor = 0.45;
    private double frictionFactor = 0.8;
    private boolean sticky;
    private boolean showTrail;
    private boolean showBounceMarks;
    private double radius;
    private double damageOnDirectHit;
    private boolean consumeOnDirectHit;
    private boolean friendlyFire;
    @Nullable
    private PlayableSound explosionSound;
    @Nullable
    private ItemStack displayItem;
    private Particle trailParticle = Particle.END_ROD;

    private boolean grounded;
    private int groundedTicks; // Amount of ticks the projectile has been rolling on the ground

    private final Set<UUID> spawnedProjectiles = new HashSet<>();
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

        BouncyProjectile bouncyProjectile = new BouncyProjectile(plugin, projectile, source, maxTicks);
        bouncyProjectile.setDisplayItem(displayItem);
        return bouncyProjectile;
    }

    /**
     * Creates a new bouncy projectile out of the given normal projectile. The given normal projectile can have been
     * spawned from anywhere but the specified source living entity will be set as the shooter.
     *
     * @param plugin     the plugin to register the events with
     * @param projectile the projectile to turn into a bouncy projectile
     * @param source     the living entity that will be set as the shooter of the projectile
     * @param maxTicks   the max amount of ticks this projectile can live before being removed
     */
    public BouncyProjectile(@NotNull Plugin plugin, @NotNull Projectile projectile, @NotNull LivingEntity source, int maxTicks) {

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        this.uuid = UUID.randomUUID();
        this.source = source;
        this.sourceIsPlayer = source instanceof Player;
        this.projectileClass = projectile.getClass();
        this.isArrow = AbstractArrow.class.isAssignableFrom(projectileClass);
        setCurrentProjectile(projectile);

        // Remove the entity when the server stops
        //noinspection deprecation
        getCurrentProjectile().setPersistent(false);


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

                p.getWorld().spawnParticle(trailParticle,
                        p.getLocation() // Align the trail to fit the actual path better
                                .add(p.getVelocity().multiply(-1).multiply(0.3))
                                .add(0, RADIUS, 0),
                        1, 0, 0, 0, 0, null, true);
            }
        };
        trailTask.runTaskTimer(plugin, TRAIL_FREQUENCY_TICKS, TRAIL_FREQUENCY_TICKS);

        outlineEffect = OutlineEffect.personalEffect(plugin, source, entityId -> entityId == getCurrentProjectile().getEntityId());
    }

    /**
     * Makes the projectile explode (figuratively) and disappear from the world.
     */
    public void explode() {
        Projectile projectile = getCurrentProjectile();
        if (explosionSound != null) explosionSound.playAt(projectile.getLocation());

        if (onExplode != null) {
            Location location = projectile.getLocation().clone();
            double extendedRadius = radius + 3;
            Collection<Entity> nearbyLivingEntities = radius <= 0 ? new ArrayList<>() : projectile.getWorld().getNearbyEntities(
                    location, extendedRadius, extendedRadius, extendedRadius,
                    e -> {
                        if (!checkFriendlyFire(e)) return false;

                        double halfHeight = e.getHeight() / 2;

                        location.subtract(0, halfHeight, 0);
                        boolean inRange = e.getType().isAlive() && e.getLocation().distanceSquared(location) <= radius * radius;
                        location.add(0, halfHeight, 0);

                        return inRange;
                    }
            );

            onExplode.accept(projectile, nearbyLivingEntities);
        }
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
    public void setOnExplode(@Nullable BiConsumer<@NotNull Projectile, @NotNull Collection<@NotNull Entity>> onExplode) {
        this.onExplode = onExplode;
    }

    /**
     * Sets a consumer to define what happens when the projectile hits an entity.
     *
     * @param onHitEntity the consumer to set
     */
    public void setOnHitEntity(@Nullable BiConsumer<@NotNull Projectile, @NotNull Entity> onHitEntity) {
        this.onHitEntity = onHitEntity;
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
     * Sets the radius of the explosion effect.
     * <p>
     * Only living entities that have the center of their body within this range will be affected by the explosion.
     *
     * @param radius the radius of the explosion effect
     */
    public void setRadius(double radius) {
        this.radius = radius;
    }

    /**
     * Sets the damage that this projectile deals to entities it hits directly.
     *
     * @param damageOnDirectHit the damage on direct hits
     */
    public void setDamageOnDirectHit(double damageOnDirectHit) {
        this.damageOnDirectHit = damageOnDirectHit;
    }

    /**
     * Sets if the projectile should disappear when it hits an entity.
     * <p>
     * This is useful together with {@link this#setOnHitEntity(BiConsumer)} for items with single target effects.
     *
     * @param consumeOnDirectHit if the projectile should disappear when it hits an entity
     */
    public void setConsumeOnDirectHit(boolean consumeOnDirectHit) {
        this.consumeOnDirectHit = consumeOnDirectHit;
    }

    /**
     * Sets if entities from the same team as the thrower of the projectile get affected by the explosion effect.
     *
     * @param friendlyFire if entities from the same team get affected by the explosion
     */
    public void setFriendlyFire(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
    }

    /**
     * Sets the sound that gets played on explosion, or null for no sound.
     *
     * @param explosionSound the sound to play on explosion, or null for no sound
     */
    public void setExplosionSound(@Nullable PlayableSound explosionSound) {
        this.explosionSound = explosionSound;
    }

    /**
     * Sets if the projectile should be glowing (outline effect seen through walls).
     *
     * @param glowing If the projectile should be glowing
     */
    public void setGlowing(boolean glowing) {
        if (glowing) {
            if (outlineEffect.show()) {
                ProjectileSource shooter = getCurrentProjectile().getShooter();
                if (shooter instanceof Player) OutlineEffect.setColor(null, (Player) shooter, getCurrentProjectile());
                OutlineEffect.refresh(getCurrentProjectile());
            }
        } else {
            if (outlineEffect.hide()) OutlineEffect.refresh(getCurrentProjectile());
        }
    }

    /**
     * Sets the particle to use for the trail of the projectile.
     *
     * @param trailParticle the particle to use for the trail
     */
    public void setTrailParticle(@NotNull Particle trailParticle) {
        this.trailParticle = trailParticle;
    }

    /**
     * Sets the item to be displayed on the projectile.
     * <p>
     * This only affects throwable projectiles and does not work for things like arrows.
     *
     * @param displayItem the item to be displayed on the projectile
     */
    public void setDisplayItem(@Nullable ItemStack displayItem) {
        this.displayItem = displayItem;
    }

    /**
     * Sets the current active projectile.
     * <p>
     * This should always be set to the projectile, spawned from this object, that is currently flying/rolling in the
     * world.
     *
     * @param projectile the projectile to set as current
     */
    private void setCurrentProjectile(@NotNull Projectile projectile) {
        this.currentProjectile = projectile;
        spawnedProjectiles.add(projectile.getUniqueId());
    }

    /**
     * Returns if the specified entity was spawned from this bouncy projectile.
     * <p>
     * This means either the original launched projectile or any that spawned after a bounce.
     *
     * @param entity the entity to check if spawned from this
     * @return if the specified entity was spawned from this bouncy projectile
     */
    private boolean isFromThisBouncyProjectile(@NotNull Entity entity) {
        return spawnedProjectiles.contains(entity.getUniqueId());
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

    /**
     * Sets if the projectile should stick to the first surface it hits.
     *
     * @param sticky if the projectile should stick to the first surface it hits
     */
    public void setSticky(boolean sticky) {
        this.sticky = sticky;
    }

    /**
     * Returns if the projectile sticks to the first surface it hits.
     *
     * @return if the projectile sticks to the first surface it hits
     */
    public boolean isSticky() {
        return sticky;
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

        Vector velocity = getCurrentProjectile().getVelocity();
        velocity.setY(velocity.getY() - gravity);
        getCurrentProjectile().setVelocity(velocity);
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
        if (!oldProjectile.getUniqueId().equals(getCurrentProjectile().getUniqueId())) return;

        Vector velocity = oldProjectile.getVelocity().clone();
        Block hitBlock = event.getHitBlock();
        BlockFace hitBlockFace = event.getHitBlockFace();
        Entity hitEntity = event.getHitEntity();
        World world = oldProjectile.getWorld();

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

        // On hit entity
        if (hitEntity != null) {
            if (onHitEntity != null && checkFriendlyFire(hitEntity)) onHitEntity.accept(oldProjectile, hitEntity);
            // Projectile will be removed after modifying damage in EntityDamageByEntityEvent
            if (consumeOnDirectHit) return;
        }

        if (isArrow) {
            // If it is a sticky arrow-like projectile, it can be left stuck in the ground, as it is, without having to spawn a new one.
            if (isSticky()) {
                grounded = true;
                // Don't allow picking it back up
                ((AbstractArrow) getCurrentProjectile()).setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                return;
            }

            // Remove previous projectile, which is left stuck in the ground
            oldProjectile.remove();
        }


        // Check if the bounce was on the ground (or the roof if gravity is inverted)
        boolean groundBounce = hitBlockFace != null && ((gravity > 0 && hitBlockFace.getModY() > 0) || (gravity < 0 && hitBlockFace.getModY() < 0));

        // Bounce logic
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

        // Spawn new projectile with the post-bounce velocity
        setCurrentProjectile(world.spawn(hitLocation, projectileClass, e -> {
            if (displayItem != null && e instanceof ThrowableProjectile) ((ThrowableProjectile) e).setItem(displayItem);
            e.setVelocity(velocity);
            e.setGravity(oldProjectile.hasGravity());
            e.setShooter(source);
            e.setFireTicks(oldProjectile.getFireTicks());
            //noinspection deprecation
            e.setPersistent(oldProjectile.isPersistent());
            if (sourceIsPlayer && outlineEffect.isShown()) OutlineEffect.setColor(null, (Player) source, e);
        }));
    }

    /**
     * Makes the projectile deal damage on direct hits if enabled and removes it if it should be consumed on direct
     * hit.
     */
    @EventHandler(priority = EventPriority.LOW)
    private void onDirectHitDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!isFromThisBouncyProjectile(event.getDamager())) return;

        event.setDamage(damageOnDirectHit);
        if (consumeOnDirectHit) remove();
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
     * Checks if the given hit entity is allowed to be affected by this projectile in regards to friendly fire.
     *
     * @param hitEntity the entity to check if allowed to be hit
     * @return if the given hit entity is allowed to be affected by this projectile
     */
    private boolean checkFriendlyFire(@NotNull Entity hitEntity) {
        return !(!friendlyFire && sourceIsPlayer && fromSameTeam((Player) source, hitEntity));
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

    /**
     * Returns if the given other entity is on the specified player's team.
     *
     * @param player the player to get the team from
     * @param other  the entity to check if on the player's team
     * @return if the other entity is on the player's team
     */
    private static boolean fromSameTeam(@NotNull Player player, @NotNull Entity other) {
        if (player.getUniqueId().equals(other.getUniqueId())) return true;

        Scoreboard scoreboard = player.getScoreboard();
        Team team1 = scoreboard.getEntryTeam(player.getName());
        Team team2 = scoreboard.getEntryTeam(other.getName());
        return team1 != null && team1.equals(team2);
    }
}
