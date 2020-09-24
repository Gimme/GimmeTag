package me.gimme.gimmetag.command.commands;

import me.gimme.gimmetag.command.BaseCommand;
import me.gimme.gimmetag.tag.TagManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HunterCommand extends BaseCommand {

    private TagManager tagManager;

    public HunterCommand(@NotNull TagManager tagManager) {
        super("hunter");

        addAlias("h");
        setPlayerOnly(true);
        setDescription("Mark yourself as hunter for the next round");

        this.tagManager = tagManager;
    }

    @Override
    protected @Nullable String execute(@NotNull CommandSender sender, @NotNull String[] args) {
        tagManager.setDesiredHunter((Player) sender, true);
        return null;
    }
}
