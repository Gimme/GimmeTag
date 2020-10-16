package me.gimme.gimmetag.config.type;

import me.gimme.gimmetag.config.AbstractConfig;
import me.gimme.gimmetag.config.IConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ValueConfig<T> extends AbstractConfig<T> {
    public ValueConfig(@NotNull IConfig<ConfigurationSection> parent, @NotNull String path) {
        super(parent, path);
    }

    @Nullable
    public T getValue(@NotNull ConfigurationSection configurationSection) {
        return (T) configurationSection.getObject(getPath(), Object.class);
    }
}
