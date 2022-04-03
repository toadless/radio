package net.toadless.radio.objects.music;

import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.toadless.radio.main.Constants;
import net.toadless.radio.util.StringUtils;

import static net.toadless.radio.modules.MusicModule.BASS_BOOST;

public class TrackScheduler extends AudioEventAdapter
{
    private final AudioPlayer player;
    private final LinkedList<AudioTrack> queue;
    private final LinkedList<AudioTrack> history;
    private final GuildMusicManager handler;

    private final EqualizerFactory equalizer;

    private RepeatMode repeatMode;
    private float bassBoostPercentage;

    public TrackScheduler(AudioPlayer player, GuildMusicManager handler)
    {
        this.player = player;
        this.queue = new LinkedList<>();
        this.history = new LinkedList<>();
        this.handler = handler;
        this.equalizer = new EqualizerFactory();

        this.repeatMode = RepeatMode.OFF;

        this.player.setFilterFactory(equalizer);
        this.player.setFrameBufferDuration(500); // prevent bass boost taking time to take effect

        this.bassBoostPercentage = 0.00f;
    }

    public void queue(AudioTrack track, User user)
    {
        track.setUserData(user);
        if (!player.startTrack(track, true))
        {
            synchronized (queue)
            {
                queue.offer(track);
            }
        }
    }

    public List<AudioTrack> getQueue()
    {
        return queue;
    }

    public void bassBoost(float percentage)
    {
        this.bassBoostPercentage = percentage;

        final float multiplier = percentage / 100.00f;

        for (int i = 0; i < BASS_BOOST.length; i++)
        {
            equalizer.setGain(i, BASS_BOOST[i] * multiplier);
        }
    }

    public void skipOne(boolean trackEnded, boolean force)
    {
        if (!trackEnded) this.history.push(this.player.getPlayingTrack());

        if (this.repeatMode == RepeatMode.SONG && !force)
        {
            this.player.startTrack(this.history.poll().makeClone(), false);
            return;
        }

        if (this.repeatMode == RepeatMode.QUEUE)
        {
            synchronized (queue)
            {
                queue.offer(this.history.poll().makeClone());
            }
        }

        synchronized (queue)
        {
            player.startTrack(queue.poll(), false);
        }
        player.setPaused(false);
    }

    public boolean playPrevious()
    {
        if (!hasPrevious()) return false;
        if (this.player.getPlayingTrack() != null) this.queue.addFirst(this.player.getPlayingTrack().makeClone());

        synchronized (history)
        {
            player.startTrack(history.poll().makeClone(), false);
        }
        player.setPaused(false);

        return true;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason)
    {
        this.history.push(track);

        if (endReason.mayStartNext) skipOne(true, false);
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track)
    {
        Duration length = Duration.between(LocalDateTime.now(), LocalDateTime.now().plusSeconds(track.getDuration() / 1000));
        Duration passed = Duration.between(LocalDateTime.now(), LocalDateTime.now().plusSeconds(track.getPosition() / 1000));

        if (handler != null && handler.getChannel() != null)
        {
            handler.sendController(new EmbedBuilder()
                    .setTitle("Now playing")
                    .setDescription(
                            "[" + track.getInfo().title + "](" + track.getInfo().uri + ")" +
                                    "\n**Author**: " + track.getInfo().author +
                                    "\n**Position**: " + StringUtils.parseDuration(passed) +
                                    "\n**Length**: " + StringUtils.parseDuration(length) +
                                    "\n**Requested by**: " + track.getUserData(User.class).getAsMention())
                    .setColor(Constants.EMBED_COLOUR)
                    .setTimestamp(Instant.now())
                    .build());
        }
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs)
    {
        if (handler != null && handler.getChannel() != null)
        {
            handler.getChannel().sendMessageEmbeds(new EmbedBuilder()
                    .setTitle("Something went wrong")
                    .setDescription("An error occurred while playing " + track.getInfo().title)
                    .setColor(Constants.EMBED_COLOUR)
                    .setTimestamp(Instant.now())
                    .build()).queue(null, error -> handler.bind(null));
        }
    }

    public boolean hasNext()
    {
        return queue.peek() != null;
    }

    public boolean hasPrevious()
    {
        return history.peek() != null;
    }

    public void clear()
    {
        queue.clear();
    }

    public void shuffle()
    {
        Collections.shuffle(queue);
    }

    public RepeatMode getRepeatMode()
    {
        return this.repeatMode;
    }

    public void setRepeatMode(RepeatMode repeatMode)
    {
        this.repeatMode = repeatMode;
    }

    public float getBassBoostPercentage()
    {
        return this.bassBoostPercentage;
    }
}