package me.gimme.gimmetag.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

class ValueConfig<T> extends AbstractConfig<T> {
    ValueConfig(@NotNull AbstractConfig<ConfigurationSection> parent, @NotNull String path) {
        super(parent, path);
    }

    ValueConfig(@NotNull ConfigurationSection configurationSection, @NotNull String path) {
        super(configurationSection, path);
    }

    @NotNull
    @Override
    public T getValue() {
        return (T) Objects.requireNonNull(getConfigurationSection().getObject(getPath(), Object.class));
    }
}
