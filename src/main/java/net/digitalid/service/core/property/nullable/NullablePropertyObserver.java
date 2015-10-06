package net.digitalid.service.core.property.nullable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.property.PropertyObserver;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.0
 */
public interface NullablePropertyObserver<V> extends PropertyObserver {
    
    public void replaced(@Nonnull ReadOnlyNullableProperty<V> property, @Nullable V oldValue, @Nullable V newValue);
    
}
