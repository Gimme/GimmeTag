package me.gimme.gimmetag.item.items;

import me.gimme.gimmetag.config.type.BouncyProjectileConfig;
import me.gimme.gimmetag.item.CustomItem;
import me.gimme.gimmetag.item.entities.BouncyProjectile;
import me.gimme.gimmetag.utils.Ticks;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * A trident that can be thrown and pulls in anyone it hits. Like the League of Legends champion Pyke's Q ability.
 */
public class PykesHook extends CustomItem implements Listener {

    private static final String NAME = "Pyke's Hook";
    private static final Material MATERIAL = Material.TRIDENT;
    private static final EntityType ENTITY_TYPE = EntityType.TRIDENT;

    private final Plugin plugin;

    private final BouncyProjectileConfig config;
    private final int cooldownTicks;
    private final double speed;
    private final int maxTicks;
    private final double power;

    public PykesHook(@NotNull String id, @NotNull BouncyProjectileConfig config, @NotNull Plugin plugin) {
        super(id, NAME, MATERIAL);

        this.plugin = plugin;
        this.config = config;
        this.cooldownTicks = Ticks.secondsToTicks(config.getCooldown());
        this.speed = config.getSpeed();
        this.maxTicks = Ticks.secondsToTicks(config.getMaxExplosionTimer());
        this.power = config.getPower();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    protected void onCreate(@NotNull ItemStack itemStack, @NotNull ItemMeta itemMeta) {
    }

    @EventHandler
    private void onLaunch(ProjectileLaunchEvent event) {
        if (event.isCancelled()) return;

        Projectile projectile = event.getEntity();
        ProjectileSource projectileSource = projectile.getShooter();

        if (projectile.getType() != ENTITY_TYPE) return;
        if (!(projectileSource instanceof Player)) return;
        Player thrower = (Player) projectileSource;

        ItemStack main = thrower.getInventory().getItemInMainHand();
        ItemStack off = thrower.getInventory().getItemInOffHand();
        boolean usedMain = main.getType() == MATERIAL;

        ItemStack usedItemStack = usedMain ? main : off;

        if (!isThisCustomItem(usedItemStack)) return;

        // Re-add the thrown item to the hand
        if (!config.isConsumable()) {
            if (usedMain) thrower.getInventory().setItemInMainHand(createItemStack());
            else thrower.getInventory().setItemInOffHand(createItemStack());
        }

        thrower.setCooldown(usedItemStack.getType(), cooldownTicks);

        projectile.setVelocity(projectile.getVelocity().multiply(speed));

        BouncyProjectile bouncyProjectile = new BouncyProjectile(plugin, projectile, thrower, maxTicks);
        BouncyProjectileConfig.init(bouncyProjectile, config);

        bouncyProjectile.setOnHitEntity((p, e) -> {
            Vector velocity = p.getVelocity().clone();
            velocity.setY(0);
            velocity.normalize();
            velocity.multiply(-1);
            velocity.multiply(power);
            velocity.setY(0.5 + 0.01 * power);

            e.setVelocity(velocity);
        });
    }
}
