package net.digitalid.core.property.replaceable.nullable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.property.ReadOnlyProperty;
import net.digitalid.core.property.ValueValidator;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.0
 */
public abstract class ReadOnlyNullableReplaceableProperty<V> extends ReadOnlyProperty<V, NullableReplaceablePropertyObserver<V>> {
    
    /**
     * Creates a new read-only nullable replaceable property with the given validator.
     * 
     * @param validator the validator used to validate the value of this property.
     */
    ReadOnlyNullableReplaceableProperty(@Nonnull ValueValidator<? super V> validator) {
        super(validator);
    }
    
    public abstract @Nullable V get();
    
}
