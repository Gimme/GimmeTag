package me.gimme.gimmetag.item.items;

import me.gimme.gimmetag.config.AbilityItemConfig;
import me.gimme.gimmetag.item.ContinuousAbilityItem;
import me.gimme.gimmetag.tag.TagManager;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class HunterCompass extends ContinuousAbilityItem {

    private static final Material TYPE = Material.COMPASS;
    private static final List<String> LORE = Arrays.asList(
            "" + ChatColor.ITALIC + ChatColor.YELLOW + "(Right click to activate)",
            "Points to nearest runner");

    private final TagManager tagManager;
    private final boolean glowOnlyWhenActive;

    public HunterCompass(@NotNull AbilityItemConfig config, @NotNull TagManager tagManager) {
        super(
                "Hunter Compass",
                TYPE,
                config
        );

        this.tagManager = tagManager;
        this.glowOnlyWhenActive = hasDuration();

        if (glowOnlyWhenActive) disableGlow();
    }

    @Override
    protected void onCreate(@NotNull ItemStack itemStack, @NotNull ItemMeta itemMeta) {
        itemMeta.setLore(LORE);
    }

    @Override
    protected @NotNull ContinuousUse createContinuousUse(@NotNull ItemStack itemStack, @NotNull Player user) {
        if (glowOnlyWhenActive) setGlowing(itemStack, true);
        setTarget(itemStack, null);

        return new ContinuousUse() {
            private Entity closestTarget;

            @Override
            public void onCalculate() {
                closestTarget = tagManager.getClosestRunner(user);
            }

            @Override
            public void onTick() {
                if (closestTarget != null) setTarget(itemStack, closestTarget.getLocation());
            }

            @Override
            public void onFinish() {
                if (glowOnlyWhenActive) setGlowing(itemStack, false);
            }
        };
    }

    private static void setTarget(@NotNull ItemStack item, @Nullable Location location) {
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());

        // Necessary check because if the item was dropped, it no longer has a CompassMeta
        if (!(meta instanceof CompassMeta)) return;
        CompassMeta compassMeta = (CompassMeta) meta;

        // For some reason setting the Lodestone location to null does not work, so we make it point to (0, 0, 0) instead
        if (location == null) compassMeta.setLodestone(new Location(Bukkit.getWorlds().get(0), 0, 0, 0));
        else compassMeta.setLodestone(location);

        item.setItemMeta(compassMeta);
    }
}
