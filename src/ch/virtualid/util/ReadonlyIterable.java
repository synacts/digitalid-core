package ch.virtualid.util;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Freezable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.Readonly;
import javax.annotation.Nonnull;

/**
 * This interface provides readonly access to {@link Iterable iterables} and should not be lost by assigning its objects to a supertype.
 * <p>
 * <em>Important:</em> Only use freezable or immutable types for the elements!
 * (The type is not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @see FreezableIterable
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface ReadonlyIterable<E> extends Iterable<E>, Readonly {
    
    @Pure
    @Override
    public @Nonnull ReadonlyIterator<E> iterator();
    
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableIterable<E> clone();
    
}
