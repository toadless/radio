package net.toadless.radio.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.exception.*;
import net.toadless.radio.objects.music.GuildMusicManager;

public class CommandChecks
{
    private CommandChecks()
    {
        //Overrides the default, public, constructor
    }

    public static boolean sharesVoice(CommandEvent event, Consumer<CommandException> callback)
    {
        GuildVoiceState state = event.getMember().getVoiceState();
        GuildVoiceState selfState = event.getSelfMember().getVoiceState();

        if (state == null || selfState == null)
        {
            callback.accept(new CommandResultException("Something went wrong when finding your VC."));
            return true;
        }

        else if (state.getChannel() == null)
        {
            callback.accept(new CommandResultException("You are not in a voice channel."));
            return true;
        }
        else if (selfState.inVoiceChannel() && !state.getChannel().getMembers().contains(event.getSelfMember()))
        {
            callback.accept(new CommandResultException("You are not in a voice channel with me."));
            return true;
        }
        else if (!selfState.inVoiceChannel() && !event.getSelfMember().hasPermission(state.getChannel(), Permission.VIEW_CHANNEL, Permission.VOICE_SPEAK))
        {
            callback.accept(new CommandException("I cannot join / speak in your channel."));
            return true;
        }
        return false;
    }

    public static boolean inVoice(CommandEvent event, Consumer<CommandException> callback)
    {
        GuildVoiceState state = event.getMember().getVoiceState();
        GuildVoiceState selfState = event.getSelfMember().getVoiceState();

        if (state == null || selfState == null)
        {
            callback.accept(new CommandResultException("Something went wrong when finding your VC."));
            return true;
        }
        else if (!selfState.inVoiceChannel())
        {
            callback.accept(new CommandResultException("I am not in a voice channel."));
            return true;
        }
        return false;
    }

    public static boolean canSee(MessageChannel channel, Member selfMember, String name, Consumer<CommandException> callback)
    {
        if (!selfMember.hasPermission((GuildChannel) channel, Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS))
        {
            callback.accept(new CommandResultException("I cannot access " + name));
            return true;
        }
        return false;
    }


    public static boolean channelConfigured(MessageChannel channel, String name, Consumer<CommandException> callback)
    {
        if (channel == null)
        {
            callback.accept(new MissingConfigurationException(name));
            return true;
        }
        return false;
    }

    public static boolean roleConfigured(Role role, String name, Consumer<CommandException> callback)
    {
        if (role == null)
        {
            callback.accept(new MissingConfigurationException(name));
            return true;
        }
        return false;
    }

    public static boolean userConfigured(User user, String name, Consumer<CommandException> callback)
    {
        if (user == null)
        {
            callback.accept(new MissingConfigurationException(name));
            return true;
        }
        return false;
    }

    public static boolean isURL(String url, CommandEvent ctx, Consumer<CommandException> callback)
    {
        try
        {
            URL obj = new URL(url);
            obj.toURI();
            return false;
        }
        catch (Exception exception)
        {
            callback.accept(new CommandSyntaxException(ctx));
            return true;
        }
    }

    public static boolean argsEmpty(CommandEvent ctx, Consumer<CommandException> callback)
    {
        if (ctx.getArgs().isEmpty())
        {
            callback.accept(new CommandSyntaxException(ctx));
            return true;
        }
        return false;
    }

    public static boolean argsSizeExceeds(CommandEvent ctx, int size, Consumer<CommandException> callback)
    {
        if (ctx.getArgs().size() > size)
        {
            callback.accept(new CommandSyntaxException(ctx));
            return true;
        }
        return false;
    }

    public static boolean argsSizeSubceeds(CommandEvent ctx, int size, Consumer<CommandException> callback)
    {
        if (ctx.getArgs().size() < size)
        {
            callback.accept(new CommandSyntaxException(ctx));
            return true;
        }
        return false;
    }

    public static boolean argsSizeSubceeds(List<String> args, CommandEvent ctx, int size, Consumer<CommandException> callback)
    {
        if (args.size() < size)
        {
            callback.accept(new CommandSyntaxException(ctx));
            return true;
        }
        return false;
    }

    public static boolean argsSizeMatches(CommandEvent ctx, int size, Consumer<CommandException> callback)
    {
        if (ctx.getArgs().size() != size)
        {
            callback.accept(new CommandSyntaxException(ctx));
            return true;
        }
        return false;
    }

    public static boolean argsEmbedCompatible(CommandEvent ctx, Consumer<CommandException> callback)
    {
        List<Character> chars = new ArrayList<>();
        ctx.getArgs().stream().map(arg -> arg.split("")).forEach(
                words ->
                {
                    for (String word : words)
                    {
                        for (char character : word.toCharArray())
                        {
                            chars.add(character);
                        }
                    }
                });
        if (chars.size() > MessageEmbed.TEXT_MAX_LENGTH)
        {
            callback.accept(new CommandInputException("Input too large."));
            return true;
        }
        return false;
    }

    public static boolean boundToChannel(GuildMusicManager handler, MessageChannel channel, Consumer<CommandException> callback)
    {
        if (handler.getChannel() != null && handler.getChannel().getIdLong() != channel.getIdLong())
        {
            callback.accept(new CommandResultException("I'm bound to " + StringUtils.getChannelAsMention(handler.getChannel().getIdLong()) + " for this session."));
            return true;
        }
        return false;
    }

    public static boolean isURL(String url)
    {
        try
        {
            URL obj = new URL(url);
            obj.toURI();
            return true;
        }
        catch (Exception exception)
        {
            return false;
        }
    }
}