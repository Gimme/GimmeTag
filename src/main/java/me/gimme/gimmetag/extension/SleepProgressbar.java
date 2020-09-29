package me.gimme.gimmetag.extension;

import me.gimme.gimmecore.chat.Chat;
import me.gimme.gimmetag.events.HunterSleepEvent;
import me.gimme.gimmetag.tag.Role;
import me.gimme.gimmetag.tag.TagManager;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class SleepProgressbar implements Listener {
    private final Plugin plugin;
    private final TagManager tagManager;

    public SleepProgressbar(@NotNull Plugin plugin, @NotNull TagManager tagManager) {
        this.plugin = plugin;
        this.tagManager = tagManager;
    }

    /**
     * Displays progress bars to runners when a hunter is sleeping.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onHunterSleep(HunterSleepEvent event) {
        Chat.sendProgressBar(plugin, () -> !tagManager.isActiveRound(), tagManager.getOnlineRunners(),
                event.getSleepSeconds() * 20,
                Role.HUNTER.playerDisplayName(event.getPlayer()) + ChatColor.RESET + " sleeping...",
                BarColor.RED);
    }
}
