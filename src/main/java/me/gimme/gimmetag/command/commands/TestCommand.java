package me.gimme.gimmetag.command.commands;

import me.gimme.gimmetag.GimmeTag;
import me.gimme.gimmetag.command.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TestCommand extends BaseCommand {

    public TestCommand() {
        super("test");

        addAlias("t");
        setPlayerOnly(true);
        setDescription("Dev test command");
        setPermission(GimmeTag.DEV_PERMISSIONS_PATH);
    }

    @Override
    protected @Nullable String execute(@NotNull CommandSender sender, @NotNull String[] args) {
        Player player = (Player) sender;


        return null;
    }
}
