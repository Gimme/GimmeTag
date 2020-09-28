package me.gimme.gimmetag.command.commands;

import me.gimme.gimmetag.command.BaseCommand;
import me.gimme.gimmetag.config.Config;
import me.gimme.gimmetag.tag.TagManager;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StartCommand extends BaseCommand {

    private TagManager tagManager;

    public StartCommand(@NotNull TagManager tagManager) {
        super("start");

        addAlias("s");
        setArgsUsage("[levels to end] [sleep duration] [hunters=1]");
        addArgsAlternative("100 30 1");
        setMinArgs(0);
        setMaxArgs(3);
        setDescription("Starts a round of tag with randomly selected hunters");

        this.tagManager = tagManager;
    }

    @Override
    protected @Nullable String execute(@NotNull CommandSender sender, @NotNull String[] args) {
        int levelsToEnd = args.length >= 1 ? requireInt(args[0]) : Config.SCORING_LEVELS_TO_END.getValue();
        int sleepSeconds = args.length >= 2 ? requireInt(args[1]) : Config.TAG_SLEEP_TIME.getValue();
        int numberOfHunters = args.length >= 3 ? requireInt(args[2]) : 1;

        if (!tagManager.start(levelsToEnd, sleepSeconds, numberOfHunters)) return errorMessage(
                "Could not start round. Already an ongoing round or too few players.");

        return null;
    }
}
