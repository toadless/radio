package net.toadless.radio.util;

import java.awt.*;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.toadless.radio.objects.Emoji;
import net.toadless.radio.objects.command.Command;
import net.toadless.radio.objects.command.CommandEvent;

public class EmbedUtils
{
    public static final int REACTION_LIMIT = 20;

    private EmbedUtils()
    {
        //Overrides the default, public, constructor
    }

    public static void sendError(MessageChannel channel, String errorText)
    {
        sendDeletingEmbed(channel, new EmbedBuilder()
                .setDescription(Emoji.FAILURE.getAsChat() + errorText)
                .setColor(Color.RED)
                .setTimestamp(Instant.now()));
    }

    public static void sendSyntaxError(CommandEvent ctx)
    {
        Command cmd = ctx.getCommand();
        ctx.addErrorReaction();
        if (ctx.isChild())
        {
            sendDeletingEmbed(ctx.getChannel(), new EmbedBuilder()
                    .setDescription(Emoji.FAILURE.getAsChat() + "You entered something wrong:\n`" + ctx.getPrefix() + cmd.getParent().getAliases().get(0) + " " + cmd.getName() + " " + cmd.getSyntax() + "`")
                    .setTimestamp(Instant.now())
                    .setColor(Color.RED), 30000);
        }
        else
        {
            sendDeletingEmbed(ctx.getChannel(), new EmbedBuilder()
                    .setDescription(Emoji.FAILURE.getAsChat() + "You entered something wrong:\n`" + ctx.getPrefix() + cmd.getAliases().get(0) + " " + cmd.getSyntax() + "`")
                    .setTimestamp(Instant.now())
                    .setColor(Color.RED), 20000);
        }
    }

    public static void sendMemberPermissionError(CommandEvent ctx)
    {
        ctx.addErrorReaction();
        Command cmd = ctx.getCommand();
        StringBuilder perms = new StringBuilder();

        cmd.getMemberRequiredPermissions().forEach(perm -> perms.append("*").append(perm.getName()).append("*").append("\n"));
        sendDeletingEmbed(ctx.getChannel(), new EmbedBuilder()
                .setDescription(Emoji.FAILURE.getAsChat() +
                        " You do not have the following required permissions:"
                        + perms.toString())
                .setColor(Color.RED)
                .setTimestamp(Instant.now()));
    }


    public static void sendSelfPermissionError(CommandEvent ctx)
    {
        ctx.addErrorReaction();
        Command cmd = ctx.getCommand();
        StringBuilder perms = new StringBuilder();

        cmd.getSelfRequiredPermissions().forEach(perm -> perms.append("*").append(perm.getName()).append("*").append("\n"));
        sendDeletingEmbed(ctx.getChannel(), new EmbedBuilder()
                .setDescription(Emoji.FAILURE.getAsChat() +
                        " I do not have the following required permission:`"
                        + perms.toString())
                .setColor(Color.RED)
                .setTimestamp(Instant.now()));
    }

    public static void sendSuccess(MessageChannel channel, String successText)
    {
        sendDeletingEmbed(channel, new EmbedBuilder()
                .setDescription(Emoji.SUCCESS.getAsChat() + successText)
                .setColor(Color.GREEN)
                .setTimestamp(Instant.now()));
    }

    public static void sendDisabledError(CommandEvent ctx)
    {
        ctx.addErrorReaction();
        sendDeletingEmbed(ctx.getChannel(), new EmbedBuilder()
                .setDescription(Emoji.FAILURE.getAsChat() + " `" + ctx.getCommand().getName() + "` is currently disabled!")
                .setColor(Color.RED)
                .setTimestamp(Instant.now()));
    }

    public static void sendDeletingEmbed(MessageChannel channel, EmbedBuilder embed, long delay)
    {
        channel.sendMessage(embed.build()).queue(message -> message.delete().queueAfter(delay, TimeUnit.MILLISECONDS, null, error ->
        { }));
    }

    public static void sendDeletingEmbed(MessageChannel channel, EmbedBuilder embed)
    {
        sendDeletingEmbed(channel, embed, 10000);
    }

    public static void sendReplacedEmbed(Message message, EmbedBuilder newEmbed)
    {
        message.editMessage(newEmbed.build()).queue();
    }
}