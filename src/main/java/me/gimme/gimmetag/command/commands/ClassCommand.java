package me.gimme.gimmetag.command.commands;

import me.gimme.gimmetag.command.BaseCommand;
import me.gimme.gimmetag.roleclass.ClassSelectionManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClassCommand extends BaseCommand {

    private final ClassSelectionManager classSelectionManager;

    public ClassCommand(@NotNull ClassSelectionManager classSelectionManager) {
        super("class");

        addAlias("c");
        setPlayerOnly(true);
        setDescription("Open the GUI to choose a class");

        this.classSelectionManager = classSelectionManager;
    }

    @Override
    protected @Nullable String execute(@NotNull CommandSender sender, @NotNull String[] args) {
        Player player = (Player) sender;

        classSelectionManager.openClassSelectionMenu(player);

        return null;
    }
}
