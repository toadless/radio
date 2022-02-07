package net.toadless.radio.commands.maincommands.misc;

import java.util.List;
import java.util.function.Consumer;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.toadless.radio.objects.command.Command;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.exception.CommandException;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings ("unused")
public class PingCommand extends Command
{
    public PingCommand()
    {
        super("Ping", "Shows the bot's ping to Discord.", "[none]");
        addAliases("ping");
    }

    @Override
    public void run(@NotNull List<String> args, @NotNull CommandEvent event, @NotNull Consumer<CommandException> failure)
    {
        JDA jda = event.getJDA();
        jda.getRestPing().queue(
                ping ->
                {
                    int oCount = (int) (ping / 100);

                    if (oCount > 252)
                    {
                        oCount = 252;
                    }

                    event.sendMessage(new EmbedBuilder()
                            .setTitle("P" + "o".repeat(oCount) + "ng.")
                            .setDescription(
                                    "**Shard ID**: " + jda.getShardInfo().getShardId()
                                            + "\n**REST Ping**: " + ping
                                            + "ms\n**Gateway Ping**: " + jda.getGatewayPing() + "ms"));
                });
    }
}