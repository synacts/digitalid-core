package ch.virtualid.collections;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Freezable;
import ch.virtualid.interfaces.Immutable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class extends the {@link HashMap} and makes it {@link Freezable}.
 * Be careful when treating instances of this class as normal {@link Map maps} because all modifying methods may fail with an {@link AssertionError}.
 * <p>
 * <em>Important:</em> Only use {@link Immutable immutable} or {@link Freezable frozen} objects as keys!
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public class FreezableHashMap<K,V> extends HashMap<K,V> implements FreezableMap<K,V> {
    
    /**
     * Stores whether this object is frozen.
     */
    private boolean frozen = false;
    
    @Pure
    @Override
    public final boolean isFrozen() {
        return frozen;
    }
    
    @Pure
    @Override
    public final boolean isNotFrozen() {
        return !frozen;
    }
    
    @Override
    public @Nonnull ReadonlyMap<K,V> freeze() {
        if (!frozen) {
            frozen = true;
            // Assuming that the keys are already immutable.
            for (V value : values()) {
                if (value instanceof Freezable) {
                    ((Freezable) value).freeze();
                } else {
                    break;
                }
            }
        }
        return this;
    }
    
    
    /**
     * @see HashMap#HashMap()
     */
    public FreezableHashMap() {
        super();
    }
    
    /**
     * @see HashMap#HashMap(int)
     */
    public FreezableHashMap(int initialCapacity) {
        super(initialCapacity);
    }
    
    /**
     * @see HashMap#HashMap(int, float)
     */
    public FreezableHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }
    
    /**
     * @see HashMap#HashMap(java.util.Map)
     */
    public FreezableHashMap(@Nonnull Map<? extends K, ? extends V> map) {
        super(map);
    }
    
    
    @Pure
    @Override
    public boolean isNotEmpty() {
        return !isEmpty();
    }
    
    @Pure
    @Override
    public @Nonnull FreezableSet<K> keySet() {
        return new BackedFreezableSet<K>(this, super.keySet());
    }
    
    @Pure
    @Override
    public @Nonnull FreezableCollection<V> values() {
        return new BackedFreezableCollection<V>(this, super.values());
    }
    
    @Pure
    @Override
    public @Nonnull FreezableSet<Map.Entry<K,V>> entrySet() {
        return new BackedFreezableSet<Map.Entry<K,V>>(this, super.entrySet());
    }
    
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public @Nullable V put(@Nullable K key, @Nullable V value) {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.put(key, value);
    }
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public void putAll(@Nonnull Map<? extends K,? extends V> map) {
        assert isNotFrozen() : "This object is not frozen.";
        
        super.putAll(map);
    }
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public @Nullable V remove(@Nullable Object object) {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.remove(object);
    }
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public void clear() {
        assert isNotFrozen() : "This object is not frozen.";
        
        super.clear();
    }
    
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableHashMap<K,V> clone() {
        return new FreezableHashMap<K,V>(this);
    }
    
}
