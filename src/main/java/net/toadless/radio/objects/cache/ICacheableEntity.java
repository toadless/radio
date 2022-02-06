package net.toadless.radio.objects.cache;

import org.jetbrains.annotations.NotNull;

public interface ICacheableEntity<K, V>
{
    @NotNull K getKey();

    @NotNull V getData();
}