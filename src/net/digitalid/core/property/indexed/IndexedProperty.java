package net.digitalid.core.property.indexed;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.FreezableMap;
import net.digitalid.core.collections.ReadOnlyCollection;
import net.digitalid.core.collections.ReadOnlyMap;

/**
 * Description.
 * 
 * <em>Important:</em> Make sure that {@code F} is a subtype of {@code R}!
 * Unfortunately, this cannot be enforced with the limited Java generics.
 * 
 * [used for the hosts in the Server class and modules in the Service class]
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
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
