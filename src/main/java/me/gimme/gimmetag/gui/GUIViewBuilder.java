package me.gimme.gimmetag.gui;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GUIViewBuilder {
    private String title = "";
    private List<ItemView> buttons = new ArrayList<>();
    private ItemView[] itemViews = new ItemView[0];

    public GUIViewBuilder setTitle(@NotNull String title) {
        this.title = title;
        return this;
    }

    public GUIViewBuilder addButtons(@NotNull Button... buttons) {
        this.buttons.addAll(Arrays.asList(buttons));
        return this;
    }

    public GUIViewBuilder addButton(@NotNull ItemView button) {
        buttons.add(button);
        return this;
    }

    public GUIViewBuilder setItemViews(@NotNull ItemView... itemViews) {
        this.itemViews = itemViews;
        return this;
    }

    public GUIViewBuilder setItemViews(@NotNull ItemStack... itemStacks) {
        this.itemViews = Arrays.stream(itemStacks).map(ItemView::new).toArray(ItemView[]::new);
        return this;
    }

    public GUIView build() {
        int rows = 0;
        int buttonRows = (8 + buttons.size()) / 9;
        int displayRows = (8 + itemViews.length) / 9;
        int buttonRowsStartIndex = 0;
        int displayRowsStartIndex = 0;

        if (buttonRows > 0 && displayRows > 0) {
            rows++;
            buttonRowsStartIndex += 9 * 2;
        }

        rows += (buttonRows + displayRows);
        if (rows == 0) rows = 1;

        Inventory inventory = Bukkit.createInventory(null, rows * 9, title);
        Map<Integer, ItemView> buttonBySlot = new HashMap<>();
        for (int i = 0; i < buttons.size(); i++) {
            int slot = buttonRowsStartIndex + i;
            inventory.setItem(slot, buttons.get(i).getItem());
            buttonBySlot.put(slot, buttons.get(i));
        }
        buttonBySlot.forEach(((slot, button) -> inventory.setItem(slot, button.getItem())));
        for (int i = 0; i < itemViews.length; i++) {
            inventory.setItem(displayRowsStartIndex + i, itemViews[i].getItem());
        }

        return new GUIView(title, inventory, buttonBySlot);
    }
}
