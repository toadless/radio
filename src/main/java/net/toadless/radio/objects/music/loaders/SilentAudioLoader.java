package net.toadless.radio.objects.music.loaders;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.music.GuildMusicManager;

public class SilentAudioLoader implements AudioLoadResultHandler
{
    private final GuildMusicManager manager;
    private final CommandEvent event;
    private final VoiceChannel channel;

    public SilentAudioLoader(GuildMusicManager manager, CommandEvent event, VoiceChannel channel)
    {
        this.manager = manager;
        this.event = event;
        this.channel = channel;
    }

    @Override
    public void trackLoaded(AudioTrack track)
    {
        manager.play(channel, track, event.getAuthor()); //Safe due to CommandChecks
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist)
    {
        if (playlist.isSearchResult())
        {
            AudioTrack track = playlist.getTracks().get(0);
            manager.play(channel, track, event.getAuthor()); //Safe due to CommandChecks
        }
        else
        {
            manager.playAll(channel, playlist.getTracks(), event.getAuthor()); //Safe due to CommandChecks
        }

    }

    @Override
    public void noMatches()
    {
        // i should prob do stuff here
    }

    @Override
    public void loadFailed(FriendlyException exception)
    {
        // i should prob do stuff here
    }
}
