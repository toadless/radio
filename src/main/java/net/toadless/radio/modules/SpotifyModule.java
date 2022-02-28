package net.toadless.radio.modules;

import com.neovisionaries.i18n.CountryCode;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.toadless.radio.Radio;
import net.toadless.radio.objects.bot.ConfigOption;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.exception.CommandException;
import net.toadless.radio.objects.exception.CommandResultException;
import net.toadless.radio.objects.module.Module;
import net.toadless.radio.objects.module.Modules;
import net.toadless.radio.objects.music.GuildMusicManager;
import net.toadless.radio.objects.music.SearchEngine;
import net.toadless.radio.util.EmbedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;

public class SpotifyModule extends Module
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SpotifyModule.class);

    private final SpotifyApi spotify;
    private final ClientCredentialsRequest clientCredentialsRequest;

    public SpotifyModule(Radio radio, Modules modules)
    {
        super(radio, modules);

        this.spotify = new SpotifyApi.Builder()
                .setClientId(radio.getConfiguration().getString(ConfigOption.SPOTIFYID))
                .setClientSecret(radio.getConfiguration().getString(ConfigOption.SPOTIFYSECRET))
                .build();

        this.clientCredentialsRequest = this.spotify.clientCredentials().build();
        modules.addRepeatingTask(this::refreshAccessToken, TimeUnit.HOURS, 1);
    }

    private void refreshAccessToken()
    {
        try
        {
            this.spotify.setAccessToken(this.clientCredentialsRequest.execute().getAccessToken());
        }
        catch(Exception e)
        {
            LOGGER.error("Updating the access token failed. Retrying in 20 seconds", e);
            this.modules.addTask(this::refreshAccessToken, TimeUnit.SECONDS, 20);
        }
    }

    public void load(CommandEvent event, Matcher matcher, Consumer<CommandException> failure, VoiceChannel voiceChannel, GuildMusicManager manager)
    {
        String identifier = matcher.group("identifier");

        switch (matcher.group("type"))
        {
            case "album" -> loadAlbum(identifier, failure, event, voiceChannel);
            case "track" -> loadTrack(identifier, failure, event, manager);
            case "playlist" -> loadPlaylist(identifier, failure, event, voiceChannel);
            case "artist" -> loadArtist(identifier, failure, event, voiceChannel);
        }
    }

    private void loadAlbum(String id, Consumer<CommandException> failure, CommandEvent event, VoiceChannel voiceChannel)
    {
        this.spotify.getAlbumsTracks(id).build().executeAsync().thenAcceptAsync(tracks ->
        {
            var items = tracks.getItems();
            var toLoad = new ArrayList<String>();

            for(var track : items)
            {
                toLoad.add("ytsearch:" + track.getArtists()[0].getName() + " " + track.getName());
            }

            loadTracks(event, toLoad, voiceChannel);
        }).exceptionally(throwable ->
        {
            failure.accept(new CommandResultException(throwable.getMessage().contains("invalid id") ? "Album not found" : "There was an error while loading the album"));
            return null;
        });
    }

    private void loadTrack(String id, Consumer<CommandException> failure, CommandEvent event, GuildMusicManager manager)
    {
        this.spotify.getTrack(id).build().executeAsync().thenAcceptAsync(track ->
                this.modules.get(MusicModule.class).play(manager, track.getArtists()[0].getName() + " " + track.getName(), failure, event, SearchEngine.YOUTUBE)
        ).exceptionally(throwable ->
        {
            failure.accept(new CommandResultException(throwable.getMessage().contains("invalid id") ? "Track not found" : "There was an error while loading the track"));
            return null;
        });
    }

    private void loadPlaylist(String id, Consumer<CommandException> failure, CommandEvent event, VoiceChannel voiceChannel)
    {
        this.spotify.getPlaylistsItems(id).build().executeAsync().thenAcceptAsync(tracks ->
        {
            var items = tracks.getItems();
            var toLoad = new ArrayList<String>();

            for(var item : items)
            {
                var track = (Track) item.getTrack();
                toLoad.add("ytsearch:" + track.getArtists()[0].getName() + " " + track.getName());
            }

            loadTracks(event, toLoad, voiceChannel);
        }).exceptionally(throwable ->
        {
            failure.accept(new CommandResultException(throwable.getMessage().contains("Invalid playlist Id") ? "Playlist not found" : "There was an error while loading the playlist"));
            return null;
        });
    }

    private void loadArtist(String id, Consumer<CommandException> failure, CommandEvent event, VoiceChannel voiceChannel)
    {
        this.spotify.getArtistsTopTracks(id, CountryCode.SE).build().executeAsync().thenAcceptAsync(tracks ->
        {
            var toLoad = new ArrayList<String>();

            for(var track : tracks)
            {
                toLoad.add("ytsearch:" + track.getArtists()[0].getName() + " " + track.getName());
            }

            loadTracks(event, toLoad, voiceChannel);
        }).exceptionally(throwable ->
        {
            failure.accept(new CommandResultException(throwable.getMessage().contains("Invalid artist Id") ? "Artist not found" : "There was an error while loading the artist"));
            return null;
        });
    }

    private void loadTracks(CommandEvent event, List<String> toLoad, VoiceChannel voiceChannel)
    {
        EmbedUtils.sendSuccess(event.getChannel(), "Queueing " + toLoad.size() + " tracks!");

        toLoad.forEach(track -> {
            this.modules.get(MusicModule.class).playFromSpotify(event, track, voiceChannel);
        });
    }
}