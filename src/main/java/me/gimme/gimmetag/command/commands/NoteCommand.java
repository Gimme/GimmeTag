package me.gimme.gimmetag.command.commands;

import me.gimme.gimmecore.command.CommandUsageException;
import me.gimme.gimmetag.command.BaseCommand;
import me.gimme.gimmetag.sfx.SoundEffect;
import org.bukkit.Bukkit;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class NoteCommand extends BaseCommand {

    private Random random = new Random();

    public NoteCommand() {
        super("note");

        setMinArgs(2);
        setMaxArgs(3);
        setPlayerOnly(true);
    }

    @Override
    protected @Nullable String execute(@NotNull CommandSender sender, @NotNull String[] args) throws CommandUsageException {
        Player player = (Player) sender;

        Instrument[] instruments = Instrument.values();
        Instrument instrument = args.length == 3 ? Instrument.valueOf(args[2]) : instruments[random.nextInt(instruments.length)];

        double r = Math.random();
        if (r < 0.9) player.playNote(player.getLocation().add(player.getLocation().getDirection()), instrument, Note.natural(requireInt(args[0]), Note.Tone.valueOf(args[1])));
        else SoundEffect.TELEPORT.play(player);
        Bukkit.getLogger().info(r + " Sound: " + instrument.name());
        return null;
    }
}
