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
    private static final List<String> LORE = Collections.singletonList("" + ChatColor.ITALIC + ChatColor.YELLOW + "(Right click to activate)");

    private TagManager tagManager;

    public HunterRadar(double cooldown, boolean consumable, double duration, int level, @NotNull TagManager tagManager) {
        super(
                "Hunter Radar",
                TYPE,
                consumable,
                level <= 1 ? 0 : duration
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
                if (isInfinite() && !isItemInHand(user, itemStack)) {
                    Chat.hideActionBar(user);
                } else if (closestTarget == null) {
                    Chat.sendActionBar(user, ChatColor.YELLOW + "---");
                } else {
                    double distance = closestTarget.getLocation().distance(user.getLocation());
                    Chat.sendActionBar(user, formatMeters(distance));
                }
            }

            @Override
            public void onFinish() {
                Chat.hideActionBar(user);
            }
        };
    }

    private static String formatMeters(double blocks) {
        return ChatColor.YELLOW + df.format(blocks) + " m";
    }
}
