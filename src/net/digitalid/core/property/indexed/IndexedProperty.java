package net.digitalid.core.property.indexed;

import javax.annotation.Nonnull;
import net.digitalid.collections.annotations.freezable.NonFrozen;
import net.digitalid.annotations.state.Pure;
import net.digitalid.collections.freezable.FreezableMap;
import net.digitalid.collections.readonly.ReadOnlyCollection;
import net.digitalid.collections.readonly.ReadOnlyMap;

/**
 * Description.
 * 
 * <em>Important:</em> Make sure that {@code F} is a subtype of {@code R}!
 * Unfortunately, this cannot be enforced with the limited Java generics.
 * 
 * [used for the hosts in the Server class and modules in the Service class]
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.0
 */
public class IndexedProperty<K, V, R extends ReadOnlyMap<K, V>, F extends FreezableMap<K, V>> implements ReadOnlyIndexedProperty<K, V, R> {
    
    public IndexedProperty(@Nonnull F map) {
        this.map = map;
    }
    
    private final @Nonnull F map;
    
    @Pure
    @Override
    public @Nonnull V get(@Nonnull K key) {
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
