package net.digitalid.service.core.property;

import javax.annotation.Nonnull;
import net.digitalid.utility.annotations.state.Pure;

/**
 * A value validator checks whether a value is valid.
 */
public interface ValueValidator<V> {
    
    public final static ValueValidator<Object> DEFAULT = new ValueValidator<Object>() {
        @Pure
        @Override
        public boolean isValid(@Nonnull Object value) {
            return true;
        }
    };
    
    @Pure
    public boolean isValid(@Nonnull V value);
    
}
