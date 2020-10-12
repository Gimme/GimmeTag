package me.gimme.gimmetag.command.commands;

import me.gimme.gimmetag.command.BaseCommand;
import me.gimme.gimmetag.tag.TagManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RunnerCommand extends BaseCommand {

    private final TagManager tagManager;

    public RunnerCommand(@NotNull TagManager tagManager) {
        super("runner");

        setPlayerOnly(true);
        setDescription("Mark yourself as runner for the next round");

        this.tagManager = tagManager;
    }

    @Override
    protected @Nullable String execute(@NotNull CommandSender sender, @NotNull String[] args) {
        tagManager.setDesiredHunter((Player) sender, false);
        return null;
    }
}
