package net.digitalid.core.property.replaceable.nullable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.property.PropertyObserver;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public interface NullableReplaceablePropertyObserver<V> extends PropertyObserver {
    
    public void replaced(@Nonnull ReadOnlyNullableReplaceableProperty<V> property, @Nullable V oldValue, @Nullable V newValue);
    
}
