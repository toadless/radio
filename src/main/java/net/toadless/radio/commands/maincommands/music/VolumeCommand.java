package net.toadless.radio.commands.maincommands.music;

import java.util.List;
import java.util.OptionalInt;
import java.util.function.Consumer;
import net.toadless.radio.modules.MusicModule;
import net.toadless.radio.objects.command.Command;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.command.CommandFlag;
import net.toadless.radio.objects.exception.CommandException;
import net.toadless.radio.objects.exception.CommandInputException;
import net.toadless.radio.objects.music.GuildMusicManager;
import net.toadless.radio.util.CommandChecks;
import net.toadless.radio.util.Parser;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings ("unused")
public class VolumeCommand extends Command
{
    public VolumeCommand()
    {
        super("Volume", "Sets the music volume", "<volume {100}>");
        addAliases("volume", "vol");
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

        if (args.isEmpty())
        {
            event.replySuccess("The volume is " + manager.getPlayer().getVolume() + "%");
            return;
        }

        OptionalInt volume = new Parser(args.get(0), event).parseAsUnsignedInt();

        if (volume.isPresent())
        {
            if (volume.getAsInt() > 100)
            {
                failure.accept(new CommandInputException("Volume must be 100 or lower."));
                return;
            }

            manager.setVolume(volume.getAsInt());
            event.replySuccess("Set the volume to " + volume.getAsInt() + "%");
        }
    }
}