package me.gimme.gimmetag.item.items;

import me.gimme.gimmetag.config.type.AbilityItemConfig;
import me.gimme.gimmetag.item.ContinuousAbilityItem;
import me.gimme.gimmetag.tag.TagManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class HunterCompass extends ContinuousAbilityItem {

    private static final String NAME = ChatColor.DARK_RED + "Hunter Compass";
    private static final Material MATERIAL = Material.COMPASS;
    private static final String INFO = "Points to nearest runner";

    private final TagManager tagManager;
    private final boolean followTarget;

    public HunterCompass(@NotNull String id, @NotNull AbilityItemConfig config, @NotNull TagManager tagManager) {
        super(id, NAME, MATERIAL, config);

        this.tagManager = tagManager;
        this.followTarget = getDurationTicks() != 0;

        if (!followTarget) {
            setDuration(getCooldown());
            setGlowWhenActive(true);
        }

        setInfo(INFO);
    }

    @Override
    protected void onCreate(@NotNull ItemStack itemStack, @NotNull ItemMeta itemMeta) {
    }

    @Override
    protected @NotNull ContinuousUse createContinuousUse(@NotNull ItemStack itemStack, @NotNull Player user) {
        setTarget(itemStack, null);

        if (followTarget) return new ContinuousUse() {
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
            }
        };

        Entity closestTarget = tagManager.getClosestRunner(user);
        Location closestTargetLocation = closestTarget != null ? closestTarget.getLocation() : null;
        return new ContinuousUse() {
            @Override
            public void onCalculate() {
            }

            @Override
            public void onTick() {
                setTarget(itemStack, closestTargetLocation);
            }

            @Override
            public void onFinish() {
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
