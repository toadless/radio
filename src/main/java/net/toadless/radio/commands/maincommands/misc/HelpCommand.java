package net.toadless.radio.commands.maincommands.misc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.dv8tion.jda.api.EmbedBuilder;
import net.toadless.radio.Constants;
import net.toadless.radio.Radio;
import net.toadless.radio.modules.CommandModule;
import net.toadless.radio.modules.PaginationModule;
import net.toadless.radio.objects.command.Command;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.exception.CommandException;
import net.toadless.radio.util.StringUtils;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings ("unused")
public class HelpCommand extends Command
{
    public HelpCommand()
    {
        super("Help", "Shows the help menu for this bot.", "[page / command]");
        addAliases("help", "?", "howto", "commands");
    }

    @Override
    public void run(@NotNull List<String> args, @NotNull CommandEvent event, @NotNull Consumer<CommandException> failure)
    {
        if (!args.isEmpty())
        {
            Command command = event.getRadio().getModules().get(CommandModule.class).getCommandMap().get(args.get(0));
            if (command != null)
            {
                event.sendMessage(generateHelpPerCommand(command, event.getPrefix()));
                return;
            }
        }

        List<Command> commands = getCommands(event.getRadio());
        ArrayList<String> pages = new ArrayList<>();

        StringBuilder commandMessage = new StringBuilder()
                .append("**")
                .append(commands.size())
                .append(" ")
                .append(StringUtils.plurify("command", commands.size()))
                .append("**\n");

        for(Command cmd: commands)
        {
            String formattedCommand = new StringBuilder()
                    .append("`")
                    .append(cmd.getName())
                    .append("` - *")
                    .append(cmd.getDescription())
                    .append("*\n")
                    .toString();

            if (commandMessage.length() + formattedCommand.length() >= 2048)
            {
                pages.add(commandMessage.toString());
                commandMessage = new StringBuilder();
            }

            commandMessage.append(formattedCommand);
        }

        pages.add(commandMessage.toString());

        event.getRadio().getModules().get(PaginationModule.class).create(
                event.getChannel(),
                event.getMember().getIdLong(),
                pages.size(),
                (page, embedBuilder) -> embedBuilder.setColor(Constants.EMBED_COLOUR)
                        .setDescription(pages.get(page))
                        .setTimestamp(Instant.now())
        );
    }

    public List<Command> getCommands(Radio radio)
    {
        List<Command> commands = new ArrayList<>();
        for (Command cmd : radio.getModules().get(CommandModule.class).getCommandMap().values())
        {
            if (!commands.contains(cmd))
            {
                commands.add(cmd);
            }
        }

        return commands;
    }

    private EmbedBuilder generateHelpPerCommand(Command command, String prefix)
    {
        EmbedBuilder result = new EmbedBuilder()
                .setTitle("**Help for " + command.getName() + "**")
                .setFooter("<> Optional;  [] Required; {} Maximum Quantity | ");
        result.addField(prefix + command.getAliases().get(0), command.getDescription() + "\n`Syntax: " + command.getSyntax() + "`", false);
        if (command.hasChildren())
        {
            command.getChildren().forEach(
                    child ->
                            result.addField(prefix + command.getAliases().get(0) + " " + child.getName(), child.getDescription() + "\n`Syntax: " + child.getSyntax() + "`", false));
        }
        return result;
    }
}