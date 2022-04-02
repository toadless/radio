package net.toadless.radio.commands.subcommands.repeat;

import net.toadless.radio.modules.MusicModule;
import net.toadless.radio.objects.command.Command;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.command.CommandFlag;
import net.toadless.radio.objects.exception.CommandException;
import net.toadless.radio.objects.music.GuildMusicManager;
import net.toadless.radio.objects.music.RepeatMode;
import net.toadless.radio.util.CommandChecks;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class RepeatQueueCommand extends Command
{

    public RepeatQueueCommand(Command parent)
    {
        super(parent, "Queue", "Sets the repeat mode to queue.", "[none]");
        addFlags(CommandFlag.GUILD_ONLY);
        addAliases("queue");
    }

    @Override
    public void run(@NotNull List<String> args, @NotNull CommandEvent event, @NotNull Consumer<CommandException> failure)
    {
        MusicModule musicModule = event.getRadio().getModules().get(MusicModule.class);
        GuildMusicManager manager = musicModule.getGuildMusicManager(event.getGuild());

        if (CommandChecks.boundToChannel(manager, event.getChannel(), failure)) return;
        if (CommandChecks.sharesVoice(event, failure)) return;
        if (CommandChecks.isUserDj(event, failure)) return;

        manager.getScheduler().setRepeatMode(RepeatMode.QUEUE);
        event.replySuccess("Set the repeat mode to queue.");
    }
}