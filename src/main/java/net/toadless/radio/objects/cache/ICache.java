package net.toadless.radio.objects.cache;

import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public interface ICache<K, V extends ICacheableEntity<K, V>> //K-KEY TO ENTITY V-VALUE TO ENTITY
{
    void put(V value);

    void put(Collection<V> values);

    @NotNull V get(K key);

    void update(V oldValue, V newValue);

    void update(K oldValue, V newValue);

    boolean isCached(K key);

    void remove(K key);

    void remove(V key);

    void remove(Collection<V> values);
}