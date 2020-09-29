package me.gimme.gimmetag.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public class TagEndEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    @Nullable
    private Player winner;
    private Map<UUID, Integer> scores;

    public TagEndEvent(@Nullable Player winner, @Nullable Map<UUID, Integer> scores) {
        this.winner = winner;
        this.scores = scores;
    }

    @Nullable
    public Player getWinner() {
        return winner;
    }

    public Map<UUID, Integer> getScores() {
        return scores;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
