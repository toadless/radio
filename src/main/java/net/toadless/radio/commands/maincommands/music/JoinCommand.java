package net.toadless.radio.commands.maincommands.music;

import java.util.List;
import java.util.function.Consumer;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.toadless.radio.modules.MusicModule;
import net.toadless.radio.objects.command.Command;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.command.CommandFlag;
import net.toadless.radio.objects.exception.CommandException;
import net.toadless.radio.objects.music.GuildMusicManager;
import net.toadless.radio.util.CommandChecks;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings ("unused")
public class JoinCommand extends Command
{
    public JoinCommand()
    {
        super("Join", "Makes the bot join your VC.", "[none]");
        addAliases("join", "summon", "connect");
        addFlags(CommandFlag.GUILD_ONLY);
    }

    @Override
    public void run(@NotNull List<String> args, @NotNull CommandEvent event, @NotNull Consumer<CommandException> failure)
    {
        MusicModule musicModule = event.getRadio().getModules().get(MusicModule.class);
        GuildMusicManager manager = musicModule.getGuildMusicManager(event.getGuild());

        if (CommandChecks.sharesVoice(event, failure)) return;
        if (CommandChecks.boundToChannel(manager, event.getChannel(), failure)) return;
        if (CommandChecks.isUserDj(event, failure)) return;

        VoiceChannel channel = event.getMember().getVoiceState().getChannel();
        manager.join(channel);
        event.replySuccess("Joined " + channel.getName());
        manager.bind(event.getChannel());
    }
}