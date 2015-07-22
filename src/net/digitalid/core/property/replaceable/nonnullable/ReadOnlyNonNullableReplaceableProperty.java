package net.digitalid.core.property.replaceable.nonnullable;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.Validated;
import net.digitalid.core.property.ReadOnlyProperty;
import net.digitalid.core.property.ValueValidator;

/**
 * This is the read-only interface to a property that stores a non-null replaceable value.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public abstract class ReadOnlyNonNullableReplaceableProperty<V> extends ReadOnlyProperty<V, NonNullableReplaceablePropertyObserver<V>> {
    
    /**
     * Creates a new read-only non-nullable replaceable property with the given validator.
     * 
     * @param validator the validator used to validate the value of this property.
     */
    ReadOnlyNonNullableReplaceableProperty(@Nonnull ValueValidator<? super V> validator) {
        super(validator);
    }
    
    /**
     * Returns the value of this replaceable property.
     * 
     * @return the value of this replaceable property.
     */
    @Pure
    public abstract @Nonnull @Validated V get() throws SQLException;
    
}
