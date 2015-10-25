package net.digitalid.service.core.property.indexed;

import javax.annotation.Nonnull;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.freezable.FreezableMap;
import net.digitalid.utility.collections.readonly.ReadOnlyCollection;
import net.digitalid.utility.collections.readonly.ReadOnlyMap;

/**
 * Description.
 * 
 * <em>Important:</em> Make sure that {@code F} is a subtype of {@code R}!
 * Unfortunately, this cannot be enforced with the limited Java generics.
 * 
 * [used for the hosts in the Server class and modules in the Service class]
 */
public class IndexedProperty<K, V, R extends ReadOnlyMap<K, V>, F extends FreezableMap<K, V>> implements ReadOnlyIndexedProperty<K, V, R> {
    
    public IndexedProperty(@Nonnull F map) {
        this.map = map;
    }
    
    private final @Nonnull F map;
    
    @Pure
    @Override
    public @Nonnull V get(@Nonnull K key) { // TODO: How to handle the situation if the key was not found? Probably return null or throw exception.
        return map.get(key);
    }
    
    @Pure
    @Override
    public @Nonnull ReadOnlyCollection<V> getAll() {
        return map.values();
    }
    
    @Pure
    @Override
    @SuppressWarnings("unchecked")
    public @Nonnull @NonFrozen R getMap() {
        return (R) map;
    }
    
    public void add(@Nonnull K key, @Nonnull V value) {
        // TODO
    }
    
    public void remove(@Nonnull K key) {
        // TODO
    }
    
}
