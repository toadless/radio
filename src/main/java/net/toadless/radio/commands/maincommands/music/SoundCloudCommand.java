package net.toadless.radio.commands.maincommands.music;

import net.dv8tion.jda.api.entities.VoiceChannel;
import net.toadless.radio.modules.MusicModule;
import net.toadless.radio.objects.command.Command;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.command.CommandFlag;
import net.toadless.radio.objects.exception.CommandException;
import net.toadless.radio.objects.music.GuildMusicManager;
import net.toadless.radio.objects.music.SearchEngine;
import net.toadless.radio.util.CommandChecks;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings ("unused")
public class SoundCloudCommand extends Command
{
    public SoundCloudCommand()
    {
        super("Soundcloud", "Plays music from Soundcloud.", "[song]");
        addAliases("soundcloud", "sc");
        addFlags(CommandFlag.GUILD_ONLY);
    }

    @Override
    public void run(@NotNull List<String> args, @NotNull CommandEvent event, @NotNull Consumer<CommandException> failure)
    {
        MusicModule musicModule = event.getRadio().getModules().get(MusicModule.class);
        GuildMusicManager manager = musicModule.getGuildMusicManager(event.getGuild());

        if (CommandChecks.argsEmpty(event, failure)) return;
        if (CommandChecks.sharesVoice(event, failure)) return;
        if (CommandChecks.boundToChannel(manager, event.getChannel(), failure)) return;
        if (CommandChecks.isUserDj(event, failure)) return;

        VoiceChannel channel = event.getMember().getVoiceState().getChannel(); //Safe due to CommandChecks
        String query = String.join("", args);

        manager.bind(event.getChannel());
        musicModule.play(manager, query, failure, event, SearchEngine.SOUNDCLOUD); // if spotify will be overridden
    }
}