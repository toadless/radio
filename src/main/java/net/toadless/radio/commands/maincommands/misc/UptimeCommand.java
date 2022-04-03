package net.toadless.radio.commands.maincommands.misc;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.toadless.radio.main.Constants;
import net.toadless.radio.objects.command.Command;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.exception.CommandException;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings ("unused")
public class UptimeCommand extends Command
{
    public UptimeCommand()
    {
        super("Uptime", "Displays the bots uptime.", "[none]");
        addAliases("uptime");
    }

    @Override
    public void run(@NotNull List<String> args, @NotNull CommandEvent event, @NotNull Consumer<CommandException> failure)
    {
        Duration uptime = Duration.between(event.getRadio().getStartTimestamp(), LocalDateTime.now());
        event.sendMessage(new EmbedBuilder()
                .setDescription(
                        "Uptime: " + uptime.toDaysPart() +
                                " days, " + uptime.toHoursPart() +
                                " hours, " + uptime.toSecondsPart() +
                                " seconds.")
                .setColor(Constants.EMBED_COLOUR));
    }
}