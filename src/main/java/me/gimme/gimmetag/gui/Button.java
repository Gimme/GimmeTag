package me.gimme.gimmetag.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class Button extends ItemView {
    private final @Nullable Consumer<@NotNull Player> onClick;

    public Button(@NotNull String title, @NotNull Material icon, @Nullable List<String> description, @Nullable Consumer<@NotNull Player> onClick) {
        super(title, icon, description);
        this.onClick = onClick;
    }

    @Override
    void click(@NotNull Player clicker) {
        if (onClick != null) onClick.accept(clicker);
    }
}
