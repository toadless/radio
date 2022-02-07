package net.toadless.radio.commands.subcommands.dj;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.toadless.radio.objects.cache.GuildSettingsCache;
import net.toadless.radio.objects.command.Command;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.command.CommandFlag;
import net.toadless.radio.objects.exception.CommandException;
import net.toadless.radio.util.EmbedUtils;
import net.toadless.radio.util.Parser;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class DjSetCommand extends Command
{
    public DjSetCommand(Command parent)
    {
        super(parent, "Set", "Sets the DJ role.", "[role]");
        addFlags(CommandFlag.GUILD_ONLY);
        addMemberPermissions(Permission.MANAGE_SERVER);
        addAliases("set", "change");
    }

    @Override
    public void run(@NotNull List<String> args, @NotNull CommandEvent event, @NotNull Consumer<CommandException> failure)
    {
        MessageChannel channel = event.getChannel();
        GuildSettingsCache config = GuildSettingsCache.getCache(event.getGuildIdLong(), event.getRadio());

        if (args.isEmpty())
        {
            event.replyError("Please provide a new DJ role.");
            return;
        }

        new Parser(args.get(0), event).parseAsRole(newRole ->
        {
            config.setDjRole(newRole.getIdLong());
            EmbedUtils.sendSuccess(channel, "Set the guilds DJ role to " + newRole.getAsMention() + ".");
        });
    }
}