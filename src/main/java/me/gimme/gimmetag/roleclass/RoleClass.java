package me.gimme.gimmetag.roleclass;

import me.gimme.gimmetag.tag.ArmorSlot;
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
    public static final String DEFAULT_CLASS_NAME = "-";

    @NotNull
    private final String name;
    @Nullable
    private final Material icon;
    @NotNull
    private final Map<String, Integer> items;
    @Nullable
    private Map<ArmorSlot, Color> colors;

    public RoleClass(@NotNull String name, @Nullable Material icon, @NotNull Map<String, Integer> items, @Nullable Map<ArmorSlot, Color> colors) {
        this.name = name;
        this.icon = icon;
        this.items = items;
        this.colors = colors;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Nullable
    public Material getIcon() {
        return icon;
    }

    @NotNull
    public Map<String, Integer> getItemMap() {
        return items;
    }

    public void setColor(@NotNull ArmorSlot armorSlot, @NotNull Color color) {
        if (colors == null) colors = new HashMap<>();
        colors.put(armorSlot, color);
    }

    @Nullable
    public Map<ArmorSlot, Color> getColors() {
        return colors;
    }


    private static final String NAME = "name";
    private static final String ICON = "icon";
    private static final String ITEMS = "items";
    private static final String COLORS = "colors";

    @Override
    @NotNull
    @Utility
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();

        map.put(NAME, name);
        if (icon != null) map.put(ICON, icon.name());
        map.put(ITEMS, items);
        map.put(COLORS, colors);

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

        Map<String, Integer> items = ((Map<String, Integer>) args.get(ITEMS));
        if (items == null) items = new HashMap<>();

        Map<String, Integer> hexColors = ((Map<String, Integer>) args.get(COLORS));
        Map<ArmorSlot, Color> colors = null;
        if (hexColors != null) {
            colors = hexColors.entrySet().stream().collect(Collectors.toMap(
                    entry -> ArmorSlot.valueOf(entry.getKey().toUpperCase()),
                    entry -> Color.fromRGB(entry.getValue())
            ));
        }

        return new RoleClass(name, icon, items, colors);
    }
}
