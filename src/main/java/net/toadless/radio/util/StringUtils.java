package net.toadless.radio.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.toadless.radio.objects.Emoji;

public class StringUtils
{
    private StringUtils()
    {
        //Overrides the default, public, constructor
    }

    public static String getEmoteAsMention(String emote)
    {
        try
        {
            return "<:emote:" + Long.parseLong(emote) + ">";
        }
        catch (Exception exception)
        {
            return emote;
        }
    }

    public static String markdownSanitize(String text)
    {
        return MarkdownSanitizer.sanitize(text, MarkdownSanitizer.SanitizationStrategy.REMOVE);
    }

    public static String URLSanitize(String text)
    {
        return text.replaceAll("(\\?|\\&)([^=]+)\\=([^&]+)", "");
    }

    public static String plurify(String prefix, int number)
    {
        return number == 1 ? prefix : prefix + "s";
    }

    public static String parseToEmote(int number)
    {
        return switch (number)
                {
                    case 1 -> Emoji.ONE.getAsChat();
                    case 2 -> Emoji.TWO.getAsChat();
                    case 3 -> Emoji.THREE.getAsChat();
                    case 4 -> Emoji.FOUR.getAsChat();
                    case 5 -> Emoji.FIVE.getAsChat();
                    case 6 -> Emoji.SIX.getAsChat();
                    case 7 -> Emoji.SEVEN.getAsChat();
                    case 8 -> Emoji.EIGHT.getAsChat();
                    case 9 -> Emoji.NINE.getAsChat();
                    case 0 -> Emoji.ZERO.getAsChat();
                    default -> "";
                };
    }

    public static String getRoleAsMention(long roleId)
    {
        return "<@&" + roleId + ">";
    }

    public static String getChannelAsMention(String channelID)
    {
        return "<#" + channelID + ">";
    }

    public static String getChannelAsMention(long channelID)
    {
        return "<#" + channelID + ">";
    }

    public static String getTimestamp()
    {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now());
    }

    public static String getMessageLink(long messageId, long channelId, long guildId)
    {
        return "https://discord.com/channels/" + guildId + "/" + channelId + "/" + messageId;
    }

    public static String parseDateTime(LocalDateTime time)
    {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(time);
    }

    public static String parseDateTime(OffsetDateTime time)
    {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(time);
    }

    public static String getUserAsMention(String userId)
    {
        return "<@!" + userId + ">";
    }

    public static String getUserAsMention(long userId)
    {
        return "<@!" + userId + ">";
    }

    public static String parseDuration(Duration duration)
    {
        return duration.toHoursPart() + "h : " + duration.toMinutesPart() + "m : " + duration.toSecondsPart() + "s";
    }

    public static void sendPartialMessages(String input, MessageChannel channel)
    {
        while (true)
        {
            if (input.length() >= Message.MAX_CONTENT_LENGTH)
            {
                channel.sendMessage(input.substring(0, Message.MAX_CONTENT_LENGTH))
                        .allowedMentions(Collections.emptyList())
                        .queue();

                input = input.substring(Message.MAX_CONTENT_LENGTH);
            }
            else
            {
                channel.sendMessage(input)
                        .allowedMentions(Collections.emptyList())
                        .queue();
                break;
            }
        }
    }

    public static String prettyPrintJSON(String json)
    {
        StringBuilder jsonBuilder = new StringBuilder();
        int indentLevel = 0;
        boolean inQuote = false;

        for (char jsonChar : json.toCharArray())
        {
            switch (jsonChar)
            {
                case '"':
                    inQuote = !inQuote;
                    jsonBuilder.append(jsonChar);
                    break;

                case ' ':
                    if (inQuote)
                    {
                        jsonBuilder.append(jsonChar);
                    }
                    break;

                case '{':
                case '[':
                    jsonBuilder.append(jsonChar);
                    indentLevel++;
                    indentSection(indentLevel, jsonBuilder);
                    break;

                case '}':
                case ']':
                    indentLevel--;
                    indentSection(indentLevel, jsonBuilder);
                    jsonBuilder.append(jsonChar);
                    break;

                case ',':
                    jsonBuilder.append(jsonChar);
                    if (!inQuote)
                    {
                        indentSection(indentLevel, jsonBuilder);
                    }
                    break;

                default:
                    jsonBuilder.append(jsonChar);
            }
        }
        return jsonBuilder.toString();
    }

    private static void indentSection(int indentLevel, StringBuilder stringBuilder)
    {
        stringBuilder.append("\n");
        stringBuilder.append("    ".repeat(Math.max(0, indentLevel)));
    }
}