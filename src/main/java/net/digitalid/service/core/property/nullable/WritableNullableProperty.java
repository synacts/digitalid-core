package net.digitalid.service.core.property.nullable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.concept.property.nullable.NullableConceptProperty;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.property.ValueValidator;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.Committing;
import net.digitalid.utility.database.annotations.Locked;

/**
 * This is the writable abstract class for properties that stores a nullable replaceable value.
 * 
 * @see VolatileNullableProperty
 * @see NullableConceptProperty
 */
public abstract class WritableNullableProperty<V> extends ReadOnlyNullableProperty<V> {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new nullable replaceable property with the given validator.
     * 
     * @param validator the validator used to validate the value of this property.
     */
    protected WritableNullableProperty(@Nonnull ValueValidator<? super V> validator) {
        super(validator);
    }
    
    /* -------------------------------------------------- Setter -------------------------------------------------- */
    
    /**
     * Sets the value of this property to the given new value.
     * 
     * @param newValue the new value to replace the old one with.
     * 
     * @require getValidator().isValid(newValue) : "The new value is valid.";
     */
    @Locked
    @Committing
    public abstract void set(@Nullable @Validated V newValue) throws AbortException;
    
    /* -------------------------------------------------- Notification -------------------------------------------------- */
    
    /**
     * Notifies the observers that the value of this property has changed.
     * 
     * @param oldValue the old value of this property that got replaced.
     * @param newValue the new value of this property that replaced the old one.
     * 
     * @require !oldValue.equals(newValue) : "The old and the new value are not the same.";
     */
    protected final void notify(@Nonnull @Validated V oldValue, @Nonnull @Validated V newValue) {
        assert !oldValue.equals(newValue) : "The old and the new value are not the same.";
        
        if (hasObservers()) {
            for (final @Nonnull NullablePropertyObserver<V> observer : getObservers()) {
                observer.replaced(this, oldValue, newValue);
            }
        }
    }
    
}
