package net.digitalid.service.core.property.nullable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.property.ReadOnlyProperty;
import net.digitalid.service.core.property.ValueValidator;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * This is the read-only abstract class for properties that stores a nullable replaceable value.
 * 
 * @see NullableReplaceableProperty
 */
public abstract class ReadOnlyNullableProperty<V> extends ReadOnlyProperty<V, NullablePropertyObserver<V>> {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new read-only nullable replaceable property with the given validator.
     * 
     * @param validator the validator used to validate the value of this property.
     */
    protected ReadOnlyNullableProperty(@Nonnull ValueValidator<? super V> validator) {
        super(validator);
    }
    
    /* -------------------------------------------------- Getter -------------------------------------------------- */
    
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
    public abstract @Nullable @Validated V get();
    
}
