package net.toadless.radio.commands.maincommands.music;

import net.toadless.radio.objects.command.Command;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.command.CommandFlag;
import net.toadless.radio.objects.exception.CommandException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class LyricsCommand extends Command
{
    public LyricsCommand()
    {
        super("Lyrics", "Fetches the lyrics for the provided song.", "[Song Name]");
        addAliases("lyrics");
        addFlags(CommandFlag.DISABLED);
    }

    @Override
    public void run(@NotNull List<String> args, @NotNull CommandEvent event, @NotNull Consumer<CommandException> failure)
    {
        event.replyError("Unimplemented");
    }
}