package ch.virtualid.util;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Freezable;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This interface provides readonly access to {@link FreezableArrayIterator array iterators} and should not be lost by assigning its objects to a supertype.
 * Never call {@link Iterator#remove()} on a readonly array iterator! Unfortunately, this method cannot be undeclared again.
 * (Please note that only the underlying array and not the iterator itself is readonly (and possibly frozen).)
 * <p>
 * <em>Important:</em> Only use freezable or immutable types for the elements!
 * (The type is not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @see FreezableArrayIterator
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface ReadonlyArrayIterator<E> extends ReadonlyIterator<E> {
    
    /**
     * Returns whether this iterator has a previous element.
     * 
     * @return whether this iterator has a previous element.
     */
    @Pure
    public boolean hasPrevious();
    
    /**
     * Returns the previous element of this iterator.
     * 
     * @return the previous element of this iterator.
     */
    public @Nullable E previous();
    
    /**
     * Returns the index of the next element.
     * 
     * @return the index of the next element.
     */
    @Pure
    public int nextIndex();
    
    /**
     * Returns the index of the previous element.
     * 
     * @return the index of the previous element.
     */
    @Pure
    public int previousIndex();
    
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableArrayIterator<E> clone();
    
}
