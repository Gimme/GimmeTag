package me.gimme.gimmetag.item.items;

import me.gimme.gimmetag.GimmeTag;
import me.gimme.gimmetag.item.AbilityItem;
import me.gimme.gimmetag.sfx.SoundEffect;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.*;

public class HunterRadar extends AbilityItem {

    private static DecimalFormat df = new DecimalFormat("0.00");
    private static final Material TYPE = Material.CLOCK;
    private static final List<String> LORE = Collections.singletonList("" + ChatColor.ITALIC + ChatColor.YELLOW + "(Right click to activate)");

    private GimmeTag plugin;
    private Set<UUID> activeItems = new HashSet<>();

    public HunterRadar(@NotNull GimmeTag plugin) {
        super(
                "Hunter Radar",
                TYPE,
                true,
                0.5,
                false,
                null
        );

        this.plugin = plugin;
    }

    @Override
    protected void onCreate(@NotNull ItemStack itemStack, @NotNull ItemMeta itemMeta) {
        mute();
        hideCooldown();
        itemMeta.getPersistentDataContainer().set(getIdKey(), PersistentDataType.STRING, UUID.randomUUID().toString());
        itemMeta.setLore(LORE);
    }

    @Override
    protected boolean onUse(@NotNull ItemStack itemStack, @NotNull Player user) {
        ItemMeta itemMeta = Objects.requireNonNull(itemStack.getItemMeta());
        String uuidString = itemMeta.getPersistentDataContainer().get(getIdKey(), PersistentDataType.STRING);
        UUID uuid = UUID.fromString(Objects.requireNonNull(uuidString));

        if (activeItems.contains(uuid)) return false;
        activeItems.add(uuid);
        SoundEffect.ACTIVATE.play(user);

        new HunterCompass.ItemOngoingUseTaskTimer(plugin, user, itemStack, 10, null) {
            Entity closestTarget = null;

            @Override
            public void onRecalculation() {
                closestTarget = plugin.getTagManager().getClosestRunner(user);
            }

            @Override
            public void onTick() {
                if (closestTarget == null || !isItemInHand(user, item)) {
                    hideActionBar(user);
                    return;
                }

                double distance = closestTarget.getLocation().distance(user.getLocation());
                sendActionBar(user, ChatColor.YELLOW + formatMeters(distance));
            }

            @Override
            public void onFinish() {
                hideActionBar(user);
                activeItems.remove(uuid);
            }
        }.start();

        return true;
    }

    private NamespacedKey getIdKey() {
        return new NamespacedKey(plugin, getId());
    }

    private static boolean isItemInHand(@NotNull Player player, ItemStack itemStack) {
        return itemStack.equals(player.getInventory().getItemInMainHand())  || itemStack.equals(player.getInventory().getItemInOffHand());
    }

    private static void sendActionBar(@NotNull Player player, @NotNull String text) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(text));
    }

    private static void hideActionBar(@NotNull Player player) {
        sendActionBar(player, "");
    }

    private static String formatMeters(double blocks) {
        return df.format(blocks) + " m";
    }
}
