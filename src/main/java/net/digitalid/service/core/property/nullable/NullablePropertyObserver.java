package net.digitalid.service.core.property.nullable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.property.PropertyObserver;

/**
 * Description.
 */
public interface NullablePropertyObserver<V> extends PropertyObserver {
    
    public void replaced(@Nonnull ReadOnlyNullableProperty<V> property, @Nullable V oldValue, @Nullable V newValue);
    
}
