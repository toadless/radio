package net.toadless.radio.objects.music;

public enum SearchEngine
{
    YOUTUBE("youtube", "yt"),
    SOUNDCLOUD("soundcloud", "sc"),
    SPOTIFY("spotify", "sp");

    private final String name;
    private final String shortName;

    SearchEngine(String name, String shortName)
    {
        this.name = name;
        this.shortName = shortName;
    }

    public String getName()
    {
        return name;
    }

    public String getShortName()
    {
        return shortName;
    }
}
