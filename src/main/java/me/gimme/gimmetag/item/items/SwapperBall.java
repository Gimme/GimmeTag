package me.gimme.gimmetag.item.items;

import me.gimme.gimmetag.config.type.BouncyProjectileConfig;
import me.gimme.gimmetag.item.BouncyProjectileItem;
import me.gimme.gimmetag.sfx.SoundEffects;
import me.gimme.gimmetag.tag.TagManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SwapperBall extends BouncyProjectileItem {

    private static final String NAME = ChatColor.LIGHT_PURPLE + "Swapper Ball";
    private static final Material MATERIAL = Material.ENDER_PEARL;
    private static final List<String> INFO = Collections.singletonList("Swap positions with the hit player");

    private final boolean allowHunterSwap;
    private final TagManager tagManager;

    public SwapperBall(@NotNull BouncyProjectileConfig config, boolean allowHunterSwap, @NotNull Plugin plugin,
                       @NotNull TagManager tagManager) {
        super(NAME, MATERIAL, config, plugin);

        this.allowHunterSwap = allowHunterSwap;
        this.tagManager = tagManager;

        setInfo(INFO);
        setDisplayItem(MATERIAL, true);
        setTrailParticle(Particle.DRAGON_BREATH);
    }

    @Override
    protected void onCreate(@NotNull ItemStack itemStack, @NotNull ItemMeta itemMeta) {
    }

    @Override
    protected void onExplode(@NotNull Projectile projectile, @NotNull Collection<@NotNull Entity> livingEntities) {
    }

    @Override
    protected void onHitEntity(@NotNull Projectile projectile, @NotNull Entity entity) {
        if (entity.getType() != EntityType.PLAYER) return; // Didn't hit a player

        Entity shooter = (Entity) projectile.getShooter();
        assert shooter != null;

        swap(shooter, entity);
    }

    private void swap(@NotNull Entity shooter, @NotNull Entity hit) {
        // Don't allow swapping with hunters if disabled
        if (!allowHunterSwap && tagManager.getHunters().contains(hit.getUniqueId())) return;

        // Don't swap if either is dead
        if (shooter.isDead() || hit.isDead()) return;

        Location shooterLocation = shooter.getLocation();
        Location hitLocation = hit.getLocation();

        shooterLocation.setDirection(hitLocation.toVector().subtract(shooterLocation.toVector()));
        hitLocation.setDirection(shooterLocation.toVector().subtract(hitLocation.toVector()));

        if (shooter.getType() == EntityType.PLAYER) playSound((Player) shooter);
        if (hit.getType() == EntityType.PLAYER) playSound((Player) hit);

        shooter.teleport(hitLocation);
        hit.teleport(shooterLocation);
    }


    private static void playSound(@NotNull Player player) {
        SoundEffects.TELEPORT.playAt(player);
    }
}
