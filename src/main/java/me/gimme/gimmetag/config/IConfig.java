package me.gimme.gimmetag.config;

import org.jetbrains.annotations.Nullable;

public interface IConfig<T> {
    @Nullable T getValue();
}
