package net.digitalid.core.collections;

import java.util.Iterator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.interfaces.Freezable;
import net.digitalid.core.annotations.Immutable;

/**
 * This interface provides read-only access to {@link FreezableArrayIterator array iterators} and should not be lost by assigning its objects to a supertype.
 * Never call {@link Iterator#remove()} on a read-only array iterator! Unfortunately, this method cannot be undeclared again.
 * (Please note that only the underlying array and not the iterator itself is read-only (and possibly frozen).)
 * <p>
 * <em>Important:</em> Only use freezable or immutable types for the elements!
 * (The type is not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @see FreezableArrayIterator
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface ReadOnlyArrayIterator<E> extends ReadOnlyIterator<E> {
    
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
