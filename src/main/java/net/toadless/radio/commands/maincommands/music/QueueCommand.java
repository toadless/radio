package net.toadless.radio.commands.maincommands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.entities.User;
import net.toadless.radio.Constants;
import net.toadless.radio.modules.PaginationModule;
import net.toadless.radio.objects.command.Command;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.command.CommandFlag;
import net.toadless.radio.objects.exception.CommandException;
import net.toadless.radio.objects.exception.CommandResultException;
import net.toadless.radio.objects.music.GuildMusicManager;
import net.toadless.radio.util.CommandChecks;
import net.toadless.radio.modules.MusicModule;
import net.toadless.radio.util.CommandUtils;
import net.toadless.radio.util.StringUtils;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings ("unused")
public class QueueCommand extends Command
{
    public QueueCommand()
    {
        super("Queue", "Shows the queue.", "[none]");
        addAliases("queue", "q");
        addFlags(CommandFlag.GUILD_ONLY);
    }

    @Override
    public void run(@NotNull List<String> args, @NotNull CommandEvent event, @NotNull Consumer<CommandException> failure)
    {
        MusicModule musicModule = event.getRadio().getModules().get(MusicModule.class);
        GuildMusicManager manager = musicModule.getGuildMusicManager(event.getGuild());

        if (CommandChecks.boundToChannel(manager, event.getChannel(), failure)) return;
        if (CommandChecks.sharesVoice(event, failure)) return;

        Collection<AudioTrack> tracks = manager.getScheduler().getQueue();

        if (tracks.isEmpty())
        {
            failure.accept(new CommandResultException("The queue is empty."));
            return;
        }

        StringBuilder trackMessage = new StringBuilder()
                .append("**")
                .append("Currently ")
                .append(tracks.size())
                .append(" ")
                .append(StringUtils.plurify("track", tracks.size()))
                .append(" are queued")
                .append(":**\n");

        ArrayList<String> pages = new ArrayList<>();

        int i = 1;

        for(AudioTrack track : tracks)
        {
            String formattedTrack = i + ". " + formatTrackWithInfo(track) + "\n";

            if (trackMessage.length() + formattedTrack.length() >= 2048)
            {
                pages.add(trackMessage.toString());
                trackMessage = new StringBuilder();
            }

            trackMessage.append(formattedTrack);
            i++;
        }

        pages.add(trackMessage.toString());

        event.getRadio().getModules().get(PaginationModule.class).create(
                event.getChannel(),
                event.getMember().getIdLong(),
                pages.size(),
                (page, embedBuilder) -> embedBuilder.setColor(Constants.EMBED_COLOUR)
                        .setDescription(pages.get(page))
                        .setTimestamp(Instant.now())
        );
    }

    public static String formatTrackWithInfo(AudioTrack track)
    {
        AudioTrackInfo info = track.getInfo();

        return new StringBuilder()
                .append("[`")
                .append(info.title)
                .append("`]")
                .append("(")
                .append(info.uri)
                .append(")")
                .append(" - ")
                .append(CommandUtils.formatDuration(info.length))
                .append(" [")
                .append(track.getUserData(User.class).getAsMention())
                .append("]")
                .toString();
    }
}