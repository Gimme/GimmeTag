package me.gimme.gimmetag.item.items;

import me.gimme.gimmetag.GimmeTag;
import me.gimme.gimmetag.config.Config;
import me.gimme.gimmetag.item.CustomItem;
import me.gimme.gimmetag.sfx.SoundEffect;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SpeedBoost extends CustomItem {

    private static boolean registeredEvents = false;

    private static final String NAME = "speed_boost";
    private static final NamespacedKey TAG_KEY = new NamespacedKey(GimmeTag.getPlugin(), NAME);
    private static final Material MATERIAL = Material.SUGAR;
    private static final String DISPLAY_NAME = "Speed Boost";
    private static final String USE_RESPONESE_MESSAGE = ChatColor.YELLOW + "Activated speed boost";

    public SpeedBoost() {
        this(1);
    }

    public SpeedBoost(int amount) {
        super(NAME, MATERIAL, amount);

        long seconds = Math.round(Config.SPEED_BOOST_DURATION.getValue().doubleValue());

        ItemMeta meta = Objects.requireNonNull(getItemMeta());
        meta.setDisplayName(DISPLAY_NAME + " " + Config.SPEED_BOOST_LEVEL.getValue() + " "
                + ChatColor.GRAY + ChatColor.ITALIC + "(" + seconds + "s)");
        meta.getPersistentDataContainer().set(TAG_KEY, PersistentDataType.STRING, "");
        meta.addEnchant(Enchantment.SOUL_SPEED, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        setItemMeta(meta);

        if (!registeredEvents) {
            registeredEvents = true;
            Plugin plugin = GimmeTag.getPlugin();
            plugin.getServer().getPluginManager().registerEvents(new SpeedBoost.OnUseListener(), plugin);
        }
    }


    private static boolean isSpeedBoostItem(@NotNull ItemStack item) {
        if (!item.getType().equals(MATERIAL)) return false;
        return Objects.requireNonNull(item.getItemMeta()).getPersistentDataContainer().has(TAG_KEY, PersistentDataType.STRING);
    }

    private static void onUse(@NotNull ItemStack item, @NotNull Player user) {
        int duration = Math.round(Config.SPEED_BOOST_DURATION.getValue().floatValue() * 20);
        int amplifier = Config.SPEED_BOOST_LEVEL.getValue() - 1;

        PotionEffect currentSpeedEffect = user.getPotionEffect(PotionEffectType.SPEED);
        int currentEffectDuration = 0;

        if (currentSpeedEffect != null) {
            currentEffectDuration = currentSpeedEffect.getDuration();
            int currentEffectAmplifier = currentSpeedEffect.getAmplifier();
            if (currentEffectAmplifier > amplifier) return; // Don't allow overriding higher-speed boosts
            if (currentEffectAmplifier < amplifier) currentEffectDuration = 0; // Overlap lower-speed boosts
            // Extend same-speed boosts
        }

        user.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration + currentEffectDuration, amplifier));
        item.setAmount(item.getAmount() - 1);
        user.sendMessage(USE_RESPONESE_MESSAGE);
        SoundEffect.USE_EFFECT.play(user);
    }


    private static class OnUseListener implements Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        private void onInteract(PlayerInteractEvent event) {
            ItemStack item = event.getItem();
            if (item == null) return;
            if (!item.getType().equals(MATERIAL)) return;
            if (!(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)))
                return;

            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && clickedBlock.getType().isInteractable()) return;

            if (!isSpeedBoostItem(item)) return;

            SpeedBoost.onUse(item, event.getPlayer());
        }
    }
}
