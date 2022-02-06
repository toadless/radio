package net.toadless.radio.objects.command;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.toadless.radio.Constants;
import net.toadless.radio.Radio;
import net.toadless.radio.objects.Emoji;
import net.toadless.radio.objects.bot.ConfigOption;
import net.toadless.radio.objects.cache.GuildSettingsCache;
import net.toadless.radio.util.EmbedUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandEvent
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandEvent.class);
    private final MessageReceivedEvent event;
    private final Radio radio;
    private final Command command;
    private final List<String> args;

    public CommandEvent(@NotNull MessageReceivedEvent event, @NotNull Radio radio, @NotNull Command command, @NotNull List<String> args)
    {
        this.event = event;
        this.radio = radio;
        this.command = command;
        this.args = args;
    }

    public @NotNull List<String> getArgs()
    {
        return args;
    }

    public @NotNull String getPrefix()
    {
        if (!isFromGuild())
        {
            return Constants.DEFAULT_BOT_PREFIX;
        }
        else
        {
            return GuildSettingsCache.getCache(getGuildIdLong(), radio).getPrefix();
        }
    }

    public @NotNull Member getSelfMember()
    {
        return getGuild().getSelfMember();
    }

    public void addErrorReaction()
    {
        getMessage().addReaction(Emoji.FAILURE.getAsReaction()).queue(
                success -> getMessage().removeReaction(Emoji.FAILURE.getAsReaction()).queueAfter(10, TimeUnit.SECONDS, null,
                        error -> LOGGER.debug("A command exception occurred", error)),
                error -> LOGGER.debug("A command exception occurred", error));
    }

    public void addSuccessReaction()
    {
        getMessage().addReaction(Emoji.SUCCESS.getAsReaction()).queue(
                success -> getMessage().removeReaction(Emoji.SUCCESS.getAsReaction()).queueAfter(10, TimeUnit.SECONDS, null,
                        error -> LOGGER.debug("A command exception occurred", error)),
                error -> LOGGER.debug("A command exception occurred", error));
    }

    public @NotNull Radio getRadio()
    {
        return radio;
    }

    public @NotNull Command getCommand()
    {
        return command;
    }

    public @NotNull MessageChannel getChannel()
    {
        return event.getChannel();
    }

    public @NotNull Message getMessage()
    {
        return event.getMessage();
    }

    public @NotNull Guild getGuild()
    {
        if (event.isFromGuild())
        {
            return event.getGuild();
        }
        throw new IllegalStateException("Cannot get the guild of a private channel.");
    }

    public long getGuildIdLong()
    {
        return getGuild().getIdLong();
    }

    public @NotNull User getAuthor()
    {
        return event.getAuthor();
    }

    public @NotNull JDA getJDA()
    {
        return event.getJDA();
    }

    public boolean isChild()
    {
        return command.getParent() != null;
    }

    public @NotNull ChannelType getChannelType()
    {
        return event.getChannelType();
    }

    public @NotNull MessageReceivedEvent getEvent()
    {
        return event;
    }

    public @NotNull Member getMember()
    {
        return Objects.requireNonNull(event.getMember());
    }

    public void replyError(String errorText)
    {
        addErrorReaction();
        EmbedUtils.sendError(getChannel(), errorText);
    }

    public @NotNull TextChannel getTextChannel()
    {
        if (!isFromGuild())
        {
            throw new IllegalStateException("Event did not occur in a text channel.");
        }
        return event.getTextChannel();
    }

    public void replySuccess(String successText)
    {
        addSuccessReaction();
        EmbedUtils.sendSuccess(getChannel(), successText);
    }

    public boolean isDeveloper()
    {
        return List.of(radio.getConfiguration().getString(ConfigOption.PRIVILEGEDUSERS).split(",")).contains(getAuthor().getId());
    }

    public boolean isFromGuild()
    {
        return event.isFromGuild();
    }

    public boolean memberPermissionCheck(List<Permission> permissions)
    {
        return (event.getMember() != null && event.getMember().hasPermission((GuildChannel) event.getChannel(), permissions));
    }

    public boolean memberPermissionCheck(Permission... permissions)
    {
        return (event.getMember() != null && event.getMember().hasPermission((GuildChannel) event.getChannel(), permissions));
    }

    public boolean selfPermissionCheck(Permission... permissions)
    {
        return event.getGuild().getSelfMember().hasPermission(permissions);
    }

    public void sendMessage(EmbedBuilder embed)
    {
        addSuccessReaction();
        getChannel().sendMessage(embed.setColor(Constants.EMBED_COLOUR).setTimestamp(Instant.now()).build()).queue();
    }

    public void sendDeletingMessage(EmbedBuilder embed)
    {
        addSuccessReaction();
        EmbedUtils.sendDeletingEmbed(getChannel(), embed.setColor(Constants.EMBED_COLOUR).setTimestamp(Instant.now()));
    }


    public boolean selfPermissionCheck(List<Permission> permissions)
    {
        return event.getGuild().getSelfMember().hasPermission(permissions);
    }
}