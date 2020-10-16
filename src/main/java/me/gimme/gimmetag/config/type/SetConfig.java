package me.gimme.gimmetag.config.type;

import me.gimme.gimmetag.config.AbstractConfig;
import me.gimme.gimmetag.config.IConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SetConfig<T> extends AbstractConfig<Set<T>> {
    public SetConfig(@NotNull IConfig<ConfigurationSection> parent, @NotNull String path) {
        super(parent, path);
    }

    @Override
    public @Nullable Set<T> getValue(@NotNull ConfigurationSection configurationSection) {
        return new HashSet<>((List<T>) configurationSection.getList(getPath()));
    }
}
