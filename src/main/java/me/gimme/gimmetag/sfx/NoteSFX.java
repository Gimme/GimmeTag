package me.gimme.gimmetag.sfx;

import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

class NoteSFX extends SFX {
    private Instrument instrument;
    private Note note;

    NoteSFX(@NotNull Instrument instrument, @NotNull Note note){
        this.instrument = instrument;
        this.note = note;
    }

    @Override
    public void play(@NotNull Player player) {
        player.playNote(getFrontOfPlayer(player), instrument, note);
    }
}
