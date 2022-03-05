package net.toadless.radio.commands.maincommands.music;

import net.toadless.radio.modules.MusicModule;
import net.toadless.radio.objects.Emoji;
import net.toadless.radio.objects.command.Command;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.command.CommandFlag;
import net.toadless.radio.objects.exception.CommandException;
import net.toadless.radio.objects.music.GuildMusicManager;
import net.toadless.radio.util.CommandChecks;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class LoopCommand extends Command
{

    public LoopCommand()
    {
        super("Loop", "Loops the current song.", "[none]");
        addAliases("loop", "repeat");
        addFlags(CommandFlag.GUILD_ONLY);
    }

    @Override
    public void run(@NotNull List<String> args, @NotNull CommandEvent event, @NotNull Consumer<CommandException> failure)
    {
        MusicModule musicModule = event.getRadio().getModules().get(MusicModule.class);
        GuildMusicManager manager = musicModule.getGuildMusicManager(event.getGuild());

        if (CommandChecks.boundToChannel(manager, event.getChannel(), failure)) return;
        if (CommandChecks.sharesVoice(event, failure)) return;
        if (CommandChecks.isUserDj(event, failure)) return;

        manager.getScheduler().toggleLoop();
        boolean loop = manager.getScheduler().getLoop();

        event.replySuccess("Looping " + (loop ? "Enabled" : "Disabled") + " " + Emoji.REPEAT.getAsChat());
    }
}