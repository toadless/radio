package net.toadless.radio.commands.maincommands.music;

import net.toadless.radio.modules.MusicModule;
import net.toadless.radio.objects.command.Command;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.command.CommandFlag;
import net.toadless.radio.objects.exception.CommandException;
import net.toadless.radio.objects.exception.CommandSyntaxException;
import net.toadless.radio.objects.music.GuildMusicManager;
import net.toadless.radio.util.CommandChecks;
import net.toadless.radio.util.Parser;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.OptionalInt;
import java.util.function.Consumer;

@SuppressWarnings ("unused")
public class BassBoostCommand extends Command
{
    public BassBoostCommand()
    {
        super("BassBoost", "Sets the bass-boost level, 0 - 200!", "[level {0-200}]");
        addAliases("bassboost", "bass");
        addFlags(CommandFlag.GUILD_ONLY);
    }

    @Override
    public void run(@NotNull List<String> args, @NotNull CommandEvent event, @NotNull Consumer<CommandException> failure)
    {
        MusicModule musicModule = event.getRadio().getModules().get(MusicModule.class);
        GuildMusicManager manager = musicModule.getGuildMusicManager(event.getGuild());

        if (CommandChecks.sharesVoice(event, failure)) return;
        if (CommandChecks.boundToChannel(manager, event.getChannel(), failure)) return;

        if (event.getArgs().isEmpty())
        {
            event.replySuccess("Current bass-boost level `" + manager.getScheduler().getBassBoostPercentage() + "`%!");
            return;
        }

        if (CommandChecks.isUserDj(event, failure)) return;
        if (CommandChecks.argsSizeSubceeds(event, 1, failure)) return;
        if (CommandChecks.argsEmpty(event, failure)) return; // redundant

        OptionalInt unsignedPercentage = new Parser(args.get(0), event).parseAsUnsignedIntWithZero();

        if (unsignedPercentage.isEmpty())
        {
            return;
        }

        float percentage = unsignedPercentage.getAsInt();

        if (percentage > 200 || percentage < 0)
        {
            failure.accept(new CommandSyntaxException(this));
            return;
        }

        manager.getScheduler().bassBoost(percentage);
        event.replySuccess("Set the bass-boost level to `" + unsignedPercentage.getAsInt() + "`!");
    }
}