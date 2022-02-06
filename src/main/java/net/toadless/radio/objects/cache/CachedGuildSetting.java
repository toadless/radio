package net.toadless.radio.objects.cache;

import org.jetbrains.annotations.NotNull;

public class CachedGuildSetting implements ICacheableEntity<String, CachedGuildSetting>
{
    private final String key;
    private final String value;

    public CachedGuildSetting(String key, String value)
    {
        this.key = key;
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }

    @Override
    public @NotNull String getKey()
    {
        return key;
    }

    @Override
    public @NotNull CachedGuildSetting getData()
    {
        return this;
    }
}