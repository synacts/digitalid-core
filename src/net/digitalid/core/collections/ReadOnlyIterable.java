package net.digitalid.core.collections;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.Immutable;

/**
 * This interface provides read-only access to {@link Iterable iterables} and should not be lost by assigning its objects to a supertype.
 * <p>
 * <em>Important:</em> Only use freezable or immutable types for the elements!
 * (The type is not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @see FreezableIterable
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface ReadOnlyIterable<E> extends Iterable<E>, ReadOnly {
    
    @Pure
    @Override
    public @Nonnull ReadOnlyIterator<E> iterator();
    
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableIterable<E> clone();
    
}
