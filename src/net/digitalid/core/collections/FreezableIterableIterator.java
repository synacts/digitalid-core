package net.digitalid.core.collections;

import java.util.Iterator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.interfaces.Freezable;
import net.digitalid.core.annotations.Immutable;

/**
 * This interface models an {@link Iterator iterator} that can be {@link Freezable frozen}.
 * As a consequence, all modifying methods may fail with an {@link AssertionError}.
 * (Please note that only the underlying iterable and not the iterator itself is freezable.)
 * <p>
 * <em>Important:</em> Only use freezable or immutable types for the elements!
 * (The type is not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @see FreezableIterable
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
class FreezableIterableIterator<E> implements FreezableIterator<E> {
    
    /**
     * Stores a reference to the underlying iterable.
     */
    protected final @Nonnull FreezableIterable<E> iterable;
    
    /**
     * Stores a reference to the underlying iterator.
     */
    private final @Nonnull Iterator<E> iterator;
    
    /**
     * Creates a new freezable iterator.
     * 
     * @param iterable a reference to the underlying iterable.
     * @param iterator a reference to the underlying iterator.
     */
    protected FreezableIterableIterator(@Nonnull FreezableIterable<E> iterable, @Nonnull Iterator<E> iterator) {
        this.iterable = iterable;
        this.iterator = iterator;
    }
    
    
    @Pure
    @Override
    public final boolean isFrozen() {
        return iterable.isFrozen();
    }
    
    @Pure
    @Override
    public final boolean isNotFrozen() {
        return iterable.isNotFrozen();
    }
    
    @Override
    public @Nonnull ReadOnlyIterator<E> freeze() {
        iterable.freeze();
        return this;
    }
    
    
    @Pure
    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }
    
    @Override
    public @Nullable E next() {
        return iterator.next();
    }
    
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public void remove() {
        assert isNotFrozen() : "This object is not frozen.";
        
        iterator.remove();
    }
    
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableIterator<E> clone() {
        return iterable.clone().iterator();
    }
    
}
