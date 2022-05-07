package net.toadless.radio.modules;

import com.sedmelluq.discord.lavaplayer.natives.ConnectorNativeLibLoader;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.toadless.radio.Radio;
import net.toadless.radio.objects.cache.GuildSettingsCache;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.exception.CommandException;
import net.toadless.radio.objects.module.Module;
import net.toadless.radio.objects.module.Modules;
import net.toadless.radio.objects.music.GuildMusicManager;
import net.toadless.radio.objects.music.RepeatMode;
import net.toadless.radio.objects.music.SearchEngine;
import net.toadless.radio.objects.music.loaders.DefaultAudioLoader;
import net.toadless.radio.objects.music.loaders.SilentAudioLoader;
import net.toadless.radio.util.EmbedUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MusicModule extends Module
{
    public static final Pattern URL_PATTERN = Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]?");
    public static final Pattern SPOTIFY_URL_PATTERN = Pattern.compile("^(https?://)?(www\\.)?open\\.spotify\\.com/(user/[a-zA-Z0-9-_]+/)?(?<type>track|album|artist|playlist)/(?<identifier>[a-zA-Z0-9-_]+)?.+");

    public static final float[] BASS_BOOST = {
            0.2f,
            0.15f,
            0.1f,
            0.05f,
            0.0f,
            -0.05f,
            -0.1f,
            -0.1f,
            -0.1f,
            -0.1f,
            -0.1f,
            -0.1f,
            -0.1f,
            -0.1f,
            -0.1f
    };

    private final Map<Long, GuildMusicManager> musicHandlers;
    private final AudioPlayerManager playerManager;

    public MusicModule(Radio radio, Modules modules)
    {
        super(radio, modules);
        this.musicHandlers = new ConcurrentHashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerLocalSource(playerManager);
        AudioSourceManagers.registerRemoteSources(playerManager);

        playerManager.getConfiguration().setFilterHotSwapEnabled(true); // hotswap for the filters

        this.modules.addRepeatingTask(this::cleanupPlayers, TimeUnit.MINUTES, 1);

        ConnectorNativeLibLoader.loadConnectorLibrary();
    }

    public GuildMusicManager getGuildMusicManager(Guild guild)
    {
        GuildMusicManager manager = musicHandlers.get(guild.getIdLong());
        if (musicHandlers.get(guild.getIdLong()) == null)
        {
            manager = new GuildMusicManager(playerManager);
            musicHandlers.put(guild.getIdLong(), manager);
        }

        guild.getAudioManager().setSendingHandler(manager.getSendHandler());
        return manager;
    }

    public AudioPlayerManager getPlayerManager()
    {
        return playerManager;
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event)
    {
        if (event.getUserIdLong() == event.getJDA().getSelfUser().getIdLong())
        {
            return;
        }

        GuildMusicManager manager = this.musicHandlers.get(event.getGuild().getIdLong());
        if (manager == null)
        {
            return;
        }

        Member member = event.getMember();

        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState == null || voiceState.getChannel() == null || voiceState.getChannel().getIdLong() != voiceState.getChannel().getIdLong())
        {
            return;
        }

        long messageId = event.getMessageIdLong();

        if (messageId != manager.getControllerId())
        {
            return;
        }

        if (!isUserDj(member))
        {
            event.getChannel().sendMessage(member.getAsMention() + ", you need to be a DJ to perform this action!").queue();
            return;
        }

        switch (event.getReactionEmote().getAsReactionCode())
        {
            case "\u2B05\uFE0F" -> previousFromController(manager, member);
            case "\u27A1\uFE0F" -> skipFromController(manager, member);
            case "\u23EF" -> togglePauseFromController(manager, member);
            case "\uD83D\uDD00" -> shuffleFromController(manager, member);
            case "\uD83D\uDD09" -> setVolumeFromController(manager, member, manager.getPlayer().getVolume() - 10);
            case "\uD83D\uDD0A" -> setVolumeFromController(manager, member, manager.getPlayer().getVolume() + 10);
            case "\uD83D\uDD01" -> toggleLoopingFromController(manager, member);
            case "\u274C" -> cleanupPlayer(event.getGuild(), member.getAsMention() + " disconnected me.");
        }

        if (event.getGuild().getSelfMember().hasPermission(event.getChannel(), Permission.MESSAGE_MANAGE))
        {
            event.getReaction().removeReaction(event.getUser()).queue(success -> {}, failure -> {});
        }
    }

    private void skipFromController(GuildMusicManager manager, Member member)
    {
        manager.getScheduler().skipOne(false, true);

        if (manager.getChannel() == null) return;
        EmbedUtils.sendSuccess(manager.getChannel(), member.getAsMention() + " has skipped the track.");
    }

    private void previousFromController(GuildMusicManager manager, Member member)
    {
        boolean previous = manager.getScheduler().playPrevious();

        if (manager.getChannel() == null) return;

        if (previous) EmbedUtils.sendSuccess(manager.getChannel(), member.getAsMention() + " has skipped to the previous the track.");
        else EmbedUtils.sendError(manager.getChannel(), member.getAsMention() + ", there is no previous track.");
    }

    private void togglePauseFromController(GuildMusicManager manager, Member member)
    {
        manager.togglePause();
        boolean paused = manager.getPaused();

        if (manager.getChannel() == null) return;
        EmbedUtils.sendSuccess(manager.getChannel(), member.getAsMention() + " has " + (paused ? "paused" : "unpaused") + " the player.");
    }

    private void setVolumeFromController(GuildMusicManager manager, Member member, int volume)
    {
        int volBefore = manager.getVolume();
        manager.setVolume(volume);
        int volAfter = manager.getVolume();

        String action = (volBefore > volAfter ? "decreased" : "increased");

        if (manager.getChannel() == null) return;
        EmbedUtils.sendSuccess(manager.getChannel(), member.getAsMention() + " has " + action + " the volume.");
    }

    private void toggleLoopingFromController(GuildMusicManager manager, Member member)
    {
        RepeatMode repeatMode = manager.getScheduler().getRepeatMode();

        if (repeatMode == RepeatMode.OFF)
        {
            manager.getScheduler().setRepeatMode(RepeatMode.SONG);
        }
        else if (repeatMode == RepeatMode.SONG)
        {
            manager.getScheduler().setRepeatMode(RepeatMode.QUEUE);
        }
        else if (repeatMode == RepeatMode.QUEUE)
        {
            manager.getScheduler().setRepeatMode(RepeatMode.OFF);
        }

        if (manager.getChannel() == null) return;

        EmbedUtils.sendSuccess(
                manager.getChannel(),
                member.getAsMention() +
                        " has set the repeat mode to " +
                        manager.getScheduler().getRepeatMode().toString().toLowerCase() +
                        "!"
                );
    }

    private void shuffleFromController(GuildMusicManager manager, Member member)
    {
        manager.getScheduler().shuffle();

        if (manager.getChannel() == null) return;
        EmbedUtils.sendSuccess(manager.getChannel(), member.getAsMention() + " has shuffled the player.");
    }

    public void cleanupPlayers()
    {
        this.radio.getShardManager().getGuilds().forEach(guild ->
        {
            GuildMusicManager manager = musicHandlers.get(guild.getIdLong());
            VoiceChannel vc = guild.getAudioManager().getConnectedChannel();
            if (vc == null)
            {
                return; // if we aren't connected there's no point in checking.
            }

            long humansInVC = vc.getMembers().stream().filter(member -> !member.getUser().isBot()).count();
            if (humansInVC == 0)
            {
                manager.getPlayer().destroy();
                manager.leave(guild);
                manager.getScheduler().clear();
                manager.unbind();
                this.musicHandlers.remove(guild.getIdLong());
            }
        });
    }

    public void cleanupPlayer(Guild guild, String reason)
    {
        GuildMusicManager manager = musicHandlers.get(guild.getIdLong());

        if (manager == null) return;

        MessageChannel channel = manager.getChannel();

        manager.removeOldController();
        manager.getPlayer().destroy();
        manager.leave(guild);
        manager.getScheduler().clear();
        manager.unbind();
        this.musicHandlers.remove(guild.getIdLong());

        if (channel != null)
        {
            EmbedUtils.sendError(channel, reason);
        }
    }

    public boolean isUserDj(CommandEvent event)
    {
        GuildSettingsCache guildSettingsCache = GuildSettingsCache.getCache(event.getGuildIdLong(), radio);
        long djRole = guildSettingsCache.getDjRole();

        if (djRole == -1L) return true; // return true if dj role not setup
        else return event.getMember().getRoles().stream().anyMatch(role -> role.getIdLong() == djRole);
    }

    public boolean isUserDj(Member member)
    {
        GuildSettingsCache guildSettingsCache = GuildSettingsCache.getCache(member.getGuild().getIdLong(), radio);
        long djRole = guildSettingsCache.getDjRole();

        if (djRole == -1L) return true; // return true if dj role not setup
        else return member.getRoles().stream().anyMatch(role -> role.getIdLong() == djRole);
    }

    public void play(GuildMusicManager manager, String query, Consumer<CommandException> failure, CommandEvent event, SearchEngine searchEngine)
    {
        VoiceChannel channel = event.getMember().getVoiceState().getChannel(); //Safe due to CommandChecks

        Matcher matcher = SPOTIFY_URL_PATTERN.matcher(query);

        if (matcher.matches())
        {
            this.modules.get(SpotifyModule.class).load(event, matcher, failure, channel, manager);
            return;
        }

        if (!URL_PATTERN.matcher(query).matches())
        {
            switch(searchEngine)
            {
                case YOUTUBE -> query = "ytsearch:" + query;
                case SOUNDCLOUD -> query = "scsearch:" + query;
            }
        }

        getPlayerManager().loadItemOrdered(manager, query, new DefaultAudioLoader(manager, failure, event, channel));
    }

    public int getPlayers()
    {
        return musicHandlers.size();
    }

    public Radio getRadio()
    {
        return this.radio;
    }

    public void playFromSpotify(CommandEvent event, String query, VoiceChannel channel) // separate method to avoid spamming channel
    {
        GuildMusicManager manager = getGuildMusicManager(event.getGuild());
        getPlayerManager().loadItemOrdered(manager, query, new SilentAudioLoader(manager, event, channel));
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event)
    {
        if (event.getChannelLeft() != Objects.requireNonNull(Objects.requireNonNull(event.getGuild().getMemberById(event.getJDA().getSelfUser().getIdLong())).getVoiceState()).getChannel())
        {
            return;
        }

        if (event.getMember().equals(event.getGuild().getSelfMember()))
        {
            cleanupPlayer(event.getGuild(), "Disconnected due to being kicked.");
        }

        long humansInVC = event.getChannelLeft().getMembers().stream().filter(member -> !member.getUser().isBot()).count();
        if (humansInVC == 0)
        {
           cleanupPlayer(event.getGuild(), "Disconnected due to inactivity.");
        }
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event)
    {
        if (event.getChannelLeft() != Objects.requireNonNull(Objects.requireNonNull(event.getGuild().getMemberById(event.getJDA().getSelfUser().getIdLong())).getVoiceState()).getChannel())
        {
            return;
        }

        long humansInVC = event.getChannelLeft().getMembers().stream().filter(member -> !member.getUser().isBot()).count();
        if (humansInVC == 0)
        {
            cleanupPlayer(event.getGuild(), "Disconnected due to inactivity.");
        }
    }
}