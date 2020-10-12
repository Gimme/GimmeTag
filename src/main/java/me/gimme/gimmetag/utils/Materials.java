package me.gimme.gimmetag.utils;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Materials {

    /**
     * Returns if the specified material can be interacted with. Materials that are normally not interactable but are
     * still considered so by {@link Material#isInteractable()} are excluded here.
     * <p>
     * This is a incomplete utility function used to predict if a right click on the material will cause an interaction
     * with the block. Not all materials are guaranteed to be covered.
     *
     * @param material the material to check
     * @return if the material can be interacted with
     */
    public static boolean isInteractable(@NotNull Material material) {
        return material.isInteractable() && !UNINTERACTABLE.contains(material);
    }

    private static final Set<Material> OTHER = new HashSet<>(Arrays.asList(
            Material.IRON_DOOR,
            Material.IRON_TRAPDOOR,
            Material.CAMPFIRE,
            Material.SOUL_CAMPFIRE
    ));

    private static final Set<Material> FENCES = new HashSet<>(Arrays.asList(
            Material.ACACIA_FENCE,
            Material.BIRCH_FENCE,
            Material.CRIMSON_FENCE,
            Material.DARK_OAK_FENCE,
            Material.JUNGLE_FENCE,
            Material.NETHER_BRICK_FENCE,
            Material.OAK_FENCE,
            Material.SPRUCE_FENCE,
            Material.WARPED_FENCE
    ));

    private static final Set<Material> STAIRS = new HashSet<>(Arrays.asList(
            Material.ACACIA_STAIRS,
            Material.ANDESITE_STAIRS,
            Material.BIRCH_STAIRS,
            Material.BLACKSTONE_STAIRS,
            Material.BRICK_STAIRS,
            Material.COBBLESTONE_STAIRS,
            Material.CRIMSON_STAIRS,
            Material.DARK_OAK_STAIRS,
            Material.DARK_PRISMARINE_STAIRS,
            Material.DIORITE_STAIRS,
            Material.END_STONE_BRICK_STAIRS,
            Material.GRANITE_STAIRS,
            Material.JUNGLE_STAIRS,
            Material.MOSSY_COBBLESTONE_STAIRS,
            Material.MOSSY_STONE_BRICK_STAIRS,
            Material.NETHER_BRICK_STAIRS,
            Material.OAK_STAIRS,
            Material.POLISHED_ANDESITE_STAIRS,
            Material.POLISHED_BLACKSTONE_BRICK_STAIRS,
            Material.POLISHED_BLACKSTONE_STAIRS,
            Material.POLISHED_DIORITE_STAIRS,
            Material.POLISHED_GRANITE_STAIRS,
            Material.PRISMARINE_BRICK_STAIRS,
            Material.PRISMARINE_STAIRS,
            Material.PURPUR_STAIRS,
            Material.QUARTZ_STAIRS,
            Material.RED_NETHER_BRICK_STAIRS,
            Material.RED_SANDSTONE_STAIRS,
            Material.SANDSTONE_STAIRS,
            Material.SMOOTH_QUARTZ_STAIRS,
            Material.SMOOTH_RED_SANDSTONE_STAIRS,
            Material.SMOOTH_SANDSTONE_STAIRS,
            Material.SPRUCE_STAIRS,
            Material.STONE_BRICK_STAIRS,
            Material.STONE_STAIRS,
            Material.WARPED_STAIRS
    ));

    private static final Set<Material> UNINTERACTABLE = Stream.of(
            OTHER,
            FENCES,
            STAIRS
    ).flatMap(Set::stream).collect(Collectors.toSet());
}
