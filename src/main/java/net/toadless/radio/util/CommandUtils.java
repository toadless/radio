package net.toadless.radio.util;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import net.toadless.radio.Radio;
import net.toadless.radio.objects.cache.GuildSettingsCache;
import net.toadless.radio.objects.command.CommandEvent;

public class CommandUtils
{
    private CommandUtils()
    {
        // Override the default, public, constructor
    }

    public static boolean isValidCommand(String message, long guildId, Radio radio)
    {
        return message.startsWith(GuildSettingsCache.getCache(guildId, radio).getPrefix()) || message.startsWith("<@" + radio.getSelfUser().getId() + ">") || message.startsWith("<@!" + radio.getSelfUser().getId() + ">");
    }

    public static String formatTime(int time){
        return time > 9 ? String.valueOf(time) : "0" + time;
    }

    public static String formatDuration(long length)
    {
        Duration duration = Duration.ofMillis(length);
        long hours = duration.toHours();

        if (hours > 0)
        {
            return String.format("%s:%s:%s", formatTime((int) hours), formatTime(duration.toMinutesPart()), formatTime(duration.toSecondsPart()));
        }

        return String.format("%s:%s", formatTime((int) duration.toMinutes()), formatTime(duration.toSecondsPart()));
    }

    public static void interactionCheck(User user1, User user2, CommandEvent ctx, Runnable onSuccess)
    {
        List<RestAction<?>> actions = new ArrayList<>();
        actions.add(ctx.getGuild().retrieveMember(user1));
        actions.add(ctx.getGuild().retrieveMember(user2));
        RestAction.allOf(actions).queue(results ->
                {
                    Member member1 = (Member) results.get(0);
                    Member member2 = (Member) results.get(1);

                    if (member1.canInteract(member2))
                    {
                        onSuccess.run();
                    }
                    else
                    {
                        ctx.replyError("A hierarchy issue occurred when trying to execute command `" + ctx.getCommand().getName() + "`");
                    }
                }
        );
    }
}