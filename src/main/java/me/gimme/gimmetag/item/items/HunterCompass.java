package me.gimme.gimmetag.item.items;

import me.gimme.gimmetag.GimmeTag;
import me.gimme.gimmetag.item.CustomItem;
import me.gimme.gimmetag.sfx.SoundEffect;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class HunterCompass extends CustomItem {

    private static boolean registeredEvents = false;

    private static final Material TYPE = Material.COMPASS;
    private static final String NAME = "hunter_compass";
    private static final NamespacedKey TAG_KEY = new NamespacedKey(GimmeTag.getPlugin(), NAME);
    private static final String ACTIVE_DISPLAY_NAME = ChatColor.DARK_RED + "Hunter Compass" + ChatColor.ITALIC + ChatColor.WHITE + " (active)";
    private static final List<String> ACTIVE_LORE = Collections.singletonList(ChatColor.RED + "Points to the closest runner");
    private static final String INACTIVE_DISPLAY_NAME = ChatColor.DARK_RED + "Hunter Compass" + ChatColor.ITALIC + ChatColor.GRAY + " (inactive)";
    private static final List<String> INACTIVE_LORE = Collections.singletonList("" + ChatColor.ITALIC + ChatColor.YELLOW + "(Right click to activate)");
    private static final List<String> NO_RUNNERS_LORE = Collections.singletonList("" + ChatColor.ITALIC + ChatColor.GRAY + "-No runners-");
    private static final String USE_RESPONESE_MESSAGE = ChatColor.YELLOW + "Now points to nearby runners";

    public HunterCompass() {
        this(GimmeTag.getPlugin());
    }

    private HunterCompass(@NotNull GimmeTag plugin) {
        super(NAME, TYPE);

        ItemMeta meta = Objects.requireNonNull(getItemMeta());
        meta.getPersistentDataContainer().set(TAG_KEY, PersistentDataType.STRING, UUID.randomUUID().toString());
        meta.addEnchant(Enchantment.LUCK, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        setItemMeta(meta);

        clearTargetingTask(this);

        if (!registeredEvents) {
            registeredEvents = true;
            plugin.getServer().getPluginManager().registerEvents(new OnUseListener(), plugin);
            plugin.getServer().getPluginManager().registerEvents(new OnPickupListener(), plugin);
        }
    }

    private static void setLore(@NotNull ItemStack item, boolean active, boolean existsRunners) {
        if (active && existsRunners) {
            ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
            meta.setDisplayName(ACTIVE_DISPLAY_NAME);
            meta.setLore(ACTIVE_LORE);
            item.setItemMeta(meta);
        } else if (active) {
            ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
            meta.setDisplayName(ACTIVE_DISPLAY_NAME);
            meta.setLore(NO_RUNNERS_LORE);
            item.setItemMeta(meta);
        } else {
            ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
            meta.setDisplayName(INACTIVE_DISPLAY_NAME);
            meta.setLore(INACTIVE_LORE);
            item.setItemMeta(meta);
        }
    }

    private static Set<UUID> activeHunterCompasses = new HashSet<>();
    private static void setActive(@NotNull ItemStack item, boolean active) {
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
        UUID uuid = UUID.fromString(dataContainer.get(TAG_KEY, PersistentDataType.STRING));

        if (active) activeHunterCompasses.add(uuid);
        else activeHunterCompasses.remove(uuid);

        item.setItemMeta(meta);
    }

    private static boolean isActive(@NotNull ItemStack item) {
        PersistentDataContainer dataContainer = Objects.requireNonNull(item.getItemMeta()).getPersistentDataContainer();
        String idString = dataContainer.get(TAG_KEY, PersistentDataType.STRING);
        return idString != null && activeHunterCompasses.contains(UUID.fromString(idString));
    }

    public static void alwaysTargetClosestPlayer(@NotNull ItemStack item, @NotNull Player user) {
        clearTargetingTask(item);
        setActive(item, true);

        new ItemOngoingUseTaskTimer(GimmeTag.getPlugin(), user, item, 5) {
            Entity closestTarget = null;

            @Override
            public void onRecalculation() {
                closestTarget = plugin.getTagManager().getClosestRunner(user);
            }

            @Override
            public void onTick() {
                boolean foundTarget = closestTarget != null;
                setLore(item, true, foundTarget);
                setTarget(item, foundTarget ? closestTarget.getLocation() : null);
            }
        }.start();
    }

    private static void setTarget(@NotNull ItemStack item, @Nullable Location location) {
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        if (!(meta instanceof CompassMeta)) return; // Can happen if the item has been dropped on the ground. CraftMetaItem instead of CraftMetaCompass

        CompassMeta compassMeta = (CompassMeta) meta;
        if (location == null) compassMeta.setLodestone(new Location(Bukkit.getWorlds().get(0), 0, 0, 0)); // Location should be null but for some reason it does not work.
        else compassMeta.setLodestone(location);
        item.setItemMeta(compassMeta);
    }

    private static void clearTargetingTask(@NotNull ItemStack item) {
        setActive(item, false);
        setTarget(item, null);
        setLore(item, false, false);
    }

    private static void onUse(@NotNull ItemStack item, @NotNull Player user) {
        if (isActive(item)) return; // One use

        alwaysTargetClosestPlayer(item, user);
        user.sendMessage(USE_RESPONESE_MESSAGE);
        SoundEffect.ACTIVATE.play(user);
    }

    private static boolean isHunterCompass(@NotNull ItemStack item) {
        if (!item.getType().equals(TYPE)) return false;
        return Objects.requireNonNull(item.getItemMeta()).getPersistentDataContainer().has(TAG_KEY, PersistentDataType.STRING);
    }


    private static abstract class ItemOngoingUseTaskTimer extends BukkitRunnable {
        protected GimmeTag plugin;
        protected Player user;
        protected ItemStack item;
        private int ticksPerRecalculation;

        private int ticksUntilRecalculation = 0;

        private ItemOngoingUseTaskTimer(@NotNull GimmeTag plugin, @NotNull Player user, @NotNull ItemStack item,
                                        int ticksPerRecalculation) {
            this.plugin = plugin;
            this.user = user;
            this.item = item;
            this.ticksPerRecalculation = ticksPerRecalculation;
        }

        @Override
        public void run() {
            if (--ticksUntilRecalculation <= 0) {
                ticksUntilRecalculation = ticksPerRecalculation;

                if (!user.isOnline() || !user.getInventory().contains(item)) {
                    clearTargetingTask(item);
                    cancel();
                    return;
                }

                onRecalculation();
            }

            onTick();
        }

        public abstract void onRecalculation();

        public abstract void onTick();

        @NotNull
        public ItemOngoingUseTaskTimer start() {
            return start(1);
        }

        @NotNull
        public ItemOngoingUseTaskTimer start(long period) {
            runTaskTimer(plugin, 0, period);
            return this;
        }
    }


    private static class OnUseListener implements Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        private void onInteract(PlayerInteractEvent event) {
            ItemStack item = event.getItem();
            if (item == null) return;
            if (!item.getType().equals(TYPE)) return;
            if (!(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))) return;

            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && clickedBlock.getType().isInteractable()) return;

            if (!isHunterCompass(item)) return;

            HunterCompass.onUse(item, event.getPlayer());
        }
    }


    private static class OnPickupListener implements Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        private void onEntityPickup(EntityPickupItemEvent event) {
            onPickup(event.getItem().getItemStack());
        }
        @EventHandler(priority = EventPriority.MONITOR)
        private void onInventoryPickup(InventoryPickupItemEvent event) {
            onPickup(event.getItem().getItemStack());
        }

        private void onPickup(@NotNull ItemStack item) {
            if (!isHunterCompass(item)) return;
            clearTargetingTask(item);
        }
    }
}
