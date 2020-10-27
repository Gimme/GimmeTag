package me.gimme.gimmetag.item.items;

import me.gimme.gimmetag.config.type.AbilityItemConfig;
import me.gimme.gimmetag.item.AbilityItem;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * Stand still and become invisible for a duration or until you move again.
 */
public class PinkWard extends AbilityItem {

    private static final String NAME = ChatColor.DARK_RED + "Pink Ward";
    private static final Material MATERIAL = Material.REDSTONE_TORCH;
    private static final String INFO = "Become invisible while standing still";

    private static final PotionEffect SLOW_EFFECT = new PotionEffect(PotionEffectType.SLOW, 15, 1000, false, false); // Prevents moving
    private static final PotionEffect INVISIBILITY_EFFECT = new PotionEffect(PotionEffectType.INVISIBILITY, 2, 0, false, false);
    private static final List<PotionEffect> VISUAL_EFFECTS = Arrays.asList(
            new PotionEffect(PotionEffectType.HUNGER, 2, 0, false, false, false),
            new PotionEffect(PotionEffectType.WITHER, 2, 0, false, false, false)
    );

    private final Plugin plugin;

    public PinkWard(@NotNull String id, @NotNull AbilityItemConfig config, @NotNull Plugin plugin) {
        super(id, NAME, MATERIAL, config);

        this.plugin = plugin;

        setInfo(INFO);
    }

    @Override
    protected boolean onUse(@NotNull ItemStack itemStack, @NotNull Player user) {
        user.setSprinting(false);
        user.addPotionEffect(SLOW_EFFECT);

        new BukkitRunnable() {
            private Location startLocation;

            private int ticks;

            @Override
            public void run() {
                if (ticks == 15) startLocation = user.getLocation().clone();

                if ((getDurationTicks() >= 0 && ++ticks > getDurationTicks())
                        || (startLocation != null && distanceSquared2d(user.getLocation(), startLocation) > Math.pow(0.1, 2))) {
                    cancel();
                    return;
                }

                user.addPotionEffect(INVISIBILITY_EFFECT);
                user.addPotionEffects(VISUAL_EFFECTS);
            }
        }.runTaskTimer(plugin, 0, 1);

        return true;
    }

    @Override
    protected void onCreate(@NotNull ItemStack itemStack, @NotNull ItemMeta itemMeta) {
    }

    private static double distanceSquared2d(@NotNull Location location1, @NotNull Location location2) {
        Vector v1 = location1.toVector();
        Vector v2 = location2.toVector();

        v1.setY(0);
        v2.setY(0);

        return v1.distanceSquared(v2);
    }
}
