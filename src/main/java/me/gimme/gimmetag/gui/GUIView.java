package me.gimme.gimmetag.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class GUIView {
    static final Map<String, GUIView> guiViews = new HashMap<>();

    private final Inventory inventory;
    private final Map<Integer, ItemView> buttonBySlot;

    GUIView(@NotNull String title, @NotNull Inventory inventory, @NotNull Map<Integer, ItemView> buttonBySlot) {
        guiViews.put(title, this);

        this.inventory = inventory;
        this.buttonBySlot = buttonBySlot;
    }

    void open(@NotNull Player player) {
        player.openInventory(inventory);
    }

    @Nullable ItemView getButton(int slot) {
        return buttonBySlot.get(slot);
    }

    public Inventory getInventory() {
        return inventory;
    }
}
