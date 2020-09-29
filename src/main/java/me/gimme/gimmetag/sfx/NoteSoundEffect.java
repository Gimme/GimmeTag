package me.gimme.gimmetag.sfx;

import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

class NoteSoundEffect extends SoundEffect {
    private Instrument instrument;
    private Note note;

    NoteSoundEffect(@NotNull Instrument instrument, @NotNull Note note){
        this.instrument = instrument;
        this.note = note;
    }

    @Override
    public void play(@NotNull Player player) {
        player.playNote(getFrontOfPlayer(player), instrument, note);
    }
}
