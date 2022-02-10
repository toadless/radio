package net.toadless.radio.objects.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import java.util.List;
import javax.annotation.Nullable;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

public class GuildMusicManager
{
    private final AudioPlayer player;
    private final TrackScheduler scheduler;
    private MessageChannel channel;
    private int volume = 30;

    public GuildMusicManager(AudioPlayerManager manager)
    {
        player = manager.createPlayer();
        scheduler = new TrackScheduler(player, this);
        player.addListener(scheduler);
    }

    public MessageChannel getChannel()
    {
        return channel;
    }

    public AudioPlayer getPlayer()
    {
        return player;
    }

    public TrackScheduler getScheduler()
    {
        return scheduler;
    }

    public boolean isPlaying()
    {
        return player.getPlayingTrack() != null;
    }

    public AudioPlayerSendHandler getSendHandler()
    {
        return new AudioPlayerSendHandler(player);
    }

    public void play(VoiceChannel channel, AudioTrack track, User user)
    {
        AudioManager manager = channel.getGuild().getAudioManager();
        manager.openAudioConnection(channel);
        scheduler.queue(track, user);
        player.setVolume(volume);
    }

    public void playAll(VoiceChannel channel, List<AudioTrack> tracks, User user)
    {
        AudioManager manager = channel.getGuild().getAudioManager();
        manager.openAudioConnection(channel);
        tracks.forEach(track -> scheduler.queue(track, user));
        player.setVolume(volume);
    }

    public void togglePause()
    {
        player.setPaused(!player.isPaused());
    }

    public boolean getPaused()
    {
        return player.isPaused();
    }

    public void leave(@NotNull Guild guild)
    {
        AudioManager manager = guild.getAudioManager();
        manager.closeAudioConnection();
    }

    public void join(@NotNull VoiceChannel channel)
    {
        AudioManager manager = channel.getGuild().getAudioManager();
        manager.openAudioConnection(channel);
        player.setVolume(volume);
    }

    public void kill(@NotNull Guild guild)
    {
        leave(guild);
        player.destroy();
        scheduler.clear();
    }

    public void bind(@Nullable MessageChannel channel)
    {
        if (channel == null)
        {
            this.channel = null;
            return;
        }
        if (this.channel == null)
        {
            this.channel = channel;
        }
    }

    public void setVolume(int volume)
    {
        this.volume = volume;
        player.setVolume(volume);
    }

    public void unbind()
    {
        this.channel = null;
    }
}