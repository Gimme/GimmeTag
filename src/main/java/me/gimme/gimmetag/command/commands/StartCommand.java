package me.gimme.gimmetag.command.commands;

import me.gimme.gimmetag.command.BaseCommand;
import me.gimme.gimmetag.tag.TagManager;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StartCommand extends BaseCommand {

    private TagManager tagManager;

    public StartCommand(@NotNull TagManager tagManager) {
        super("start");

        addAlias("s");
        setArgsUsage("<sleep> [hunters=1]");
        addArgsAlternative("60 1");
        setMinArgs(1);
        setMaxArgs(2);
        setDescription("Starts a round of tag with randomly selected hunters");

        this.tagManager = tagManager;
    }

    @Override
    protected @Nullable String execute(@NotNull CommandSender sender, @NotNull String[] args) {
        int sleepSeconds = requireInt(args[0]);
        int numberOfHunters = args.length >= 2 ? requireInt(args[1]) : 1;

        if (!tagManager.start(sleepSeconds, numberOfHunters)) return errorMessage(
                "Could not start round. There is already an ongoing round or too few players.");

        return null;
    }
}
