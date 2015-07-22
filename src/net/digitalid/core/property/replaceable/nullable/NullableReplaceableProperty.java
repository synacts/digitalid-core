package net.digitalid.core.property.replaceable.nullable;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.property.ValueValidator;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.0
 */
public class NullableReplaceableProperty<V> extends ReadOnlyNullableReplaceableProperty<V> {
    
    /**
     * Creates a new nullable replaceable property with the given validator.
     * 
     * @param validator the validator used to validate the value of this property.
     */
    NullableReplaceableProperty(@Nonnull ValueValidator<? super V> validator) {
        super(validator);
    }
    
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
