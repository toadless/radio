package net.toadless.radio.objects.music.loaders;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.exception.CommandException;
import net.toadless.radio.objects.exception.CommandResultException;
import net.toadless.radio.objects.music.GuildMusicManager;

import java.util.function.Consumer;

public class DefaultAudioLoader implements AudioLoadResultHandler
{
    private final GuildMusicManager manager;
    private final CommandEvent event;
    private final Consumer<CommandException> failure;
    private final VoiceChannel channel;

    public DefaultAudioLoader(GuildMusicManager manager, Consumer<CommandException> failure, CommandEvent event, VoiceChannel channel)
    {
        this.manager = manager;
        this.failure = failure;
        this.event = event;
        this.channel = channel;
    }

    @Override
    public void trackLoaded(AudioTrack track)
    {
        if (manager.isPlaying())
        {
            event.replySuccess("Added **" + track.getInfo().title + "** to the queue.");
        }
        manager.play(channel, track, event.getAuthor()); //Safe due to CommandChecks
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist)
    {
        try
        {
            if (playlist.isSearchResult())
            {
                AudioTrack track = playlist.getTracks().get(0);
                if (manager.isPlaying())
                {
                    event.replySuccess("Added **" + track.getInfo().title + "** to the queue.");
                }
                manager.play(channel, track, event.getAuthor()); //Safe due to CommandChecks
            }
            else
            {
                event.replySuccess("Added " + playlist.getTracks().size() + " tracks to the queue.");
                manager.playAll(channel, playlist.getTracks(), event.getAuthor()); //Safe due to CommandChecks
            }
        }
        catch (IndexOutOfBoundsException exception)
        {
            failure.accept(new CommandResultException("Couldn't find anything matchsing your query."));
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
}