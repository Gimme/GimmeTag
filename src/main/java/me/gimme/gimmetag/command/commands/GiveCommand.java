package me.gimme.gimmetag.command.commands;

import me.gimme.gimmetag.command.ArgPlaceholder;
import me.gimme.gimmetag.command.BaseCommand;
import me.gimme.gimmetag.config.Config;
import me.gimme.gimmetag.item.CustomItem;
import me.gimme.gimmetag.item.ItemManager;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class GiveCommand extends BaseCommand {

    private final ItemManager itemManager;
    private final Server server;

    public GiveCommand(@NotNull Server server, @NotNull ItemManager itemManager) {
        super("give");

        setArgsUsage("<item id> [amount=1] [player=self]");
        addArgsAlternative(ArgPlaceholder.ITEM_IDS + " 1 " + ArgPlaceholder.ONLINE_PLAYERS);
        addArgsAlternative(ArgPlaceholder.ITEM_IDS + " 64 " + ArgPlaceholder.ONLINE_PLAYERS);
        setMinArgs(1);
        setMaxArgs(3);
        setDescription("Give a custom item");

        this.itemManager = itemManager;
        this.server = server;
    }

    @Override
    protected @Nullable String execute(@NotNull CommandSender sender, @NotNull String[] args) {
        String itemId = args[0];
        int amount = args.length >= 2 ? requireInt(args[1]) : 1;

        Player player;

        if (args.length >= 3) {
            String playerName = args[2];
            player = server.getPlayer(playerName);
            if (player == null) return errorMessage("Could not find player: " + playerName);
        } else {
            if (!(sender instanceof Player)) return errorMessage(CommandError.PLAYER_ONLY, null);
            player = (Player) sender;
        }

        ItemStack itemStack = itemManager.createItemStack(itemId, amount);
        if (itemStack == null) return errorMessage("Could not find item: " + itemId);
        if (Config.SOULBOUND_ITEMS.getValue()) CustomItem.soulbind(itemStack, player);

        player.getInventory().addItem(itemStack);

        String itemDisplayName = Objects.requireNonNull(itemStack.getItemMeta()).getDisplayName();
        return successMessage("Gave " + amount + " " + ChatColor.stripColor(itemDisplayName) + " to " + player.getDisplayName());
    }
}
