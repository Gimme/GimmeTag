package me.gimme.gimmetag.item.items;

import me.gimme.gimmetag.config.type.AbilityItemConfig;
import me.gimme.gimmetag.item.ContinuousAbilityItem;
import me.gimme.gimmetag.utils.outline.CollectionOutlineEffect;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Shows players below a certain health level through walls.
 */
public class BloodHunt extends ContinuousAbilityItem {

    private static final String NAME = ChatColor.DARK_RED + "Blood Hunt";
    private static final Material MATERIAL = Material.ARROW;
    private static final List<String> INFO = Collections.singletonList("See low health targets");

    private final Plugin plugin;
    private final double healthThreshold;
    private final double range;

    /**
     * Creates a new continuous ability item with the specified name, display name, item type and configuration.
     *
     * @param id     a unique name for this item
     * @param config a config containing values to be applied to this ability item's variables
     */
    public BloodHunt(@NotNull String id, @NotNull AbilityItemConfig config, double healthThreshold, double range, @NotNull Plugin plugin) {
        super(id, NAME, MATERIAL, config);

        this.plugin = plugin;
        this.healthThreshold = healthThreshold;
        this.range = range;

        setInfo(INFO);
    }

    @Override
    protected @NotNull ContinuousUse createContinuousUse(@NotNull ItemStack itemStack, @NotNull Player user) {
        CollectionOutlineEffect outlineEffect = new CollectionOutlineEffect(plugin, user, null);
        outlineEffect.show();

        double rangeSquared = range * range;

        return new ContinuousUse() {
            @Override
            public void onCalculate() {
                List<Player> targets = user.getWorld().getPlayers().stream()
                        .filter(p -> user.getLocation().distanceSquared(p.getLocation()) < rangeSquared)
                        .filter(p -> {
                            AttributeInstance maxHealthAttribute = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                            if (maxHealthAttribute == null) return false;
                            return p.getHealth() < healthThreshold * maxHealthAttribute.getValue() + 0.001;
                        })
                        .collect(Collectors.toList());

                outlineEffect.setTargets(targets);
                outlineEffect.refresh();
            }

            @Override
            public void onTick() {
            }

            @Override
            public void onFinish() {
                outlineEffect.hide();
            }
        };
    }

    @Override
    protected void onCreate(@NotNull ItemStack itemStack, @NotNull ItemMeta itemMeta) {
    }
}
