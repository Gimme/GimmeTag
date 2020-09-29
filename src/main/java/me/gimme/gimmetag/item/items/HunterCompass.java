package me.gimme.gimmetag.item.items;

import me.gimme.gimmetag.GimmeTag;
import me.gimme.gimmetag.item.AbilityItem;
import me.gimme.gimmetag.sfx.SFX;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class HunterCompass extends AbilityItem {

    private static final Material TYPE = Material.COMPASS;

    private static final String ACTIVE_DISPLAY_NAME = ChatColor.DARK_RED + "Hunter Compass" + ChatColor.ITALIC + ChatColor.WHITE + " (active)";
    private static final String INACTIVE_DISPLAY_NAME = ChatColor.DARK_RED + "Hunter Compass" + ChatColor.ITALIC + ChatColor.GRAY + " (inactive)";

    private static final List<String> ACTIVE_LORE = Collections.singletonList(ChatColor.RED + "Points to the closest runner");
    private static final List<String> INACTIVE_LORE = Collections.singletonList("" + ChatColor.ITALIC + ChatColor.YELLOW + "(Right click to activate)");
    private static final List<String> NO_RUNNERS_LORE = Collections.singletonList("" + ChatColor.ITALIC + ChatColor.GRAY + "-No runners-");

    private Plugin plugin;

    private Set<UUID> activeHunterCompasses = new HashSet<>();

    public HunterCompass(@NotNull String id, @NotNull GimmeTag plugin) {
        super(
                id,
                INACTIVE_DISPLAY_NAME,
                TYPE,
                true,
                0.5,
                false,
                "Activated hunter compass: points to closest runner"
        );

        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(new OnPickupListener(), plugin);
    }

    private NamespacedKey getIdKey() {
        return new NamespacedKey(plugin, getId());
    }

    @Override
    protected void onCreate(@NotNull ItemStack itemStack, @NotNull ItemMeta itemMeta) {
        mute();
        hideCooldown();
        itemMeta.getPersistentDataContainer().set(getIdKey(), PersistentDataType.STRING, UUID.randomUUID().toString());
        itemStack.setItemMeta(itemMeta);
        clearTargetingTask(itemStack);
    }

    @Override
    protected boolean onUse(@NotNull ItemStack itemStack, @NotNull Player user) {
        if (isActive(itemStack)) return false; // One use

        alwaysTargetClosestPlayer(itemStack, user);
        SFX.ACTIVATE.play(user);
        return true;
    }

    private static void setLore(@NotNull ItemStack item, boolean active, boolean existsRunners) {
        ItemMeta itemMeta = Objects.requireNonNull(item.getItemMeta());
        setLore(itemMeta, active, existsRunners);
        item.setItemMeta(itemMeta);
    }

    private static void setLore(@NotNull ItemMeta itemMeta, boolean active, boolean existsRunners) {
        if (active && existsRunners) {
            itemMeta.setDisplayName(ACTIVE_DISPLAY_NAME);
            itemMeta.setLore(ACTIVE_LORE);
        } else if (active) {
            itemMeta.setDisplayName(ACTIVE_DISPLAY_NAME);
            itemMeta.setLore(NO_RUNNERS_LORE);
        } else {
            itemMeta.setDisplayName(INACTIVE_DISPLAY_NAME);
            itemMeta.setLore(INACTIVE_LORE);
        }
    }

    private void setActive(@NotNull ItemStack item, boolean active) {
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
        String uuidString = dataContainer.get(getIdKey(), PersistentDataType.STRING);
        UUID uuid = UUID.fromString(Objects.requireNonNull(uuidString));

        if (active) activeHunterCompasses.add(uuid);
        else activeHunterCompasses.remove(uuid);
    }

    private boolean isActive(@NotNull ItemStack item) {
        PersistentDataContainer dataContainer = Objects.requireNonNull(item.getItemMeta()).getPersistentDataContainer();
        String idString = dataContainer.get(getIdKey(), PersistentDataType.STRING);
        return idString != null && activeHunterCompasses.contains(UUID.fromString(idString));
    }

    public void alwaysTargetClosestPlayer(@NotNull ItemStack item, @NotNull Player user) {
        clearTargetingTask(item);
        setActive(item, true);

        new ItemOngoingUseTaskTimer(GimmeTag.getPlugin(), user, item, 5, null) {
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

            @Override
            public void onFinish() {
                clearTargetingTask(item);
            }
        }.start();
    }

    private void clearTargetingTask(@NotNull ItemStack item) {
        setActive(item, false);
        setTarget(item, null);
        setLore(item, false, false);
    }

    private boolean isHunterCompass(@NotNull ItemStack item) {
        if (!item.getType().equals(TYPE)) return false;
        return Objects.requireNonNull(item.getItemMeta()).getPersistentDataContainer().has(getIdKey(), PersistentDataType.STRING);
    }

    private static void setTarget(@NotNull ItemStack item, @Nullable Location location) {
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        if (!(meta instanceof CompassMeta))
            return; // Can happen if the item has been dropped on the ground. CraftMetaItem instead of CraftMetaCompass

        CompassMeta compassMeta = (CompassMeta) meta;
        if (location == null)
            compassMeta.setLodestone(new Location(Bukkit.getWorlds().get(0), 0, 0, 0)); // Location should be null but for some reason it does not work.
        else compassMeta.setLodestone(location);
        item.setItemMeta(compassMeta);
    }


    abstract static class ItemOngoingUseTaskTimer extends BukkitRunnable {
        protected GimmeTag plugin;
        protected Player user;
        protected ItemStack item;
        private int ticksPerRecalculation;
        @Nullable
        private StopCondition stopCondition;

        private int ticksUntilRecalculation = 0;

        ItemOngoingUseTaskTimer(@NotNull GimmeTag plugin, @NotNull Player user, @NotNull ItemStack item,
                                int ticksPerRecalculation, @Nullable StopCondition stopCondition) {
            this.plugin = plugin;
            this.user = user;
            this.item = item;
            this.ticksPerRecalculation = ticksPerRecalculation;
            this.stopCondition = stopCondition;
        }

        @Override
        public void run() {
            if (--ticksUntilRecalculation <= 0) {
                ticksUntilRecalculation = ticksPerRecalculation;

                if (!user.isOnline() || item.getType().equals(Material.AIR) || (stopCondition != null && stopCondition.shouldStop())) {
                    cancel();
                    return;
                }

                onRecalculation();
            }

            onTick();
        }

        public abstract void onRecalculation();

        public abstract void onTick();

        public abstract void onFinish();

        @Override
        public synchronized void cancel() throws IllegalStateException {
            super.cancel();
            onFinish();
        }

        @NotNull
        public ItemOngoingUseTaskTimer start() {
            return start(1);
        }

        @NotNull
        public ItemOngoingUseTaskTimer start(long period) {
            runTaskTimer(plugin, 0, period);
            return this;
        }

        interface StopCondition {
            boolean shouldStop();
        }
    }


    private class OnPickupListener implements Listener {
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
