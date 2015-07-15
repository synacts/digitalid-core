package net.digitalid.core.property.replaceable.nullable;

import javax.annotation.Nullable;
import net.digitalid.core.property.Property;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.0
 */
public abstract class ReadOnlyNullableReplaceableProperty<V> extends Property<NullableReplaceablePropertyObserver<V>> {
    
    public abstract @Nullable V get();
    
}
