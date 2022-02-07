package net.toadless.radio.commands.maincommands.developer;

import java.util.List;
import java.util.function.Consumer;

import net.toadless.radio.modules.DatabaseModule;
import net.toadless.radio.modules.MusicModule;
import net.toadless.radio.objects.command.Command;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.command.CommandFlag;
import net.toadless.radio.objects.exception.CommandException;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings ("unused")
public class ShutdownCommand extends Command
{
    public ShutdownCommand()
    {
        super("Shutdown", "Shuts the bot down gracefully.", "[none]");
        addFlags(CommandFlag.DEVELOPER_ONLY);
        addAliases("shutdown");
    }

    @Override
    public void run(@NotNull List<String> args, @NotNull CommandEvent event, @NotNull Consumer<CommandException> failure)
    {
        event.getRadio().getModules().get(DatabaseModule.class).close();
        event.getRadio().getModules().close();
        event.getJDA().getGuilds().forEach(guild -> event.getRadio().getModules().get(MusicModule.class).getGuildMusicManager(guild).kill(guild));
        event.getJDA().shutdown();

        event.getRadio().getLogger().warn("-- Quack was shutdown using shutdown command.");
        event.getRadio().getLogger().warn("-- Issued by: " + event.getAuthor().getAsTag());
        if (event.isFromGuild())
        {
            event.getRadio().getLogger().warn("-- In guild: " + event.getGuild().getName());
        }
        else
        {
            event.getRadio().getLogger().warn("-- In guild: " + "Shutdown in DMs.");
        }
        System.exit(0);
    }
}