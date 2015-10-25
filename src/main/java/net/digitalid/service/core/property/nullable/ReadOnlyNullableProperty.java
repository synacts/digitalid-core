package net.digitalid.service.core.property.nullable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.property.ReadOnlyProperty;
import net.digitalid.service.core.property.ValueValidator;

/**
 * Description.
 */
public abstract class ReadOnlyNullableProperty<V> extends ReadOnlyProperty<V, NullablePropertyObserver<V>> {
    
    /**
     * Creates a new read-only nullable replaceable property with the given validator.
     * 
     * @param validator the validator used to validate the value of this property.
     */
    ReadOnlyNullableProperty(@Nonnull ValueValidator<? super V> validator) {
        super(validator);
    }
    
    public abstract @Nullable V get();
    
}
