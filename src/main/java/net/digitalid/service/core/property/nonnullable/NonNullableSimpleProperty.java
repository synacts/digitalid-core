package net.digitalid.core.property.nonnullable;

import javax.annotation.Nonnull;
import net.digitalid.annotations.state.Pure;
import net.digitalid.annotations.state.Validated;
import net.digitalid.core.property.ValueValidator;

/**
 * This property stores a replaceable value that cannot be null.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
public final class NonNullableSimpleProperty<V> extends WriteableNonNullableProperty<V> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Value –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the value of this property.
     */
    private @Nonnull @Validated V value;
    
    @Pure
    @Override
    public @Nonnull @Validated V get() {
        return value;
    }
    
    @Override
    public void set(@Nonnull @Validated V newValue) {
        assert getValidator().isValid(newValue) : "The new value is valid.";
        
        final @Nonnull V oldValue = this.value;
        this.value = newValue;
        
        if (!newValue.equals(oldValue)) notify(oldValue, newValue);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new non-nullable replaceable property with the given initial value.
     * 
     * @param validator the validator used to validate the value of this property.
     * @param value the initial value of the new non-nullable replaceable property.
     */
    private NonNullableSimpleProperty(@Nonnull ValueValidator<? super V> validator, @Nonnull @Validated V value) {
        super(validator);
        
        assert validator.isValid(value) : "The given value is valid.";
        
        this.value = value;
    }
    
    /**
     * Returns a new non-nullable replaceable property with the given initial value.
     * 
     * @param validator the validator used to validate the value of the new property.
     * @param value the initial value of the new non-nullable replaceable property.
     * 
     * @return a new non-nullable replaceable property with the given initial value.
     */
    @Pure
    public static @Nonnull <V> WriteableNonNullableProperty<V> get(@Nonnull ValueValidator<? super V> validator, @Nonnull @Validated V value) {
        return new NonNullableSimpleProperty<>(validator, value);
    }
    
    /**
     * Returns a new non-nullable replaceable property with the given initial value.
     * 
     * @param value the initial value of the new non-nullable replaceable property.
     * 
     * @return a new non-nullable replaceable property with the given initial value.
     */
    @Pure
    public static @Nonnull <V> WriteableNonNullableProperty<V> get(@Nonnull V value) {
        return new NonNullableSimpleProperty<>(ValueValidator.DEFAULT, value);
    }
    
}
