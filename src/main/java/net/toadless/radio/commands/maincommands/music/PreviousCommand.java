package net.toadless.radio.commands.maincommands.music;

import java.util.List;
import java.util.function.Consumer;

import net.toadless.radio.modules.MusicModule;
import net.toadless.radio.objects.command.Command;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.command.CommandFlag;
import net.toadless.radio.objects.exception.CommandException;
import net.toadless.radio.objects.exception.CommandResultException;
import net.toadless.radio.objects.music.GuildMusicManager;
import net.toadless.radio.util.CommandChecks;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings ("unused")
public class PreviousCommand extends Command
{
    public PreviousCommand()
    {
        super("Previous", "Skips to the previous song.", "[none]");
        addAliases("previous", "rewind", "last");
        addFlags(CommandFlag.GUILD_ONLY);
    }

    @Override
    public void run(@NotNull List<String> args, @NotNull CommandEvent event, @NotNull Consumer<CommandException> failure)
    {
        MusicModule musicModule = event.getRadio().getModules().get(MusicModule.class);
        GuildMusicManager manager = musicModule.getGuildMusicManager(event.getGuild());

        if (CommandChecks.boundToChannel(manager, event.getChannel(), failure)) return;
        if (CommandChecks.sharesVoice(event, failure)) return;
        if (CommandChecks.isUserDj(event, failure)) return;

        if (manager.getScheduler().hasPrevious())
        {
            boolean previous = manager.getScheduler().playPrevious();

            if (previous) event.replySuccess("Skipped to the previous track.");
            else failure.accept(new CommandResultException("An error occurred whilst skipping to the previous track."));
        }
        else
        {
            failure.accept(new CommandResultException("No previous tracks."));
        }
    }
}