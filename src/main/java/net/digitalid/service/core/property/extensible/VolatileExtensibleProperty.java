package net.digitalid.service.core.property.extensible;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.property.ValueValidator;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.freezable.FreezableSet;
import net.digitalid.utility.collections.readonly.ReadOnlySet;

/**
 * The property stores values in volatile memory.
 * 
 * <em>Important:</em> Make sure that {@code F} is a sub-type of {@code R}!
 * Unfortunately, this cannot be enforced with the limited Java generics.
 * 
 * [used for the hosts in the Server class and modules in the Service class]
 * 
 * @param <V> the type of the values.
 * @param <R> the type of the read-only set to which the set is casted to when retrieved with get().
 * @param <F> the type of the set that is used to store the values.
 */
public class VolatileExtensibleProperty<V, R extends ReadOnlySet<V>, F extends FreezableSet<V>> extends WritableExtensibleProperty<V, R, F> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Map –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * A freezable set containing the values of this property.
     */
    private final @Nonnull @Validated F set;
    
    @Pure
    @Override
    @SuppressWarnings("unchecked")
    public @Nonnull @Validated @NonFrozen R get() {
        return (R) set;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Values –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Override
    public void add(@Nonnull V value) {
        boolean alreadyContained = set.add(value);
        
        if (!alreadyContained) notifyAdded(set, value);
    }
    
    @Override
    public void remove(@Nonnull V value) {
        boolean didExist = set.remove(value);
        
        if (didExist) notifyRemoved(set, value);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new volatile extensible property with the given value validator and set.
     *  
     * @param valueValidator the validator that validates the values of the set.
     * @param set the set that holds the values.
     * 
     * @require valuesValid(valueValidator, set) : "The values of the set are valid.";
     */
    private VolatileExtensibleProperty(@Nonnull ValueValidator<? super V> valueValidator, @Nonnull @Validated F set) {
        super(valueValidator);
        
        assert valuesValid(valueValidator, set) : "The values of the set are valid.";
        
        this.set = set;
    }
    
    /**
     * Creates a new volatile extensible property with the given set and the given value validator.
     * 
     * @param valueValidator the validator used to validate the values of this property
     * @param set the set that stores the values of the property.
     * 
     * @return a new volatile indexed property object.
     */
    @Pure
    public static @Nullable <V, R extends ReadOnlySet<V>, F extends FreezableSet<V>> VolatileExtensibleProperty<V, R, F> get(@Nonnull ValueValidator<? super V> valueValidator, @Nonnull @Validated F set) {
        return new VolatileExtensibleProperty<>(valueValidator, set);
    }
    
    /**
     * Creates a new volatile extensible property with the given set.
     * 
     * @param set the set that stores the values of the property.
     * 
     * @return a new volatile indexed property object.
     */
    @Pure
    public static @Nullable <V, R extends ReadOnlySet<V>, F extends FreezableSet<V>> VolatileExtensibleProperty<V, R, F> get(@Nonnull @Validated F set) {
        return get(ValueValidator.DEFAULT, set);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Validator Checks –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Validates that the contents of the map are valid by checking whether the keys and values can be validated with the given
     * key- and value validators.
     * 
     * @param keyValidator the validator used to validate the key of this property.
     * @param valueValidator the validator used to validate the value of this property
     * @param map the map that stores the indexed value of the property.
     * 
     * @return true if the keys and values are valid; false otherwise.
     */
    private boolean valuesValid(@Nonnull ValueValidator<? super V> valueValidator, @Nonnull @Validated F set) {
        for (V value : set) {
            if (!valueValidator.isValid(value)) return false;
        }
        return true;
    }
    
}
