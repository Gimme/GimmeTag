package me.gimme.gimmetag.item;

import me.gimme.gimmetag.GimmeTag;
import me.gimme.gimmetag.utils.Ticks;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents a custom item that can be used to create item stacks to be used in game.
 */
public abstract class CustomItem {

    private static final NamespacedKey ID_KEY = new NamespacedKey(GimmeTag.getPlugin(), "CustomItemId");
    private static final NamespacedKey UNIQUE_ID_KEY = new NamespacedKey(GimmeTag.getPlugin(), "UniqueId");
    private static final PersistentDataType<String, String> ID_DATA_TYPE = PersistentDataType.STRING;
    private static final PersistentDataType<String, String> UNIQUE_ID_DATA_TYPE = PersistentDataType.STRING;

    private final String id;
    private final String displayName;
    private final Material type;
    private List<String> info = new ArrayList<>();
    private boolean glowing = true;

    /**
     * Creates a new custom item with the specified name and item type.
     *
     * @param name a unique name and the display name of this item. The name can contain ChatColors and spaces, which
     *             will be stripped from the name and only be used for the display name.
     * @param type the item type of the generated item stacks
     */
    public CustomItem(@NotNull String name, @NotNull Material type) {
        this(name, name, type);
    }

    /**
     * Creates a new custom item with the specified name, display name and item type.
     *
     * @param id          a unique name for this item
     * @param displayName the display name of this item
     * @param type        the item type of the generated item stacks
     */
    public CustomItem(@NotNull String id, @NotNull String displayName, @NotNull Material type) {
        this.id = ChatColor.stripColor(id).toLowerCase().replaceAll(" ", "_");
        this.displayName = displayName;
        this.type = type;
    }

    /**
     * Creates an item stack from this custom item to be used in game.
     */
    @NotNull
    public ItemStack createItemStack() {
        return createItemStack(1);
    }

    /**
     * Creates an ItemStack from this custom item, with the specified stack size, to be used in game.
     *
     * @param amount the stack size
     * @return the created item stack
     */
    @NotNull
    public ItemStack createItemStack(int amount) {
        ItemStack itemStack = new ItemStack(type, amount);

        updateLore(itemStack);

        ItemMeta itemMeta = Objects.requireNonNull(itemStack.getItemMeta());

        itemMeta.setDisplayName(displayName);

        setGlowing(itemMeta, glowing);

        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        dataContainer.set(ID_KEY, ID_DATA_TYPE, id);
        dataContainer.set(UNIQUE_ID_KEY, UNIQUE_ID_DATA_TYPE, UUID.randomUUID().toString());

        onCreate(itemStack, itemMeta);
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    /**
     * Does something with the newly created item stack.
     * <p>
     * This can be implemented to do extra changes on the item stack before finishing the creation of it.
     * <p>
     * {@link ItemStack#setItemMeta(ItemMeta)} will be called automatically after this method returns and does not need
     * to be explicitly called.
     *
     * @param itemStack the newly created item stack
     * @param itemMeta  the item meta of the newly created item stack
     */
    protected abstract void onCreate(@NotNull ItemStack itemStack, @NotNull ItemMeta itemMeta);

    /**
     * Updates the lore of the given item stack with the specified header, the item info and the specified footer.
     * <p>
     * The lore is formatted with the specified header first, followed by the item info and lastly the footer.
     *
     * @param itemStack the item stack to modify the lore of
     * @param header    the header to be placed at the top of the lore
     * @param footer    the footer to be placed at the bottom of the lore
     */
    protected void updateLore(@NotNull ItemStack itemStack, @NotNull List<String> header, @NotNull List<String> footer) {
        ItemMeta itemMeta = Objects.requireNonNull(itemStack.getItemMeta());

        List<String> lore = new ArrayList<>();
        lore.addAll(header);
        lore.addAll(info);
        lore.addAll(footer);

        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
    }

    /**
     * Updates the lore of the given item stack with the item's info.
     *
     * @param itemStack the item stack to modify the lore of
     */
    protected void updateLore(@NotNull ItemStack itemStack) {
        updateLore(itemStack, new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Sets this item's info explaining what it does.
     *
     * @param info the info text with each element in the list representing a new line
     */
    protected void setInfo(@NotNull List<String> info) {
        this.info = info;
    }

    /**
     * Sets this item's info explaining what it does.
     *
     * @param info the info text with each element representing a new line
     */
    protected void setInfo(@NotNull String... info) {
        setInfo(Arrays.asList(info));
    }

    /**
     * Returns this item's info explaining what it does.
     * <p>
     * The info gets displayed in the item's lore.
     *
     * @return this item's info
     */
    @NotNull
    protected List<String> getInfo() {
        return info;
    }

    /**
     * Stops the glow (enchant) effect from being applied to the created item stacks.
     * <p>
     * The glow effect is enabled by default.
     */
    protected void disableGlow() {
        setGlowing(false);
    }

    /**
     * Sets if the created item stacks should glow like enchanted items.
     * <p>
     * The glow effect is enabled by default.
     */
    protected void setGlowing(boolean glowing) {
        this.glowing = glowing;
    }

    /**
     * Sets the given item meta to be glowing (as if the item was enchanted) or not.
     *
     * @param itemMeta the item meta to modify
     * @param glowing  if the item meta should be glowing
     */
    protected static void setGlowing(@NotNull ItemMeta itemMeta, boolean glowing) {
        if (glowing) {
            itemMeta.addEnchant(Enchantment.LUCK, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            itemMeta.removeEnchant(Enchantment.LUCK);
            itemMeta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
    }

    /**
     * Returns the unique name of this custom item.
     *
     * @return the unique name of this custom item
     */
    @NotNull
    public String getId() {
        return id;
    }

    /**
     * Returns the given item stack's unique identifier, or null if it was not created from a custom item.
     * <p>
     * This identifier is unique for each item stack even if created from the same custom item.
     *
     * @param itemStack the item stack to get the unique identifier of
     * @return the given item stack's unique identifier, or null if it was not created from a custom item
     */
    @Nullable
    static UUID getUniqueId(@NotNull ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return null;

        String uuidString = itemMeta.getPersistentDataContainer().get(UNIQUE_ID_KEY, UNIQUE_ID_DATA_TYPE);
        if (uuidString == null) return null;

        return UUID.fromString(uuidString);
    }

    /**
     * Returns the unique identifier of the custom item that the given item stack was created from, or null if not from
     * a custom item.
     * <p>
     * Note that this results in the same identifier for every item stack that was created from the same custom item.
     *
     * @param itemStack the item stack to get the custom item identifier of
     * @return the unique identifier of the custom item that the given item stack was created from, else null
     */
    @Nullable
    static String getCustomItemId(@NotNull ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return null;

        return itemMeta.getPersistentDataContainer().get(ID_KEY, ID_DATA_TYPE);
    }

    /**
     * Converts the specified ticks into seconds and returns it in a string formatted according to the standard way to
     * display time.
     *
     * @param ticks the time in ticks to be converted into seconds
     * @return a formatted string showing the specified time
     */
    static String formatSeconds(int ticks) {
        return Ticks.ticksToSecondsString(ticks) + "s";
    }
}
