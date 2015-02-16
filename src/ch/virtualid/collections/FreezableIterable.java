package ch.virtualid.collections;

import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Freezable;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;

/**
 * This interface models an {@link Iterable iterable} that can be {@link Freezable frozen}.
 * As a consequence, all modifying methods may fail with an {@link AssertionError}.
 * <p>
 * <em>Important:</em> Only use freezable or immutable types for the elements!
 * (The type is not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface FreezableIterable<E> extends ReadonlyIterable<E>, Freezable {
    
    @Override
    public @Nonnull ReadonlyIterable<E> freeze();
    
    @Pure
    @Override
    public FreezableIterator<E> iterator();
    
}
