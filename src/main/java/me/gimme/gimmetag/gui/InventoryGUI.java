package me.gimme.gimmetag.gui;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryGUI implements Listener {

    public final Button BACK_BUTTON = new Button("Go Back", Material.RED_WOOL, null, this::back);

    private final NavigationManager navigation = new NavigationManager();

    public InventoryGUI(@NotNull Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getPluginManager().registerEvents(navigation, plugin);
    }

    public void open(@NotNull GUIView guiView, @NotNull Player player) {
        navigation.open(guiView, player);
    }

    private void back(@NotNull Player player) {
        navigation.back(player);
    }

    public void close(@NotNull Player player) {
        navigation.close(player);
    }

    public void clearNavigationHistory(@NotNull Player player) {
        navigation.clearHistory(player);
    }

    @EventHandler
    private void onClick(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        GUIView clickedGuiView = GUIView.guiViews.get(event.getView().getTitle());
        if (clickedGuiView == null) return;

        event.setCancelled(true);

        Inventory clickedInventory = event.getClickedInventory();
        if (!clickedGuiView.getInventory().equals(clickedInventory)) return;

        ItemView clickedButton = clickedGuiView.getButton(event.getSlot());
        if (clickedButton == null) return;

        HumanEntity whoClicked = event.getWhoClicked();
        if (!(whoClicked instanceof Player)) return;

        clickedButton.click((Player) whoClicked);
    }


    private static class NavigationManager implements Listener {

        private final Map<UUID, GUIHistory> navigationHistoryByPlayer = new HashMap<>();

        private void open(@NotNull GUIView guiView, @NotNull Player player) {
            guiView.open(player);
            navigationHistoryByPlayer.put(player.getUniqueId(), new GUIHistory(guiView, navigationHistoryByPlayer.get(player.getUniqueId())));
        }

        private void back(@NotNull Player player) {
            GUIHistory history = navigationHistoryByPlayer.get(player.getUniqueId());
            if (history == null) {
                close(player);
                return;
            }

            GUIHistory previous = history.previous;

            if (previous == null) {
                close(player);
                return;
            }
            navigationHistoryByPlayer.put(player.getUniqueId(), previous);
            previous.currentView.open(player);
        }

        private void close(@NotNull Player player) {
            player.closeInventory();
            clearHistory(player);
        }

        private void clearHistory(@NotNull Player player) {
            navigationHistoryByPlayer.remove(player.getUniqueId());
        }

        /**
         * Clean up.
         */
        @EventHandler(priority = EventPriority.MONITOR)
        private void onPlayerQuit(PlayerQuitEvent event) {
            clearHistory(event.getPlayer());
        }

        private static class GUIHistory {
            private final @NotNull GUIView currentView;
            private final @Nullable GUIHistory previous;

            private GUIHistory(@NotNull GUIView currentView, @Nullable GUIHistory previous) {
                this.currentView = currentView;
                this.previous = previous;
            }
        }
    }
}
