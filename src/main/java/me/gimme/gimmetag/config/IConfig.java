package me.gimme.gimmetag.config;

import org.jetbrains.annotations.NotNull;

public interface IConfig<T> {
    @NotNull
    T getValue();
}
