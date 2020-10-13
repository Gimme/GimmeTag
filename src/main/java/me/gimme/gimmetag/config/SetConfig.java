package me.gimme.gimmetag.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

class SetConfig<T> extends AbstractConfig<Set<T>> {
    SetConfig(@NotNull AbstractConfig<ConfigurationSection> parent, @NotNull String path) {
        super(parent, path, null);
    }

    SetConfig(@NotNull ConfigurationSection configurationSection, @NotNull String path) {
        super(configurationSection, path, null);
    }

    @NotNull
    @Override
    public Set<T> getValue() {
        return new HashSet<>((List<T>) Objects.requireNonNull(getConfigurationSection().getList(getPath())));
    }
}
