package net.digitalid.core.collections;

import java.util.Map;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.NonFrozenRecipient;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.interfaces.Freezable;
import net.digitalid.core.interfaces.Immutable;

/**
 * This interface models a {@link Map map} that can be {@link Freezable frozen}.
 * As a consequence, all modifying methods may fail with an {@link AssertionError}.
 * <p>
 * Please note that {@link Map#entrySet()} is not supported because every entry would need
 * to be replaced with a freezable entry and such a set can no longer be backed.
 * <p>
 * <em>Important:</em> Only use immutable types for the keys and freezable or immutable types for the values!
 * (The types are not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface FreezableMap<K,V> extends ReadOnlyMap<K,V>, Map<K,V>, Freezable {
    
    @Override
    public @Nonnull ReadOnlyMap<K,V> freeze();
    
    @Pure
    @Override
    public FreezableSet<K> keySet();
    
    @Pure
    @Override
    public FreezableCollection<V> values();
    
    /**
     * <em>Important:</em> Never call {@code Map.Entry#setValue(java.lang.Object)} on the elements!
     */
    @Pure
    @Override
    public FreezableSet<Map.Entry<K,V>> entrySet();
    
    /**
     * Associates the given value with the given key, if the
     * given key is not already associated with a value or null.
     * 
     * @param key the key to be associated with the given value.
     * @param value the value to be associated with the given key.
     * 
     * @return the value that is now associated with the given key.
     */
    @NonFrozenRecipient
    public @Nonnull V putIfAbsentOrNullElseReturnPresent(@Nonnull K key, @Nonnull V value);
    
}
