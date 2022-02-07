package net.toadless.radio.commands.maincommands.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Consumer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.toadless.radio.Constants;
import net.toadless.radio.Radio;
import net.toadless.radio.modules.CommandModule;
import net.toadless.radio.objects.command.Command;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.exception.CommandException;
import net.toadless.radio.objects.exception.CommandInputException;
import net.toadless.radio.util.Parser;
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
        OptionalInt page;
        if (args.isEmpty())
        {
            page = OptionalInt.of(1);
        }
        else
        {
            Command command = event.getRadio().getModules().get(CommandModule.class).getCommandMap().get(args.get(0));
            if (command == null)
            {
                page = new Parser(args.get(0), event).parseAsUnsignedInt();
            }
            else
            {
                event.sendMessage(generateHelpPerCommand(command, event.getPrefix()));
                return;
            }
        }


        if (page.isPresent())
        {
            if (page.getAsInt() + 1 > getHelpPages(event.getPrefix(), event.getRadio()).size() + 1)
            {
                failure.accept(new CommandInputException("Page " + args.get(0) + " does not exist."));
                return;
            }

            List<EmbedBuilder> pages = getHelpPages(event.getPrefix(), event.getRadio());
            event.sendMessage(pages.get(page.getAsInt() - 1).setTitle("Help page " + page.getAsInt() + " / " + pages.size()));
        }
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

    private List<EmbedBuilder> getHelpPages(String prefix, Radio radio)
    {

        List<EmbedBuilder> result = new ArrayList<>();
        List<Command> commands = new ArrayList<>();
        for (Command cmd : radio.getModules().get(CommandModule.class).getCommandMap().values())
        {
            if (!commands.contains(cmd))
            {
                commands.add(cmd);
            }
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();
        int fieldCount = 0;
        for (int i = 0; i < commands.size(); i++)
        {
            Command cmd = commands.get(i);
            if (fieldCount < 6)
            {
                fieldCount++;
                embedBuilder.addField(cmd.getName(), cmd.getDescription() + "\n**" + prefix + cmd.getAliases().get(0) + "**`" + cmd.getSyntax() + "`", false);
                embedBuilder.setColor(Constants.EMBED_COLOUR);
                embedBuilder.setFooter("<> Optional;  [] Required; {} Maximum Quantity | ");
            }
            else
            {
                result.add(embedBuilder);
                embedBuilder = new EmbedBuilder();
                fieldCount = 0;
                i--;
            }
        }
        return result;
    }
}