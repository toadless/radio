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
public class ShuffleCommand extends Command
{
    public ShuffleCommand()
    {
        super("Shuffle", "Shuffles the queue.", "[none]");
        addAliases("shuffle");
        addFlags(CommandFlag.GUILD_ONLY);
    }

    @Override
    public void run(@NotNull List<String> args, @NotNull CommandEvent event, @NotNull Consumer<CommandException> failure)
    {
        MusicModule musicModule = event.getRadio().getModules().get(MusicModule.class);
        GuildMusicManager manager = musicModule.getGuildMusicManager(event.getGuild());

        if (CommandChecks.boundToChannel(manager, event.getChannel(), failure)) return;
        if (CommandChecks.sharesVoice(event, failure)) return;

        if (manager.getScheduler().hasNext())
        {
            manager.getScheduler().shuffle();
            event.replySuccess("Shuffled the queue.");
        }
        else
        {
            failure.accept(new CommandResultException("No more tracks queued."));
        }
    }
}