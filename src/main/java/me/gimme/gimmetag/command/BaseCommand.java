package me.gimme.gimmetag.command;

import me.gimme.gimmetag.GimmeTag;
import org.jetbrains.annotations.NotNull;

public abstract class BaseCommand extends me.gimme.gimmecore.command.BaseCommand {
    protected BaseCommand(@NotNull String name) {
        super(GimmeTag.TAG_COMMAND, name);
        setPermission(GimmeTag.PERMISSIONS_PATH + "." + GimmeTag.TAG_COMMAND + "." + name);
    }
}
