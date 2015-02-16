package ch.virtualid.collections;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Freezable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.Readonly;
import java.util.Iterator;
import javax.annotation.Nonnull;

/**
 * This interface provides readonly access to {@link Iterator iterators} and should not be lost by assigning its objects to a supertype.
 * Never call {@link Iterator#remove()} on a readonly iterator! Unfortunately, this method cannot be undeclared again.
 * (Please note that only the underlying iterable and not the iterator itself is readonly (and possibly frozen).)
 * <p>
 * <em>Important:</em> Only use freezable or immutable types for the elements!
 * (The type is not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @see FreezableIterableIterator
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface ReadonlyIterator<E> extends Iterator<E>, Readonly {
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableIterator<E> clone();
    
}
