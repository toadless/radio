package net.toadless.radio.commands.subcommands.dj;

import net.dv8tion.jda.api.Permission;
import net.toadless.radio.objects.cache.GuildSettingsCache;
import net.toadless.radio.objects.command.Command;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.command.CommandFlag;
import net.toadless.radio.objects.exception.CommandException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class DjRemoveCommand extends Command
{
    public DjRemoveCommand(Command parent)
    {
        super(parent, "Remove", "Removes the DJ role.", "[none]");
        addFlags(CommandFlag.GUILD_ONLY);
        addMemberPermissions(Permission.MANAGE_SERVER);
        addAliases("remove", "reset");
    }

    @Override
    public void run(@NotNull List<String> args, @NotNull CommandEvent event, @NotNull Consumer<CommandException> failure)
    {
        GuildSettingsCache guildSettingsCache = GuildSettingsCache.getCache(event.getGuildIdLong(), event.getRadio());

        try
        {
            guildSettingsCache.setDjRole(-1L);
            event.replySuccess("Successfully removed the DJ role.");
        }
        catch (Exception exception)
        {
            event.replyError("Unable to remove the DJ role.");
        }
    }
}