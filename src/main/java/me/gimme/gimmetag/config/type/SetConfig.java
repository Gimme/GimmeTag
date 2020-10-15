package me.gimme.gimmetag.config.type;

import me.gimme.gimmetag.config.AbstractConfig;
import me.gimme.gimmetag.config.IConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SetConfig<T> extends AbstractConfig<Set<T>> {
    public SetConfig(@NotNull IConfig<ConfigurationSection> parent, @NotNull String path) {
        super(parent, path);
    }

    @NotNull
    @Override
    public Set<T> getValue() {
        return new HashSet<>((List<T>) Objects.requireNonNull(getConfigurationSection().getList(getPath())));
    }
}
