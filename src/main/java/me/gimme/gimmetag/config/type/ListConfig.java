package me.gimme.gimmetag.config.type;

import me.gimme.gimmetag.config.AbstractConfig;
import me.gimme.gimmetag.config.IConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ListConfig<T> extends AbstractConfig<List<T>> {
    public ListConfig(@NotNull IConfig<ConfigurationSection> parent, @NotNull String path) {
        super(parent, path);
    }

    @Override
    public @Nullable List<T> getValue(@NotNull ConfigurationSection configurationSection) {
        return (List<T>) configurationSection.getList(getPath());
    }
}
