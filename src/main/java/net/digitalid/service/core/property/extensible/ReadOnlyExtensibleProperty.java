package net.digitalid.service.core.property.extensible;

import javax.annotation.Nonnull;
import net.digitalid.service.core.property.ReadOnlyProperty;
import net.digitalid.service.core.property.ValueValidator;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.readonly.ReadOnlySet;

/**
 * This is the read-only abstract class for extensible properties.
 * 
 * @see WritableExtensibleProperty
 */
public abstract class ReadOnlyExtensibleProperty<V, R extends ReadOnlySet<V>> extends ReadOnlyProperty<V, ExtensiblePropertyObserver<V, R>> {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new read-only extensible property with the given validator.
     * 
     * @param valueValidator the validator used to validate the values of this property.
     */
    protected ReadOnlyExtensibleProperty(@Nonnull ValueValidator<? super V> valueValidator) {
        super(valueValidator);
    }
    
    /* -------------------------------------------------- Getter -------------------------------------------------- */
    
    /**
     * Returns a read-only representation of the map.
     * 
     * @return a read-only representation of the map.
     */
    public abstract @Nonnull @NonFrozen R get();
    
}
