package me.gimme.gimmetag.item.items;

import me.gimme.gimmetag.config.BouncyProjectileConfig;
import me.gimme.gimmetag.item.BouncyProjectileItem;
import me.gimme.gimmetag.sfx.SoundEffects;
import me.gimme.gimmetag.utils.ChatColorConversion;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A smoke grenade is a projectile that explodes after landing on the ground and releases a cloud of smoke, which blocks
 * vision and blinds people inside it.
 */
public class SmokeGrenade extends BouncyProjectileItem {

    private static final String NAME = "Smoke Grenade";
    private static final Material MATERIAL = Material.CLAY_BALL;

    private static final Particle PARTICLE = Particle.REDSTONE;
    private static final int BASE_PARTICLE_COUNT = 20;
    private static final int INTERVAL_TICKS = 1;

    private static final double EYE_HEIGHT = 0.5; // Of player characters for checking if to blind them
    private static final double HEIGHT_TO_WIDTH_RATIO = 0.6; // Of the potion effect area
    private static final int POTION_EFFECT_FADE_TICKS = 30; // Extra duration in ticks to match the slow fade of the particles
    private static final double PARTICLE_TO_EFFECT_RADIUS_RATIO = 0.3;
    private static final double PARTICLE_CLUSTERS_OFFSET_RATIO = 0.6;
    private static final double TOP_CENTER_CLUSTER_Y_OFFSET_RATIO = 0.75;

    private static final List<PotionEffect> AREA_POTION_EFFECTS =
            Collections.singletonList(new PotionEffect(PotionEffectType.BLINDNESS, 22, 0));

    private final Plugin plugin;
    private final double particleRadius;
    private final int rgb;
    private final boolean useTeamColor;

    public SmokeGrenade(@NotNull BouncyProjectileConfig config, int rgb, boolean useTeamColor, @NotNull Plugin plugin) {
        super(NAME, MATERIAL, config, plugin);

        this.plugin = plugin;
        this.particleRadius = getRadius() * PARTICLE_TO_EFFECT_RADIUS_RATIO;
        this.rgb = rgb;
        this.useTeamColor = useTeamColor;

        setDisplayItem(MATERIAL, false);
        setExplosionSound(SoundEffects.SMOKE_EXPLOSION);
    }

    @Override
    protected void onCreate(@NotNull ItemStack itemStack, @NotNull ItemMeta itemMeta) {
    }

    @Override
    protected void onExplode(@NotNull Projectile projectile, @NotNull Collection<@NotNull Entity> livingEntities) {
        Location location = projectile.getLocation();
        ProjectileSource shooter = projectile.getShooter();

        Color color = null;
        if (useTeamColor && (shooter instanceof Player)) color = getTeamColor((Player) shooter);
        if (color == null) color = Color.fromRGB(rgb);

        double offset = getRadius() * PARTICLE_CLUSTERS_OFFSET_RATIO;

        startSmoke(location.clone().add(offset, 0, offset), color);
        startSmoke(location.clone().add(offset, 0, -offset), color);
        startSmoke(location.clone().add(-offset, 0, offset), color);
        startSmoke(location.clone().add(-offset, 0, -offset), color);
        startSmoke(location.clone().add(0, offset * TOP_CENTER_CLUSTER_Y_OFFSET_RATIO, 0), color);
        startAreaEffect(location);
    }

    @Override
    protected void onHitEntity(@NotNull Projectile projectile, @NotNull Entity entity) {
    }

    private void startSmoke(@NotNull Location location, @NotNull Color color) {
        double thickness = getPower();

        spawnParticle(location, thickness * 10, particleRadius, color);

        for (int i = 0; i <= getDurationTicks(); i += INTERVAL_TICKS) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    spawnParticle(location, thickness, particleRadius, color);
                }
            }.runTaskLater(plugin, i);
        }
    }

    private void startAreaEffect(@NotNull Location location) {
        World world = Objects.requireNonNull(location.getWorld());

        new BukkitRunnable() {
            private int ticksLeft = getDurationTicks() + POTION_EFFECT_FADE_TICKS;

            @Override
            public void run() {
                if (ticksLeft-- < 0) {
                    cancel();
                    return;
                }

                double radius = getRadius();
                Collection<Entity> nearbyLivingEntities = world.getNearbyEntities(
                        location.clone().add(0, -EYE_HEIGHT, 0), radius, radius * HEIGHT_TO_WIDTH_RATIO, radius,
                        e -> e.getType().isAlive()
                );

                for (Entity entity : nearbyLivingEntities) {
                    LivingEntity livingEntity = (LivingEntity) entity;

                    for (PotionEffect potionEffect : AREA_POTION_EFFECTS) {
                        PotionEffect currentEffect = livingEntity.getPotionEffect(potionEffect.getType());
                        if (currentEffect != null && currentEffect.getDuration() >= potionEffect.getDuration())
                            continue;

                        livingEntity.addPotionEffect(potionEffect);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }


    private static void spawnParticle(@NotNull Location location, double thickness, double particleRadius, @NotNull Color color) {
        int particleCount = (int) Math.round(thickness * BASE_PARTICLE_COUNT * particleRadius * particleRadius);
        World world = Objects.requireNonNull(location.getWorld());
        world.spawnParticle(PARTICLE, location, particleCount, particleRadius, particleRadius, particleRadius, 0,
                new Particle.DustOptions(color, 4.0f), true);
    }

    @Nullable
    private static Color getTeamColor(@NotNull Player player) {
        Team team = player.getScoreboard().getEntryTeam(player.getName());
        if (team == null) return null;

        return Color.fromRGB(ChatColorConversion.toRGB(team.getColor()));
    }
}
