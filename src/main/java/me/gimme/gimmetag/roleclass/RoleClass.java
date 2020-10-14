package me.gimme.gimmetag.roleclass;

import me.gimme.gimmetag.tag.ArmorSlot;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Utility;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@SerializableAs("RoleClass")
public class RoleClass implements ConfigurationSerializable {
    private static final String DEFAULT_CLASS_NAME = "-";

    @NotNull
    private final String name;
    @NotNull
    private final String displayName;
    @Nullable
    private Material icon;
    @Nullable
    private Color color;
    @Nullable
    private Map<ArmorSlot, Color> colors;
    @NotNull
    private final Map<String, Integer> items;

    public RoleClass(@NotNull String name, @Nullable Material icon, @Nullable Color color, @Nullable Map<ArmorSlot, Color> colors, @NotNull Map<String, Integer> items) {
        this.displayName = name;
        this.name = ChatColor.stripColor(name).toLowerCase();
        this.icon = icon;
        this.color = color;
        this.colors = colors;
        this.items = items;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    public void setIcon(@Nullable Material icon) {
        this.icon = icon;
    }

    @Nullable
    public Material getIcon() {
        return icon;
    }

    public void setColor(@Nullable Color color) {
        this.color = color;
    }

    @Nullable
    public Color getColor() {
        return color;
    }

    @Nullable
    public Color getColor(@NotNull ArmorSlot armorSlot) {
        return colors != null ? colors.getOrDefault(armorSlot, color) : color;
    }

    public void setColor(@NotNull ArmorSlot armorSlot, @NotNull Color color) {
        if (colors == null) colors = new HashMap<>();
        colors.put(armorSlot, color);
    }

    @Nullable
    public Map<ArmorSlot, Color> getColors() {
        return colors;
    }

    @NotNull
    public Map<String, Integer> getItemMap() {
        return items;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;

        final RoleClass other = (RoleClass) obj;
        return !getName().equals(other.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }


    private static final String NAME = "name";
    private static final String ICON = "icon";
    private static final String COLOR = "color";
    private static final String COLORS = "colors";
    private static final String ITEMS = "items";

    @Override
    @NotNull
    @Utility
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();

        map.put(NAME, name);
        if (icon != null) map.put(ICON, icon.name());
        map.put(COLOR, color);
        map.put(COLORS, colors);
        map.put(ITEMS, items);

        return map;
    }

    /**
     * Required method for configuration serialization.
     *
     * @param args map to deserialize
     * @return the deserialized role class
     * @see ConfigurationSerializable
     */
    public static RoleClass deserialize(@NotNull Map<String, Object> args) {
        String name = (String) args.get(NAME);
        if (name == null) name = DEFAULT_CLASS_NAME;

        String iconName = (String) args.get(ICON);
        Material icon = iconName != null ? Material.getMaterial(iconName.toUpperCase()) : null;

        Integer hexColor = (Integer) args.get(COLOR);
        Color color = hexColor != null ? Color.fromRGB(hexColor) : null;

        Map<String, Integer> hexColors = ((Map<String, Integer>) args.get(COLORS));
        Map<ArmorSlot, Color> colors = null;
        if (hexColors != null) {
            colors = hexColors.entrySet().stream().collect(Collectors.toMap(
                    entry -> ArmorSlot.valueOf(entry.getKey().toUpperCase()),
                    entry -> Color.fromRGB(entry.getValue())
            ));
        }

        Map<String, Integer> items = ((Map<String, Integer>) args.get(ITEMS));
        if (items == null) items = new HashMap<>();

        return new RoleClass(name, icon, color, colors, items);
    }
}
