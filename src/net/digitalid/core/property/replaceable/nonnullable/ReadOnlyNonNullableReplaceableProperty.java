package net.digitalid.core.property.replaceable.nonnullable;

import javax.annotation.Nonnull;
import net.digitalid.core.property.Property;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.0
 */
public abstract class ReadOnlyNonNullableReplaceableProperty<V> extends Property<NonNullableReplaceablePropertyObserver<V>> {
    
    public abstract @Nonnull V get();
    
}
