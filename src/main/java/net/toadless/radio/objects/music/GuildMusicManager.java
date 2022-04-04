package net.toadless.radio.objects.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.List;
import javax.annotation.Nullable;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.AudioManager;
import net.toadless.radio.objects.Emoji;
import org.jetbrains.annotations.NotNull;

public class GuildMusicManager
{
    private final AudioPlayer player;
    private final TrackScheduler scheduler;
    private MessageChannel channel;
    private long controllerId;
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

    public void sendController(MessageEmbed embed)
    {
        removeOldController();

        getChannel().sendMessageEmbeds(embed).queue(message ->
        {
            setControllerId(message.getIdLong());

            message.addReaction(Emoji.ARROW_LEFT.getAsReaction()).queue(success -> {}, failure -> {});
            message.addReaction(Emoji.PLAY_PAUSE.getAsReaction()).queue(success -> {}, failure -> {});
            message.addReaction(Emoji.ARROW_RIGHT.getAsReaction()).queue(success -> {}, failure -> {});
            message.addReaction(Emoji.VOLUME_DOWN.getAsReaction()).queue(success -> {}, failure -> {});
            message.addReaction(Emoji.VOLUME_UP.getAsReaction()).queue(success -> {}, failure -> {});
            message.addReaction(Emoji.SHUFFLE.getAsReaction()).queue(success -> {}, failure -> {});
            message.addReaction(Emoji.REPEAT.getAsReaction()).queue(success -> {}, failure -> {});
            message.addReaction(Emoji.CROSS.getAsReaction()).queue(success -> {}, failure -> {});
        }, error ->
        {
            bind(null);
            setControllerId(-1L);
        });
    }

    public void removeOldController()
    {
        if (getControllerId() == -1L) return;

        try
        {
            getChannel().retrieveMessageById(getControllerId()).queue(message -> message.delete().queue(msg -> {}, throwable -> {}), throwable -> {});
        } catch (Exception ignored)
        {}
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

    public int getVolume()
    {
        return player.getVolume();
    }

    public void unbind()
    {
        this.channel = null;
    }

    public long getControllerId()
    {
        return controllerId;
    }

    public void setControllerId(long controllerId)
    {
        this.controllerId = controllerId;
    }
}