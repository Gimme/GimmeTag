package me.gimme.gimmetag.item.items;

import me.gimme.gimmecore.chat.Chat;
import me.gimme.gimmetag.item.ContinuousAbilityItem;
import me.gimme.gimmetag.tag.TagManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.*;

public class HunterRadar extends ContinuousAbilityItem {

    private static DecimalFormat df = new DecimalFormat("0.00");
    private static final Material TYPE = Material.CLOCK;
    private static final List<String> LORE = Arrays.asList(
            "" + ChatColor.ITALIC + ChatColor.YELLOW + "(Right click to activate)",
            "Shows distance to nearest runner");

    private TagManager tagManager;

    public HunterRadar(double cooldown, boolean consumable, double duration, @NotNull TagManager tagManager) {
        super(
                "Hunter Radar",
                TYPE,
                consumable,
                duration
        );

        setCooldown(cooldown);

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
                if (closestTarget == null) {
                    Chat.sendActionBar(user, ChatColor.YELLOW + "---");
                } else {
                    double distance = closestTarget.getLocation().distance(user.getLocation());
                    Chat.sendActionBar(user, formatMeters(distance));
                }
            }

            @Override
            public void onFinish() {
                if (getDurationTicks() != 0) Chat.hideActionBar(user);
            }
        };
    }

    private static String formatMeters(double blocks) {
        return ChatColor.YELLOW + df.format(blocks) + " m";
    }
}
