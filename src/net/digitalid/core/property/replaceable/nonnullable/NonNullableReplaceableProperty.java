package net.digitalid.core.property.replaceable.nonnullable;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Validated;
import net.digitalid.core.property.ValueValidator;

/**
 * This is the writable interface to a property that stores a non-null replaceable value.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public abstract class NonNullableReplaceableProperty<V> extends ReadOnlyNonNullableReplaceableProperty<V> {
    
    /**
     * Creates a new non-nullable replaceable property with the given validator.
     * 
     * @param validator the validator used to validate the value of this property.
     */
    NonNullableReplaceableProperty(@Nonnull ValueValidator<? super V> validator) {
        super(validator);
    }
    
    /**
     * Sets the value of this property to the given new value.
     * 
     * @param newValue the new value to replace the old one.
     */
    public abstract void set(@Nonnull @Validated V newValue) throws SQLException;
    
    /**
     * Notifies the observers that the value of this property has changed.
     * 
     * @param oldValue the old value of this property that got replaced.
     * @param newValue the new value of this property that replaced the old one.
     * 
     * @require !oldValue.equals(newValue) : "The old and the new value are not the same.";
     */
    final void notify(@Nonnull @Validated V oldValue, @Nonnull @Validated V newValue) {
        assert !oldValue.equals(newValue) : "The old and the new value are not the same.";
        
        if (hasObservers()) {
            for (final @Nonnull NonNullableReplaceablePropertyObserver<V> observer : getObservers()) {
                observer.replaced(this, oldValue, newValue);
            }
        }
    }
    
}
