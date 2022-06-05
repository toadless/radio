package net.toadless.radio.commands.maincommands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.toadless.radio.modules.MusicModule;
import net.toadless.radio.objects.command.Command;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.exception.CommandException;
import net.toadless.radio.objects.exception.CommandResultException;
import net.toadless.radio.objects.exception.CommandSyntaxException;
import net.toadless.radio.objects.music.GuildMusicManager;
import net.toadless.radio.util.CommandChecks;
import net.toadless.radio.util.CommandUtils;
import net.toadless.radio.util.Parser;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

public class SeekCommand extends Command
{
    public SeekCommand()
    {
        super("Seek", "Seeks to the provided position in the currently playing song!", "[Position, Example: 1m 30s]");
        addAliases("seek");
    }

    @Override
    public void run(@NotNull List<String> args, @NotNull CommandEvent event, @NotNull Consumer<CommandException> failure)
    {
        if (CommandChecks.argsEmpty(event, failure)) return;

        MusicModule musicModule = event.getRadio().getModules().get(MusicModule.class);
        GuildMusicManager manager = musicModule.getGuildMusicManager(event.getGuild());

        if (CommandChecks.boundToChannel(manager, event.getChannel(), failure)) return;
        if (CommandChecks.sharesVoice(event, failure)) return;
        if (CommandChecks.isUserDj(event, failure)) return;

        AudioTrack currentTrack = manager.getPlayer().getPlayingTrack();

        if (currentTrack == null)
        {
            failure.accept(new CommandResultException("Nothing is playing."));
            return;
        }

        LocalDateTime parsedTime = new Parser(String.join("", args), event).parseAsDuration();

        if (parsedTime == null)
        {
            failure.accept(new CommandSyntaxException(event));
            return;
        }

        Duration diff = Duration.between(LocalDateTime.now(), parsedTime);
        long position = diff.toMillis() + 1;

        if (currentTrack.getDuration() < position)
        {
            failure.accept(new CommandResultException("The provided position is outside of the tracks length."));
            return;
        }

        manager.getPlayer().getPlayingTrack().setPosition(position);
        event.replySuccess("Successfully set the tracks position to `" + CommandUtils.formatDuration(position) + "`.");
    }
}