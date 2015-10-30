package net.digitalid.service.core.property.indexed;

import javax.annotation.Nonnull;
import net.digitalid.service.core.property.ValueValidator;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.collections.freezable.FreezableMap;
import net.digitalid.utility.collections.readonly.ReadOnlyMap;

/**
 * This is the writable abstract class for properties that stores an indexed value.
 * 
 * <em>Important:</em> Make sure that {@code F} is a subtype of {@code R}!
 * Unfortunately, this cannot be enforced with the limited Java generics.
 * 
 * [used for the hosts in the Server class and modules in the Service class]
 * 
 * @see VolatileIndexedProperty
 */
public abstract class WritableIndexedProperty<K, V, R extends ReadOnlyMap<K, V>, F extends FreezableMap<K, V>> extends ReadOnlyIndexedProperty<K, V, R> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new writable indexed property with the given validators.
     * 
     * @param keyValidator the validator used to validate the key of this property.
     * @param valueValidator the validator used to validate the value of this property.
     */
    protected WritableIndexedProperty(@Nonnull ValueValidator<? super K> keyValidator, @Nonnull ValueValidator<? super V> valueValidator) {
        super(keyValidator, valueValidator);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Modifiers –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Adds a new indexed value to the map.
     * 
     * @param key the key of this property that got added.
     * @param value the value of this property that got added.
     * 
     * @require !map.containsKey(key) : "The key may not already be indexed.";
     */
    public abstract void add(@Nonnull K key, @Nonnull V value);
    
    /**
     * Removes an indexed value from the map.
     * 
     * @param key the key of this property that got removed.
     * @param value the value of this property that got removed.
     */
    public abstract void remove(@Nonnull K key);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Notification –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Notifies the observers that a key-value pair was added.
     * 
     * @param key the key of this property that got added.
     * @param value the value of this property that got added.
     */
    protected final void notifyAdded(@Nonnull @Validated K key, @Nonnull @Validated V value) {
        if (hasObservers()) {
            for (final @Nonnull IndexedPropertyObserver<K, V, R> observer : getObservers()) {
                observer.added(this, key, value);
            }
        }
    }
    
    /**
     * Notifies the observers that a key-value pair was removed.
     * 
     * @param key the key of this property that got removed.
     * @param value the value of this property that got removed.
     */
    protected final void notifyRemoved(@Nonnull @Validated K key, @Nonnull @Validated V value) {
        if (hasObservers()) {
            for (final @Nonnull IndexedPropertyObserver<K, V, R> observer : getObservers()) {
                observer.removed(this, key, value);
            }
        }
    }
    
}
