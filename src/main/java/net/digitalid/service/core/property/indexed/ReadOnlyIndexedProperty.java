package net.digitalid.service.core.property.indexed;

import javax.annotation.Nonnull;
import net.digitalid.service.core.property.ReadOnlyProperty;
import net.digitalid.service.core.property.ValueValidator;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.collections.readonly.ReadOnlyCollection;
import net.digitalid.utility.collections.readonly.ReadOnlyMap;

/**
 * This is the read-only abstract class for properties that stores an indexed value.
 * 
 * @see WritableIndexedProperty
 */
public abstract class ReadOnlyIndexedProperty<K, V, R extends ReadOnlyMap<K, V>> extends ReadOnlyProperty<V, IndexedPropertyObserver<K, V, R>> {
    
    /**
     * Stores the key validator, which is only used in indexed property objects.
     */
    private final @Nonnull ValueValidator<? super K> keyValidator;
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new read-only indexed property with the given validators.
     * 
     * @param keyValidator the validator used to validate the key of this property.
     * @param valueValidator the validator used to validate the value of this property.
     */
    protected ReadOnlyIndexedProperty(@Nonnull ValueValidator<? super K> keyValidator, @Nonnull ValueValidator<? super V> valueValidator) {
        super(valueValidator);
        
        this.keyValidator = keyValidator;
    }
    
    /* -------------------------------------------------- Getter -------------------------------------------------- */
    
    /**
     * Returns the value for a given key.
     * 
     * @param key the key which indexes the value.
     * 
     * @return the value for a given key.
     */
    public abstract @Nonnull @Validated V get(@Nonnull K key);
    
    /**
     * Returns all indexed values.
     * 
     * @return all indexed values.
     */
    public abstract @Nonnull @Validated ReadOnlyCollection<V> getAll();
    
    /**
     * Returns a read-only representation of the map.
     * 
     * @return a read-only representation of the map.
     */
    public abstract @Nonnull @Validated R getMap();
    
}
