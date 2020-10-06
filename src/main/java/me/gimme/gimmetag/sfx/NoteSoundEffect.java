package me.gimme.gimmetag.sfx;

import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Note;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

class NoteSoundEffect extends SoundEffect {
    private Instrument instrument;
    private Note note;

    NoteSoundEffect(@NotNull Instrument instrument, @NotNull Note note) {
        this.instrument = instrument;
        this.note = note;
    }

    @Override
    public void playLocal(@NotNull Player player, @NotNull Location location) {
        player.playNote(location, instrument, note);
    }

    @Override
    public void play(@NotNull Location location) {
        for (Player p : Objects.requireNonNull(location.getWorld()).getPlayers()) {
            p.playNote(location, instrument, note);
        }
    }
}
