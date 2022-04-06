package net.toadless.radio.objects.music.loaders;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.toadless.radio.Constants;
import net.toadless.radio.modules.PaginationModule;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.exception.CommandException;
import net.toadless.radio.objects.exception.CommandInputException;
import net.toadless.radio.objects.exception.CommandResultException;
import net.toadless.radio.objects.music.GuildMusicManager;
import net.toadless.radio.util.CommandUtils;
import net.toadless.radio.util.Parser;
import net.toadless.radio.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ExactSearchAudioLoader implements AudioLoadResultHandler
{
    private final GuildMusicManager manager;
    private final CommandEvent event;
    private final Consumer<CommandException> failure;
    private final VoiceChannel channel;

    public ExactSearchAudioLoader(GuildMusicManager manager, Consumer<CommandException> failure, CommandEvent event, VoiceChannel channel)
    {
        this.manager = manager;
        this.failure = failure;
        this.event = event;
        this.channel = channel;
    }

    @Override
    public void trackLoaded(AudioTrack track)
    {
        // wont get called
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist)
    {
        if (!playlist.isSearchResult())
        {
            failure.accept(new CommandException("An unknown exception has occurred."));
            return;
        }

        try
        {
            List<AudioTrack> tracks = playlist.getTracks();

            if (tracks.isEmpty())
            {
                failure.accept(new CommandResultException("No results were found."));
                return;
            }

            StringBuilder trackMessage = new StringBuilder()
                    .append("**")
                    .append(tracks.size())
                    .append(" ")
                    .append(StringUtils.plurify("track", tracks.size()))
                    .append(" have been found")
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
                            .setFooter("Please respond with the number that you would like to queue.")
            );

            event.getRadio().getEventWaiter().waitForEvent(
                    GuildMessageReceivedEvent.class,
                    msg -> msg.getAuthor().equals(event.getAuthor()),
                    msg ->
                    {
                        OptionalInt optionalTrackNumber = new Parser(msg.getMessage().getContentRaw(), event).parseAsUnsignedInt();

                        if (optionalTrackNumber.isEmpty())
                        {
                            failure.accept(new CommandInputException("You must respond with a number."));
                            return;
                        }

                        int trackNumber = optionalTrackNumber.getAsInt();

                        if (trackNumber > tracks.size() || trackNumber < 1)
                        {
                            failure.accept(new CommandInputException("You must provide a valid number."));
                            return;
                        }

                        AudioTrack track = tracks.get(trackNumber - 1);

                        if (manager.isPlaying())
                        {
                            event.replySuccess("Added **" + track.getInfo().title + "** to the queue.");
                        }

                        manager.play(channel, track, event.getAuthor());
                    },
                    20,
                    TimeUnit.SECONDS,
                    () -> event.replyError("Sorry, you too to long to respond.")
            );
        } catch (Exception exception)
        {
            failure.accept(new CommandException("An unknown exception has occurred."));
        }
    }

    @Override
    public void noMatches()
    {
        failure.accept(new CommandResultException("Couldn't find anything matching your query."));
    }

    @Override
    public void loadFailed(FriendlyException exception)
    {
        failure.accept(new CommandResultException("An error occurred while loading the song."));
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
                .toString();
    }
}