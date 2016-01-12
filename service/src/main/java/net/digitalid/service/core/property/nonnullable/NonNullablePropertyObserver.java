package net.digitalid.service.core.property.nonnullable;

import javax.annotation.Nonnull;
import net.digitalid.service.core.property.PropertyObserver;
import net.digitalid.service.core.property.ReadOnlyProperty;
import net.digitalid.utility.annotations.state.Validated;

/**
 * Objects that implement this interface can be used to observe {@link ReadOnlyNonNullableProperty non-nullable properties}.
 */
public interface NonNullablePropertyObserver<V> extends PropertyObserver {
    
    /**
     * This method is called on {@link ReadOnlyProperty#register(net.digitalid.service.core.property.PropertyObserver) registered} observers when the value of the given property has been replaced.
     * 
     * @param property the property whose value has been replaced.
     * @param oldValue the old value of the given property.
     * @param newValue the new value of the given property.
     * 
     * assert !newValue.equals(oldValue) : "The new value is not the same as the old value.";
     */
    public void replaced(@Nonnull ReadOnlyNonNullableProperty<V> property, @Nonnull @Validated V oldValue, @Nonnull @Validated V newValue);
    
}
