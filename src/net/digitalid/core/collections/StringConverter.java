package net.digitalid.core.collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Pure;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.0
 */
public interface StringConverter<E> {
    
    public final static StringConverter<Object> DEFAULT = new StringConverter<Object>() {
        @Pure
        @Override
        public @Nonnull String convertToString(Object element) {
            return element.toString();
        }
    };
    
    @Pure
    public @Nonnull String convertToString(@Nullable E element);
    
}
