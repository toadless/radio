package net.toadless.radio.objects.bot;

public enum ConfigOption
{
    TOKEN("token", "token"),
    PRIVILEGEDUSERS("privilegedusers", "0000000000000, 0000000000000"),

    DBUSERNAME("dbusername", "username"),
    DBPASSWORD("dbpassword", "password"),
    DBDRIVER("dbdriver", "org.postgresql.Driver"),
    DBURL("dburl", "jdbc:type://host:port/database");

    private final String key;
    private final String defaultValue;

    ConfigOption(String key, String defaultValue)
    {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public String getKey()
    {
        return key;
    }
}