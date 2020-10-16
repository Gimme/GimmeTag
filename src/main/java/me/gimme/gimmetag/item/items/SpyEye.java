package me.gimme.gimmetag.item.items;

import me.gimme.gimmetag.config.type.AbilityItemConfig;
import me.gimme.gimmetag.item.ContinuousAbilityItem;
import me.gimme.gimmetag.utils.outline.CollectionOutlineEffect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * During this item's effect, you can see all other players through walls. As a side effect, you can't move, the screen
 * goes dark and all other players can see you through walls as well.
 */
public class SpyEye extends ContinuousAbilityItem {
    private static final String NAME = "Spy Eye";
    private static final Material MATERIAL = Material.ENDER_EYE;
    private static final List<String> INFO = Collections.singletonList("See other players through walls");

    private static final int POTION_TICKS = 2;

    private final Plugin plugin;
    private final boolean selfGlow;
    private final List<PotionEffect> potionEffects;
    private final PotionEffect glowEffect;

    public SpyEye(@NotNull String id, @NotNull AbilityItemConfig config, boolean selfGlow, @NotNull Plugin plugin) {
        super(id, NAME, MATERIAL, config);

        this.plugin = plugin;
        this.selfGlow = selfGlow;

        setInfo(INFO);
        showLevel(false);
        setTicksPerCalculation(20);
        potionEffects = Arrays.asList(
                new PotionEffect(PotionEffectType.BLINDNESS, POTION_TICKS + 20, 0, false, false),
                new PotionEffect(PotionEffectType.SLOW, POTION_TICKS, getAmplifier(), false, false),
                new PotionEffect(PotionEffectType.JUMP, POTION_TICKS, 200, false, false, false) // Prevents jumping
        );
        glowEffect = new PotionEffect(PotionEffectType.GLOWING, POTION_TICKS, 0, false, false);
    }

    @Override
    protected void onCreate(@NotNull ItemStack itemStack, @NotNull ItemMeta itemMeta) {
    }

    @Override
    protected @NotNull ContinuousUse createContinuousUse(@NotNull ItemStack itemStack, @NotNull Player user) {
        user.setSprinting(false);

        CollectionOutlineEffect outlineEffect = new CollectionOutlineEffect(plugin, user, null);
        outlineEffect.show();

        return new ContinuousUse() {
            @Override
            public void onCalculate() {
                outlineEffect.setTargets(user.getWorld().getPlayers());
                outlineEffect.refresh();
            }

            @Override
            public void onTick() {
                for (PotionEffect potionEffect : potionEffects) {
                    user.addPotionEffect(potionEffect);
                }
                if (selfGlow) user.addPotionEffect(glowEffect);
            }

            @Override
            public void onFinish() {
                outlineEffect.hide();
            }
        };
    }
}
