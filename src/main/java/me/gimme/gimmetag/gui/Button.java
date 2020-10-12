package me.gimme.gimmetag.gui;

import me.gimme.gimmetag.sfx.SoundEffect;
import me.gimme.gimmetag.sfx.SoundEffects;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class Button extends ItemView {
    @Nullable
    private final Consumer<@NotNull Player> onClick;
    @Nullable
    private final SoundEffect soundEffect;

    public Button(@NotNull String title, @NotNull Material icon, @Nullable List<String> description, @Nullable Consumer<@NotNull Player> onClick) {
        this(title, icon, description, onClick, SoundEffects.CLICK);
    }

    public Button(@NotNull String title, @NotNull Material icon, @Nullable List<String> description, @Nullable Consumer<@NotNull Player> onClick, @Nullable SoundEffect soundEffect) {
        super(title, icon, description);
        this.onClick = onClick;
        this.soundEffect = soundEffect;
    }

    @Override
    void click(@NotNull Player clicker) {
        if (onClick == null) return;
        if (soundEffect != null) soundEffect.playLocal(clicker);
        onClick.accept(clicker);
    }
}
