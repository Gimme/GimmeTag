package me.gimme.gimmetag.item.items;

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
    private static final List<String> LORE = Collections.singletonList("" + ChatColor.ITALIC + ChatColor.YELLOW + "(Right click to activate)");

    private TagManager tagManager;

    public HunterCompass(@NotNull TagManager tagManager) {
        super(
                "Hunter Compass",
                TYPE,
                true,
                false
        );

        setUseResponseMessage("Compass: points to closest runner");
        this.tagManager = tagManager;
    }

    @Override
    protected void onCreate(@NotNull ItemStack itemStack, @NotNull ItemMeta itemMeta) {
        itemMeta.setLore(LORE);
    }

    @Override
    protected @NotNull ContinuousUse createContinuousUse(@NotNull ItemStack itemStack, @NotNull Player user) {
        return new ContinuousUse() {
            Entity closestTarget = null;

            @Override
            public void onCalculate() {
                closestTarget = tagManager.getClosestRunner(user);
            }

            @Override
            public void onTick() {
                setTarget(itemStack, closestTarget != null ? closestTarget.getLocation() : null);
            }

            @Override
            public void onFinish() {
                setTarget(itemStack, null);
            }
        };
    }

    private static void setTarget(@NotNull ItemStack item, @Nullable Location location) {
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        if (!(meta instanceof CompassMeta))
            return; // Can happen if the item has been dropped on the ground: CraftMetaItem instead of CraftMetaCompass

        CompassMeta compassMeta = (CompassMeta) meta;
        if (location == null) // Lodestone location should be set to null but for some reason it does not work
            compassMeta.setLodestone(new Location(Bukkit.getWorlds().get(0), 0, 0, 0));
        else compassMeta.setLodestone(location);
        item.setItemMeta(compassMeta);
    }
}
