package me.gimme.gimmetag.command.commands;

import me.gimme.gimmetag.command.BaseCommand;
import me.gimme.gimmetag.tag.TagManager;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StopCommand extends BaseCommand {

    private TagManager tagManager;

    public StopCommand(@NotNull TagManager tagManager) {
        super("stop");

        addAlias("end");
        addAlias("e");
        setDescription("Stops the ongoing round of tag");

        this.tagManager = tagManager;
    }

    @Override
    protected @Nullable String execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!tagManager.stop()) return errorMessage("There is no ongoing round");
        return null;
    }
}
