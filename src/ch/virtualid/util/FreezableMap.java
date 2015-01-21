package ch.virtualid.util;

import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Freezable;
import ch.virtualid.interfaces.Immutable;
import java.util.Map;
import javax.annotation.Nonnull;

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
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface FreezableMap<K,V> extends ReadonlyMap<K,V>, Map<K,V>, Freezable {
    
    @Override
    public @Nonnull ReadonlyMap<K,V> freeze();
    
    @Pure
    @Override
    public FreezableSet<K> keySet();
    
    @Pure
    @Override
    public FreezableCollection<V> values();
    
    /**
     * <em>Important:</em> Never call {@link Map.Entry#setValue(java.lang.Object)} on the elements!
     */
    @Pure
    @Override
    public FreezableSet<Map.Entry<K,V>> entrySet();
    
}
