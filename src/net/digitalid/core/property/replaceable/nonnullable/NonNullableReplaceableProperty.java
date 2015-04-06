package net.digitalid.core.property.replaceable.nonnullable;

import javax.annotation.Nonnull;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public class NonNullableReplaceableProperty<V> extends ReadOnlyNonNullableReplaceableProperty<V> {
    
    private @Nonnull V value;
    
    @Override
    public @Nonnull V get() {
        return value;
    }
    
    public void set(@Nonnull V newValue) {
        final @Nonnull V oldValue = this.value;
        this.value = newValue;
        
        if (hasObservers() && !oldValue.equals(newValue)) {
            for (final @Nonnull NonNullableReplaceablePropertyObserver<V> observer : getObservers()) observer.replaced(this, oldValue, newValue);
        }
    }
    
}
