package net.toadless.radio.modules;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import net.toadless.radio.Radio;
import net.toadless.radio.objects.Emoji;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.module.Module;
import net.toadless.radio.objects.module.Modules;
import net.toadless.radio.objects.other.Paginator;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

public class PaginationModule extends Module
{
    private final Map<Long, Paginator> paginators;

    public PaginationModule(Radio radio, Modules modules)
    {
        super(radio, modules);

        this.paginators = ExpiringMap.builder()
                .expirationPolicy(ExpirationPolicy.CREATED)
                .expiration(10, TimeUnit.MINUTES)
                .expirationListener((key, paginator) -> remove((Paginator) paginator))
                .build();
    }

    public void remove(Paginator paginator)
    {
        Guild guild = this.radio.getShardManager().getGuildById(paginator.getGuildId());
        if(guild == null)
        {
            return;
        }

        TextChannel channel = guild.getTextChannelById(paginator.getChannelId());

        if(channel == null)
        {
            return;
        }

        channel.clearReactionsById(paginator.getMessageId()).queue();
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event)
    {
        if(event.getUser().isBot())
        {
            return;
        }

        Paginator paginator = this.paginators.get(event.getMessageIdLong());
        if(paginator == null)
        {
            return;
        }

        String code = event.getReactionEmote().getAsReactionCode();
        int currentPage = paginator.getCurrentPage();
        int maxPages = paginator.getMaxPages();

        if(Emoji.ARROW_LEFT.getAsReaction().equals(code))
        {
            if(currentPage != 0)
            {
                paginator.previousPage();
                event.getChannel().editMessageEmbedsById(event.getMessageIdLong(), paginator.constructEmbed()).queue();
            }
        }
        else if(Emoji.ARROW_RIGHT.getAsReaction().equals(code))
        {
            if(currentPage != maxPages - 1)
            {
                paginator.nextPage();
                event.getChannel().editMessageEmbedsById(event.getMessageIdLong(), paginator.constructEmbed()).queue();
            }
        }
        else if(Emoji.WASTE_BASKET.getAsReaction().equals(code))
        {
            if(paginator.getAuthorId() == event.getUserIdLong())
            {
                event.getChannel().deleteMessageById(event.getMessageIdLong()).queue();
                this.paginators.remove(event.getMessageIdLong());
                return;
            }
        }
        event.getReaction().removeReaction(event.getUser()).queue();
    }

    public void create(MessageChannel channel, long authorId, int maxPages, BiFunction<Integer, EmbedBuilder, EmbedBuilder> embedFunction)
    {
        var embedBuilder = embedFunction.apply(0, new EmbedBuilder().setFooter("Page: 1/" + maxPages)).build();
        create(maxPages, embedFunction, embedBuilder, channel, authorId);
    }

    public void create(int maxPages, BiFunction<Integer, EmbedBuilder, EmbedBuilder> embedFunction, MessageEmbed embedBuilder, MessageChannel channel, long userId)
    {
        channel.sendMessageEmbeds(embedBuilder).queue(message ->
        {
            var paginator = new Paginator(message, userId, maxPages, embedFunction);
            this.paginators.put(paginator.getMessageId(), paginator);

            if(channel instanceof GuildChannel && !((GuildChannel) channel).getGuild().getSelfMember().hasPermission(Permission.MESSAGE_HISTORY, Permission.MESSAGE_ADD_REACTION))
            {
                return;
            }

            if(maxPages > 1)
            {
                message.addReaction(Emoji.ARROW_LEFT.getAsReaction()).queue();
                message.addReaction(Emoji.ARROW_RIGHT.getAsReaction()).queue();
            }
            message.addReaction(Emoji.WASTE_BASKET.getAsReaction()).queue();
        });
    }

    public void create(CommandEvent event, int maxPages, BiFunction<Integer, EmbedBuilder, EmbedBuilder> embedFunction)
    {
        MessageEmbed embedBuilder = embedFunction.apply(0, new EmbedBuilder().setFooter("Page: 1/" + maxPages)).build();
        create(maxPages, embedFunction, embedBuilder, event.getChannel(), event.getMember().getIdLong());
    }
}
