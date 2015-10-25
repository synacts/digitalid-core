package net.digitalid.service.core.property.nonnullable;

import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.property.ValueValidator;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.Committing;
import net.digitalid.utility.database.annotations.Locked;

/**
 * This is the writable interface to a property that stores a non-null replaceable value.
 * 
 * @see NonNullableSimpleProperty
 * @see NonNullableConceptProperty
 */
public abstract class WriteableNonNullableProperty<V> extends ReadOnlyNonNullableProperty<V> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new non-nullable replaceable property with the given validator.
     * 
     * @param validator the validator used to validate the value of this property.
     */
    protected WriteableNonNullableProperty(@Nonnull ValueValidator<? super V> validator) {
        super(validator);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Setter –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Sets the value of this property to the given new value.
     * 
     * @param newValue the new value to replace the old one with.
     * 
     * @require getValidator().isValid(newValue) : "The new value is valid.";
     */
    @Locked
    @Committing
    public abstract void set(@Nonnull @Validated V newValue) throws AbortException;
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Notification –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
            for (final @Nonnull NonNullablePropertyObserver<V> observer : getObservers()) {
                observer.replaced(this, oldValue, newValue);
            }
        }
    }
    
}
