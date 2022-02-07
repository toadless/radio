package net.toadless.radio.commands.maincommands.misc;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.toadless.radio.commands.subcommands.dj.DjRemoveCommand;
import net.toadless.radio.commands.subcommands.dj.DjSetCommand;
import net.toadless.radio.objects.cache.GuildSettingsCache;
import net.toadless.radio.objects.command.Command;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.command.CommandFlag;
import net.toadless.radio.objects.exception.CommandException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class DjCommand extends Command
{
    public DjCommand()
    {
        super("DJ", "View and modify the guilds DJ role.", "<set/remove>");
        addAliases("dj", "djrole");
        addFlags(CommandFlag.GUILD_ONLY);
        addChildren(
                new DjRemoveCommand(this),
                new DjSetCommand(this)
        );
    }

    @Override
    public void run(@NotNull List<String> args, @NotNull CommandEvent event, @NotNull Consumer<CommandException> failure)
    {
        GuildSettingsCache guildSettingsCache = GuildSettingsCache.getCache(event.getGuildIdLong(), event.getRadio());

        if (guildSettingsCache.getDjRole() == -1L)
        {
            event.replyError("This guild does not currently have a DJ role.");
            return;
        }

        try
        {
            Role djRole = event.getGuild().getRoleById(guildSettingsCache.getDjRole());
            event.replySuccess("Current dj role: " + djRole.getAsMention());
        }
        catch (Exception exception)
        {
            event.replyError("Unable to fetch the current DJ role.");
        }
    }
}