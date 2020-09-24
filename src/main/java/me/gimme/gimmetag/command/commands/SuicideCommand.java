package me.gimme.gimmetag.command.commands;

import me.gimme.gimmetag.command.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SuicideCommand extends BaseCommand {

    public SuicideCommand() {
        super("suicide");

        addAlias("kill");
        addAlias("die");
        addAlias("kms");
        setPlayerOnly(true);
        setDescription("Commit suicide");
    }

    @Override
    protected @Nullable String execute(@NotNull CommandSender sender, @NotNull String[] args) {
        Player player = (Player) sender;
        player.setHealth(0);
        return null;
    }
}
