package net.digitalid.core.property.replaceable.nonnullable;

import javax.annotation.Nonnull;
import net.digitalid.core.property.PropertyObserver;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.0
 */
public interface NonNullableReplaceablePropertyObserver<V> extends PropertyObserver {
    
    public void replaced(@Nonnull ReadOnlyNonNullableReplaceableProperty<V> property, @Nonnull V oldValue, @Nonnull V newValue);
    
}
