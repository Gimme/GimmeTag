package me.gimme.gimmetag.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

class ListConfig<T> extends AbstractConfig<List<T>> {
    ListConfig(@NotNull AbstractConfig<ConfigurationSection> parent, @NotNull String path) {
        super(parent, path);
    }

    ListConfig(@NotNull ConfigurationSection configurationSection, @NotNull String path) {
        super(configurationSection, path);
    }

    @NotNull
    @Override
    public List<T> getValue() {
        return (List<T>) Objects.requireNonNull(getConfigurationSection().getList(getPath()));
    }
}
