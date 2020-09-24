package me.gimme.gimmetag.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

class SetConfig<T> extends AbstractConfig<Set<T>> {
    SetConfig(@NotNull AbstractConfig<ConfigurationSection> parent, @NotNull String path) {
        super(parent, path, null);
    }

    SetConfig(@NotNull String path) {
        super(path, null);
    }

    @NotNull
    @Override
    public Set<T> getValue() {
        return Objects.requireNonNull(getConfig().getList(path)).stream().map(e -> (T) e).collect(Collectors.toSet());
    }
}
