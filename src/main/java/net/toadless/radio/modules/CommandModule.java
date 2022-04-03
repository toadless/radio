package net.toadless.radio.modules;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.toadless.radio.main.Constants;
import net.toadless.radio.main.Radio;
import net.toadless.radio.objects.cache.GuildSettingsCache;
import net.toadless.radio.objects.command.Command;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.module.Module;
import net.toadless.radio.objects.module.Modules;
import net.toadless.radio.util.EmbedUtils;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandModule extends Module
{
    public static final String COMMAND_PACKAGE = "net.toadless.radio.commands.maincommands";

    private final ClassGraph classGraph = new ClassGraph().acceptPackages(COMMAND_PACKAGE);
    private final Map<String, Command> commandMap;

    public CommandModule(Radio radio, Modules modules)
    {
        super(radio, modules);
        commandMap = loadCommands();
    }

    public Map<String, Command> loadCommands()
    {
        Map<String, Command> commands = new LinkedHashMap<>();
        try (ScanResult result = classGraph.scan())
        {
            for (ClassInfo cls : result.getAllClasses())
            {
                Constructor<?>[] constructors = cls.loadClass().getDeclaredConstructors();
                if (constructors.length == 0)
                {
                    radio.getLogger().warn("No valid constructors found for Command class (" + cls.getSimpleName() + ")!");
                    continue;
                }
                if (constructors[0].getParameterCount() > 0)
                {
                    continue;
                }
                Object instance = constructors[0].newInstance();
                if (!(instance instanceof Command))
                {
                    radio.getLogger().warn("Non Command class (" + cls.getSimpleName() + ") found in commands package!");
                    continue;
                }
                Command cmd = (Command) instance;
                commands.put(cmd.getName(), cmd);
                for (String alias : cmd.getAliases()) commands.put(alias, cmd);
            }
        }
        catch (Exception exception)
        {
            radio.getLogger().error("A command exception occurred", exception);
            System.exit(1);
        }

        return commands;
    }

    public Map<String, Command> getCommandMap()
    {
        return commandMap;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        handleEvent(event);
    }

    public void handleEvent(MessageReceivedEvent event)
    {
        if (event.getAuthor().isBot() || event.isWebhookMessage())
        {
            return;
        }

        Message referencedMessage = event.getMessage().getReferencedMessage();

        if (referencedMessage != null && referencedMessage.getAuthor().equals(radio.getSelfUser()))
        {
            return;
        }

        if (event.isFromGuild())
        {
            if (!event.getTextChannel().canTalk())
            {
                return;
            }

            handleGuild(event);
        }
        else
        {
            handleDM(event);
        }
    }

    private void handleDM(MessageReceivedEvent event)
    {
        String prefix;
        String messageContent = event.getMessage().getContentRaw();

        if (isBotMention(event))
        {
            prefix = messageContent.substring(0, messageContent.indexOf(">"));
        }
        else
        {
            prefix = Constants.DEFAULT_BOT_PREFIX;
        }

        runCommand(prefix, messageContent, event);
    }


    private void handleGuild(MessageReceivedEvent event)
    {
        String prefix = GuildSettingsCache.getCache(event.getGuild().getIdLong(), radio).getPrefix();
        String messageContent = event.getMessage().getContentRaw();

        if (isBotMention(event))
        {
            prefix = messageContent.substring(0, messageContent.indexOf(">") + 1);
        }

        runCommand(prefix, messageContent, event);
    }

    private void runCommand(String prefix, String content, MessageReceivedEvent event)
    {
        if (!content.startsWith(prefix))
        {
            return;
        }

        content = content.substring(prefix.length()); //Trim the prefix

        List<String> args = Arrays
                .stream(content.split("\\s+"))
                .filter(arg -> !arg.isBlank())
                .collect(Collectors.toList());

        if (args.isEmpty()) //No command was supplied, abort
        {
            return;
        }

        String command = args.get(0);
        if (command.isBlank() || command.startsWith(prefix)) //Empty string passed or double prefix supplied (eg ..)
        {
            return;
        }

        Command cmd = commandMap.get(command);

        if (cmd == null)
        {
            EmbedUtils.sendError(event.getChannel(), "Command `" + command + "` was not found.\n " +
                    "See " + prefix + "help for help.");
            return;
        }

        args.remove(0); //Remove the command from the arguments
        CommandEvent commandEvent = new CommandEvent(event, radio, cmd, args);

        if (!cmd.hasChildren())
        {
            cmd.process(commandEvent);
            return;
        }

        if (args.isEmpty())
        {
            cmd.process(commandEvent);
            return;
        }

        cmd.getChildren()
                .stream()
                .filter(child -> child.getAliases().stream().anyMatch(s -> s.equalsIgnoreCase(args.get(0))))
                .findFirst()
                .ifPresentOrElse(
                        child -> child.process(new CommandEvent(event, radio, child, args.subList(1, args.size()))),
                        () -> cmd.process(commandEvent)); //Run any relevant child commands, or the main command if non are found
    }

    private boolean isBotMention(MessageReceivedEvent event)
    {
        String content = event.getMessage().getContentRaw();
        long id = event.getJDA().getSelfUser().getIdLong();
        return content.startsWith("<@" + id + ">") || content.startsWith("<@!" + id + ">");
    }
}
