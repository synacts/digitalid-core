package net.digitalid.core.collections;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Frozen;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.NonFrozenRecipient;
import net.digitalid.core.annotations.Pure;

/**
 * This class extends the {@link LinkedHashMap} and makes it {@link Freezable}.
 * Be careful when treating instances of this class as normal {@link Map maps} because all modifying methods may fail with an {@link AssertionError}.
 * <p>
 * <em>Important:</em> Only use immutable types for the keys and freezable or immutable types for the values!
 * (The types are not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public class FreezableLinkedHashMap<K,V> extends LinkedHashMap<K,V> implements FreezableMap<K,V> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Freezable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores whether this object is frozen.
     */
    private boolean frozen = false;
    
    @Pure
    @Override
    public final boolean isFrozen() {
        return frozen;
    }
    
    @Override
    public @Nonnull @Frozen ReadOnlyMap<K,V> freeze() {
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructors –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * @see LinkedHashMap#LinkedHashMap()
     */
    public FreezableLinkedHashMap() {
        super();
    }
    
    /**
     * @see LinkedHashMap#LinkedHashMap(int)
     */
    public FreezableLinkedHashMap(int initialCapacity) {
        super(initialCapacity);
    }
    
    /**
     * @see LinkedHashMap#LinkedHashMap(int, float)
     */
    public FreezableLinkedHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }
    
    /**
     * @see LinkedHashMap#LinkedHashMap(int, float, boolean)
     */
    public FreezableLinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor);
    }

    /**
     * @see LinkedHashMap#LinkedHashMap(java.util.Map)
     */
    public FreezableLinkedHashMap(@Nonnull Map<? extends K, ? extends V> map) {
        super(map);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Entries –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Nonnull FreezableSet<K> keySet() {
        return new BackedFreezableSet<>(this, super.keySet());
    }
    
    @Pure
    @Override
    public @Nonnull FreezableCollection<V> values() {
        return new BackedFreezableCollection<>(this, super.values());
    }
    
    @Pure
    @Override
    public @Nonnull FreezableSet<Map.Entry<K,V>> entrySet() {
        return new BackedFreezableSet<>(this, super.entrySet());
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Operations –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Override
    @NonFrozenRecipient
    public @Nullable V put(@Nullable K key, @Nullable V value) {
        assert !isFrozen() : "This object is not frozen.";
        
        return super.put(key, value);
    }
    
    @Override
    @NonFrozenRecipient
    public void putAll(@Nonnull Map<? extends K,? extends V> map) {
        assert !isFrozen() : "This object is not frozen.";
        
        super.putAll(map);
    }
    
    @Override
    @NonFrozenRecipient
    public @Nonnull V putIfAbsentOrNullElseReturnPresent(@Nonnull K key, @Nonnull V value) {
        assert !isFrozen() : "This object is not frozen.";
        
        final @Nullable V oldValue = get(key);
        if (oldValue != null) return oldValue;
        put(key, value);
        return value;
    }
    
    @Override
    @NonFrozenRecipient
    public @Nullable V remove(@Nullable Object object) {
        assert !isFrozen() : "This object is not frozen.";
        
        return super.remove(object);
    }
    
    @Override
    @NonFrozenRecipient
    public void clear() {
        assert !isFrozen() : "This object is not frozen.";
        
        super.clear();
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Cloneable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableLinkedHashMap<K,V> clone() {
        return new FreezableLinkedHashMap<>(this);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Object –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return IterableConverter.toString(entrySet(), new ElementConverter<Entry<K, V>>() { @Pure @Override public String toString(@Nullable Entry<K, V> entry) { return entry == null ? "null" : entry.getKey() + ": " + entry.getValue(); } }, Brackets.CURLY);
    }
    
}
