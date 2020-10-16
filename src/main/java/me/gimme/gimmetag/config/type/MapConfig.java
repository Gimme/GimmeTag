package me.gimme.gimmetag.config.type;

import me.gimme.gimmetag.config.AbstractConfig;
import me.gimme.gimmetag.config.IConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.stream.Collectors;

class MapConfig<T> extends AbstractConfig<Map<String, T>> {
    public MapConfig(@NotNull IConfig<ConfigurationSection> parent, @NotNull String path) {
        super(parent, path);
    }

    @Override
    public @Nullable Map<String, T> getValue(@NotNull ConfigurationSection configurationSection) {
        ConfigurationSection section = configurationSection.getConfigurationSection(getPath());
        if (section == null) return null;

        return section.getValues(false).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (T) e.getValue()));
    }
}
