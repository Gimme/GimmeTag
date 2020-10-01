package me.gimme.gimmetag.sfx;

import org.bukkit.Bukkit;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

class NoteSFX extends SFX {
    private Instrument instrument;
    private Note note;
    private boolean local;

    NoteSFX(@NotNull Instrument instrument, @NotNull Note note) {
        this(instrument, note, false);
    }

    NoteSFX(@NotNull Instrument instrument, @NotNull Note note, boolean local) {
        this.instrument = instrument;
        this.note = note;
        this.local = local;
    }

    @Override
    public void play(@NotNull Player player) {
        if (local) {
            player.playNote(getFrontOfPlayer(player), instrument, note);
        } else {
            for (Player p : player.getWorld().getPlayers()) {
                p.playNote(getFrontOfPlayer(player), instrument, note);
            }
        }
    }
}
