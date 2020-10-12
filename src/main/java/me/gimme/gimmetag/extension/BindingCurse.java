package me.gimme.gimmetag.extension;

import me.gimme.gimmetag.GimmeTag;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BindingCurse implements Listener {

    private static final NamespacedKey BINDING_CURSE_KEY = new NamespacedKey(GimmeTag.getPlugin(), "binding_curse");
    private static final PersistentDataType<Integer, Integer> BINDING_CURSE_DATA_TYPE = PersistentDataType.INTEGER;

    /**
     * Sets if the given item stack should have a curse of binding effect (without the enchantment glow). Curse of
     * binding means the item cannot be taken off after being equipped in an armor slot.
     *
     * @param itemStack the item stack to get the binding curse
     * @param cursed    if the curse should be applied or removed
     */
    public static void setBindingCurse(@NotNull ItemStack itemStack, boolean cursed) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) itemMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
        if (itemMeta == null) return;

        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        if (cursed) dataContainer.set(BINDING_CURSE_KEY, BINDING_CURSE_DATA_TYPE, 0);
        else dataContainer.remove(BINDING_CURSE_KEY);

        itemStack.setItemMeta(itemMeta);
    }

    private static boolean hasBindingCurse(@Nullable ItemStack itemStack) {
        if (itemStack == null) return false;
        if (itemStack.getItemMeta() == null) return false;
        return itemStack.getItemMeta().getPersistentDataContainer().has(BINDING_CURSE_KEY, BINDING_CURSE_DATA_TYPE);
    }

    /**
     * Prevents cursed items from being removed from armor slots.
     */
    @EventHandler
    private void onArmorClick(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        if (event.getSlotType() != InventoryType.SlotType.ARMOR) return;
        if (!hasBindingCurse(event.getCurrentItem())) return;

        event.setResult(Event.Result.DENY);
    }
}
