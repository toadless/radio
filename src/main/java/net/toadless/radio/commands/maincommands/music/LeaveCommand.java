package net.toadless.radio.commands.maincommands.music;

import net.toadless.radio.modules.MusicModule;
import net.toadless.radio.objects.command.Command;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.command.CommandFlag;
import net.toadless.radio.objects.exception.CommandException;
import net.toadless.radio.objects.music.GuildMusicManager;
import net.toadless.radio.util.CommandChecks;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings ("unused")
public class LeaveCommand extends Command
{
    public LeaveCommand()
    {
        super("Leave", "Makes the bot leave your VC.", "[none]");
        addAliases("leave", "disconnect");
        addFlags(CommandFlag.GUILD_ONLY);
    }

    @Override
    public void run(@NotNull List<String> args, @NotNull CommandEvent event, @NotNull Consumer<CommandException> failure)
    {
        MusicModule musicModule = event.getRadio().getModules().get(MusicModule.class);
        GuildMusicManager manager = musicModule.getGuildMusicManager(event.getGuild());

        if (CommandChecks.sharesVoice(event, failure)) return;
        if (CommandChecks.boundToChannel(manager, event.getChannel(), failure)) return;
        if (CommandChecks.inVoice(event, failure)) return;
        if (CommandChecks.isUserDj(event, failure)) return;

        manager.getPlayer().destroy();
        manager.leave(event.getGuild());
        manager.getScheduler().clear();

        event.replySuccess("Bye!");
    }
}