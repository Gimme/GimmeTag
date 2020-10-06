package me.gimme.gimmetag.item.items;

import me.gimme.gimmecore.chat.Chat;
import me.gimme.gimmetag.config.AbilityItemConfig;
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

public class HunterRadar extends ContinuousAbilityItem {

    private static final String NAME = ChatColor.DARK_RED + "Hunter Radar";
    private static final Material MATERIAL = Material.CLOCK;
    private static final String INFO = "Shows distance to nearest runner";

    private final TagManager tagManager;

    public HunterRadar(@NotNull AbilityItemConfig config, @NotNull TagManager tagManager) {
        super(NAME, MATERIAL, config);

        this.tagManager = tagManager;

        setInfo(INFO);
    }

    @Override
    protected void onCreate(@NotNull ItemStack itemStack, @NotNull ItemMeta itemMeta) {
    }

    @Override
    protected @NotNull ContinuousUse createContinuousUse(@NotNull ItemStack itemStack, @NotNull Player user) {
        return new ContinuousUse() {
            private Entity closestTarget;

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
                if (hasDuration()) Chat.hideActionBar(user);
            }
        };
    }

    private static final DecimalFormat DF = new DecimalFormat("0.00");

    private static String formatMeters(double blocks) {
        return ChatColor.YELLOW + DF.format(blocks) + " m";
    }
}
