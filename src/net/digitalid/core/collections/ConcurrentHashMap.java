package net.digitalid.core.collections;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.NonNegative;
import net.digitalid.core.annotations.Positive;
import net.digitalid.core.annotations.Pure;

/**
 * Extends Java's {@link java.util.concurrent.ConcurrentHashMap ConcurrentHashMap} implementation with more useful methods.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public class ConcurrentHashMap<K, V> extends java.util.concurrent.ConcurrentHashMap<K, V> implements ConcurrentMap<K, V> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructors –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * @see java.util.concurrent.ConcurrentHashMap#ConcurrentHashMap(int, float, int)
     */
    protected ConcurrentHashMap(@NonNegative int initialCapacity, @Positive float loadFactor, @Positive int concurrencyLevel) {
        super(initialCapacity, loadFactor, concurrencyLevel);
    }
    
    /**
     * @see java.util.concurrent.ConcurrentHashMap#ConcurrentHashMap(int, float, int)
     */
    @Pure
    public static @Capturable @Nonnull <K, V> ConcurrentHashMap<K, V> get(@NonNegative int initialCapacity, @Positive float loadFactor, @Positive int concurrencyLevel) {
        return new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
    }
    
    /**
     * @see java.util.concurrent.ConcurrentHashMap#ConcurrentHashMap(int, float)
     */
    @Pure
    public static @Capturable @Nonnull <K, V> ConcurrentHashMap<K, V> get(@NonNegative int initialCapacity, @Positive float loadFactor) {
        return get(initialCapacity, loadFactor, 16);
    }
    
    /**
     * @see java.util.concurrent.ConcurrentHashMap#ConcurrentHashMap(int)
     */
    @Pure
    public static @Capturable @Nonnull <K, V> ConcurrentHashMap<K, V> get(@NonNegative int initialCapacity) {
        return get(initialCapacity, 0.75f);
    }
    
    /**
     * @see java.util.concurrent.ConcurrentHashMap#ConcurrentHashMap()
     */
    @Pure
    public static @Capturable @Nonnull <K, V> ConcurrentHashMap<K, V> get() {
        return get(16);
    }
    
    /**
     * @see java.util.concurrent.ConcurrentHashMap#ConcurrentHashMap(java.util.Map)
     */
    protected ConcurrentHashMap(@Nonnull Map<? extends K, ? extends V> map) {
        super(map);
    }
    
    /**
     * @see java.util.concurrent.ConcurrentHashMap#ConcurrentHashMap(java.util.Map)
     */
    @Pure
    public static @Capturable @Nonnull <K, V> ConcurrentHashMap<K, V> getNonNullable(@Nonnull Map<? extends K, ? extends V> map) {
        return new ConcurrentHashMap<>(map);
    }
    
    /**
     * @see java.util.concurrent.ConcurrentHashMap#ConcurrentHashMap(java.util.Map)
     */
    @Pure
    public static @Capturable @Nullable <K, V> ConcurrentHashMap<K, V> getNullable(@Nullable Map<? extends K, ? extends V> map) {
        return map == null ? null : getNonNullable(map);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– ConcurrentMap –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Override
    public @Nonnull V putIfAbsentElseReturnPresent(@Nonnull K key, @Nonnull V value) {
        @Nullable V previous = putIfAbsent(key, value);
        if (previous == null) return value;
        else return previous;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Cloneable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Capturable @Nonnull ConcurrentHashMap<K, V> clone() {
        return new ConcurrentHashMap<>(this);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Object –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return IterableConverter.toString(entrySet(), new ElementConverter<Entry<K, V>>() { @Pure @Override public String toString(@Nullable Entry<K, V> entry) { return entry == null ? "null" : entry.getKey() + ": " + entry.getValue(); } }, Brackets.CURLY);
    }
    
}
