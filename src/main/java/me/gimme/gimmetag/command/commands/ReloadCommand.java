package me.gimme.gimmetag.command.commands;

import me.gimme.gimmecore.command.CommandUsageException;
import me.gimme.gimmetag.GimmeTag;
import me.gimme.gimmetag.command.BaseCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReloadCommand extends BaseCommand {

    private final GimmeTag plugin;

    public ReloadCommand(@NotNull GimmeTag plugin) {
        super("reload");
        setDescription("Safely reload this plugin");

        this.plugin = plugin;
    }

    @Override
    protected @Nullable String execute(@NotNull CommandSender commandSender, @NotNull String[] strings) throws CommandUsageException {
        plugin.reload();

        return null;
    }
}
