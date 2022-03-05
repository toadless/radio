package net.toadless.radio.objects;

public enum Emoji
{
    THUMB_UP(":thumbsup:", "\uD83D\uDC4D"),
    THUMB_DOWN(":thumbsdown:", "\uD83D\uDC4E"),

    GREEN_TICK(":white_check_mark:", "\u2705"),
    GREEN_CROSS(":negative_squared_cross_mark:", "\u274E"),

    ARROW_LEFT(":arrow_left:", "\u2B05\uFE0F"),
    ARROW_RIGHT(":arrow_right:", "\u27A1\uFE0F"),

    STOP_SIGN(":octagonal_sign:", "\uD83D\uDED1"),

    WASTE_BASKET(":wastebasket:", "\uD83D\uDDD1\uFE0F"),

    SUCCESS(":white_check_mark:"),
    FAILURE(":negative_squared_cross_mark:"),

    VOLUME_DOWN(":sound:", "\uD83D\uDD09"),
    VOLUME_UP(":loud_sound:", "\uD83D\uDD0A"),
    PLAY_PAUSE(":play_pause:", "\u23EF"),
    SHUFFLE(":twisted_rightwards_arrows:", "\uD83D\uDD00"),
    REPEAT(":repeat:", "\uD83D\uDD01"),
    CROSS(":x:", "\u274C"),

    ZERO(":zero:", "\u0030\uFE0F"),
    ONE(":one:", "\u0031\uFE0F"),
    TWO(":two:", "\u0032\uFE0F"),
    THREE(":three:", "\u0033\uFE0F"),
    FOUR(":four:", "\u0034\uFE0F"),
    FIVE(":five:", "\u0035\uFE0F"),
    SIX(":six:", "\u0036\uFE0F"),
    SEVEN(":seven:", "\u0037\uFE0F"),
    EIGHT(":eight:", "\u0038\uFE0F"),
    NINE(":nine:", "\u0039\uFE0F");

    private final String unicode;
    private final String emote;
    private final boolean isAnimated;

    Emoji(String emote, String unicode)
    {
        this.emote = emote;
        this.unicode = unicode;
        this.isAnimated = false;
    }

    Emoji(String emote)
    {
        this.emote = emote;
        this.unicode = "";
        this.isAnimated = false;
    }

    public String getUnicode()
    {
        return unicode;
    }

    public String getAsReaction()
    {
        if (this.unicode.isBlank())
        {
            return this.emote;
        }
        return this.unicode;
    }

    public String getAsChat()
    {
        if (this.unicode.isBlank())
        {
            if (this.isAnimated)
            {
                return "<a:emote:" + this.emote + ">";
            }
            return this.emote + " ";
        }
        return this.emote;
    }
}