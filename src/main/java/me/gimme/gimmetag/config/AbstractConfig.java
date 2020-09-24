package me.gimme.gimmetag.config;

import me.gimme.gimmetag.GimmeTag;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractConfig<T> {

    private static List<AbstractConfig<?>> values = new ArrayList<>();
    private Plugin plugin = GimmeTag.getPlugin();

    protected String path;
    protected Class<? extends T> cls;

    AbstractConfig(@NotNull AbstractConfig<ConfigurationSection> parent, @NotNull String path, Class<? extends T> cls) {
        this(parent.path + "." + path, cls);
    }

    AbstractConfig(@NotNull String path, Class<? extends T> cls) {
        this.path = path;
        this.cls = cls;

        values.add(this);
    }

    @NotNull
    public abstract T getValue();

    public static AbstractConfig<?>[] values() {
        return values.toArray(new AbstractConfig<?>[0]);
    }

    protected FileConfiguration getConfig() {
        return plugin.getConfig();
    }
}
