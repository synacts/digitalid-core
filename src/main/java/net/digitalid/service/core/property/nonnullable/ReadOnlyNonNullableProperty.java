package net.digitalid.service.core.property.nonnullable;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.service.core.property.ReadOnlyProperty;
import net.digitalid.service.core.property.ValueValidator;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * This is the read-only interface to a property that stores a non-nullable replaceable value.
 * 
 * @see NonNullableReplaceableProperty
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
public abstract class ReadOnlyNonNullableProperty<V> extends ReadOnlyProperty<V, NonNullablePropertyObserver<V>> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new read-only non-nullable replaceable property with the given validator.
     * 
     * @param validator the validator used to validate the value of this property.
     */
    protected ReadOnlyNonNullableProperty(@Nonnull ValueValidator<? super V> validator) {
        super(validator);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Getter –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the value of this replaceable property.
     * 
     * @return the value of this replaceable property.
     * 
     * @ensure getValidator().isValid(return) : "The returned value is valid.";
     */
    @Pure
    @Locked
    @NonCommitting
    public abstract @Nonnull @Validated V get() throws SQLException;
    
}
