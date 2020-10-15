package me.gimme.gimmetag.config.type;

import me.gimme.gimmetag.config.AbstractConfig;
import me.gimme.gimmetag.config.IConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class ListConfig<T> extends AbstractConfig<List<T>> {
    public ListConfig(@NotNull IConfig<ConfigurationSection> parent, @NotNull String path) {
        super(parent, path);
    }

    @NotNull
    @Override
    public List<T> getValue() {
        return (List<T>) Objects.requireNonNull(getConfigurationSection().getList(getPath()));
    }
}
