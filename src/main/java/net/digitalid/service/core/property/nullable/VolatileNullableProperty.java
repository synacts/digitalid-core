package net.digitalid.service.core.property.nullable;

import net.digitalid.utility.database.annotations.Committing;
import net.digitalid.utility.database.annotations.Locked;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.property.ValueValidator;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;

/**
 * This property stores a replaceable value that can be null.
 */
public class VolatileNullableProperty<V> extends WritableNullableProperty<V> {
    
    /* -------------------------------------------------- Value -------------------------------------------------- */
    
    /**
     * Stores the value of this property.
     */
    private @Nullable @Validated V value;
    
    @Pure
    @Override
    public @Nullable @Validated V get() {
        return value;
    }
    
    @Locked
    @Committing
    @Override
    public void set(@Nullable @Validated V newValue) {
        assert getValueValidator().isValid(newValue) : "The new value is valid.";
        
        final @Nullable V oldValue = this.value;
        this.value = newValue;
        
        if (!newValue.equals(oldValue)) notify(oldValue, newValue);
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    private VolatileNullableProperty(@Nonnull ValueValidator<? super V> validator, @Nullable @Validated V value) {
        super(validator);
        
        assert validator.isValid(value) : "The given value is valid.";
        
        this.value = value;
    }
    
    /**
     * Returns a new nullable replaceable property with the given initial value.
     * 
     * @param validator the validator used to validate the value of the new property.
     * @param value the initial value of the new nullable replaceable property.
     * 
     * @return a new nullable replaceable property with the given initial value.
     */
    @Pure
    public static @Nullable <V> VolatileNullableProperty<V> get(@Nonnull ValueValidator<? super V> validator, @Nullable @Validated V value) {
        return new VolatileNullableProperty<>(validator, value);
    }
    
    /**
     * Returns a new nullable replaceable property with the given initial value.
     * 
     * @param validator the validator used to validate the value of the new property.
     * 
     * @return a new nullable replaceable property with the given initial value.
     */
    @Pure
    public static @Nullable <V> VolatileNullableProperty<V> get(@Nonnull ValueValidator<? super V> validator) {
        return get(validator, null);
    }
    
    /**
     * Returns a new nullable replaceable property with the given initial value.
     * 
     * @param value the initial value of the new nullable replaceable property.
     * 
     * @return a new nullable replaceable property with the given initial value.
     */
    @Pure
    public static @Nullable <V> VolatileNullableProperty<V> get(@Nullable @Validated V value) {
        return get(ValueValidator.DEFAULT, value);
    }
    
}