package net.digitalid.core.collections;

import java.util.ListIterator;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.interfaces.Freezable;
import net.digitalid.core.interfaces.Immutable;

/**
 * This interface provides readonly access to {@link ListIterator list iterators} and should not be lost by assigning its objects to a supertype.
 * Never call a method that modifies the underlying list on a readonly list iterator! Unfortunately, these methods cannot be undeclared again.
 * (Please note that only the underlying list and not the iterator itself is readonly (and possibly frozen).)
 * <p>
 * <em>Important:</em> Only use freezable or immutable types for the elements!
 * (The type is not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @see FreezableListIterator
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface ReadonlyListIterator<E> extends ReadonlyIterator<E>, ListIterator<E> {
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableListIterator<E> clone();
    
}
