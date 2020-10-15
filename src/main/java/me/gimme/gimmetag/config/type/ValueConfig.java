package me.gimme.gimmetag.config.type;

import me.gimme.gimmetag.config.AbstractConfig;
import me.gimme.gimmetag.config.IConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ValueConfig<T> extends AbstractConfig<T> {
    public ValueConfig(@NotNull IConfig<ConfigurationSection> parent, @NotNull String path) {
        super(parent, path);
    }

    @NotNull
    @Override
    public T getValue() {
        return (T) Objects.requireNonNull(getConfigurationSection().getObject(getPath(), Object.class));
    }
}
