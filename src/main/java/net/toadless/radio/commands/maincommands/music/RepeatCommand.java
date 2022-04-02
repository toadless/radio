package net.toadless.radio.commands.maincommands.music;

import net.toadless.radio.commands.subcommands.repeat.RepeatOffCommand;
import net.toadless.radio.commands.subcommands.repeat.RepeatQueueCommand;
import net.toadless.radio.commands.subcommands.repeat.RepeatSongCommand;
import net.toadless.radio.modules.MusicModule;
import net.toadless.radio.objects.command.Command;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.command.CommandFlag;
import net.toadless.radio.objects.exception.CommandException;
import net.toadless.radio.objects.exception.CommandInputException;
import net.toadless.radio.objects.music.GuildMusicManager;
import net.toadless.radio.util.CommandChecks;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class RepeatCommand extends Command
{

    public RepeatCommand()
    {
        super("Repeat", "Gets and sets the repeat mode of the current song.", "[none]");
        addAliases("repeat", "loop");
        addFlags(CommandFlag.GUILD_ONLY);
        addChildren(
                new RepeatOffCommand(this),
                new RepeatQueueCommand(this),
                new RepeatSongCommand(this)
        );
    }

    @Override
    public void run(@NotNull List<String> args, @NotNull CommandEvent event, @NotNull Consumer<CommandException> failure)
    {
        MusicModule musicModule = event.getRadio().getModules().get(MusicModule.class);
        GuildMusicManager manager = musicModule.getGuildMusicManager(event.getGuild());

        if (CommandChecks.boundToChannel(manager, event.getChannel(), failure)) return;
        if (CommandChecks.sharesVoice(event, failure)) return;
        if (CommandChecks.isUserDj(event, failure)) return;

        event.replySuccess("The current repeat mode is " + manager.getScheduler().getRepeatMode().toString().toLowerCase());
    }
}