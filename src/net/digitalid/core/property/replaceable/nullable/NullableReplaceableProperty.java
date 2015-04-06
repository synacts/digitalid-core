package net.digitalid.core.property.replaceable.nullable;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public class NullableReplaceableProperty<V> extends ReadOnlyNullableReplaceableProperty<V> {
    
    private @Nullable V value;
    
    @Override
    public @Nullable V get() {
        return value;
    }
    
    public void set(@Nullable V newValue) {
        final @Nullable V oldValue = this.value;
        this.value = newValue;
        
        if (hasObservers() && !Objects.equals(oldValue, newValue)) {
            for (final @Nonnull NullableReplaceablePropertyObserver<V> observer : getObservers()) observer.replaced(this, oldValue, newValue);
        }
    }
    
}
