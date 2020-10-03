package me.gimme.gimmetag.item;

import me.gimme.gimmetag.GimmeTag;
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

import java.text.DecimalFormat;
import java.util.Objects;
import java.util.UUID;

public abstract class CustomItem {

    private static final NamespacedKey ID_KEY = new NamespacedKey(GimmeTag.getPlugin(), "CustomItemId");
    private static final NamespacedKey UNIQUE_ID_KEY = new NamespacedKey(GimmeTag.getPlugin(), "UniqueId");
    private static final PersistentDataType<String, String> ID_DATA_TYPE = PersistentDataType.STRING;
    private static final PersistentDataType<String, String> UNIQUE_ID_DATA_TYPE = PersistentDataType.STRING;

    private final String id;
    private final String displayName;
    private final Material type;
    private boolean glowing = true;

    public CustomItem(@NotNull String name, @NotNull Material type) {
        this(
                ChatColor.stripColor(name).toLowerCase().replaceAll(" ", "_"),
                name,
                type);
    }

    public CustomItem(@NotNull String id, @NotNull String displayName, @NotNull Material type) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
    }

    @NotNull
    public ItemStack createItemStack() {
        return createItemStack(1);
    }

    @NotNull
    public ItemStack createItemStack(int amount) {
        ItemStack itemStack = new ItemStack(type, amount);

        ItemMeta itemMeta = itemStack.getItemMeta();
        Objects.requireNonNull(itemMeta);

        itemMeta.setDisplayName(displayName);

        if (glowing) {
            itemMeta.addEnchant(Enchantment.LUCK, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        dataContainer.set(ID_KEY, ID_DATA_TYPE, id);
        dataContainer.set(UNIQUE_ID_KEY, UNIQUE_ID_DATA_TYPE, UUID.randomUUID().toString());

        onCreate(itemStack, itemMeta);
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    protected abstract void onCreate(@NotNull ItemStack itemStack, @NotNull ItemMeta itemMeta);

    @NotNull
    public String getId() {
        return id;
    }

    @Nullable
    public static UUID getUniqueId(@NotNull ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return null;

        String uuidString = itemMeta.getPersistentDataContainer().get(UNIQUE_ID_KEY, UNIQUE_ID_DATA_TYPE);
        if (uuidString == null) return null;

        return UUID.fromString(uuidString);
    }

    /**
     * @param itemStack the ItemStack to get the custom item ID of
     * @return the custom item ID of the specified ItemStack if it is a custom item, else null
     */
    @Nullable
    public static String getCustomItemId(@NotNull ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return null;

        return itemMeta.getPersistentDataContainer().get(ID_KEY, ID_DATA_TYPE);
    }

    /**
     * @param itemStack the ItemStack to check if a custom item
     * @return if the specified ItemStack is a custom item
     */
    public static boolean isCustomItem(@NotNull ItemStack itemStack) {
        return getCustomItemId(itemStack) != null;
    }

    protected void setGlowing(boolean glowing) {
        this.glowing = glowing;
    }


    private static final DecimalFormat DF = new DecimalFormat("#.##");
    protected static String formatSeconds(int ticks) {
        return DF.format(ticks / 20d) + "s";
    }
}
