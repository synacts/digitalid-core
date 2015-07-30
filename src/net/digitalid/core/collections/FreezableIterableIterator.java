package net.digitalid.core.collections;

import java.util.Iterator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Frozen;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.NonFrozenRecipient;
import net.digitalid.core.annotations.Pure;

/**
 * This interface models an {@link Iterator iterator} that can be {@link Freezable frozen}.
 * As a consequence, all modifying methods may fail with an {@link AssertionError}.
 * (Please note that only the underlying iterable and not the iterator itself is freezable.)
 * <p>
 * <em>Important:</em> Only use freezable or immutable types for the elements!
 * (The type is not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @see FreezableListIterator
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
class FreezableIterableIterator<E> implements FreezableIterator<E> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Fields –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores a reference to the underlying iterable.
     */
    protected final @Nonnull FreezableIterable<E> iterable;
    
    /**
     * Stores a reference to the underlying iterator.
     */
    private final @Nonnull Iterator<E> iterator;
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new freezable iterator.
     * 
     * @param iterable a reference to the underlying iterable.
     * @param iterator a reference to the underlying iterator.
     */
    FreezableIterableIterator(@Nonnull FreezableIterable<E> iterable, @Nonnull Iterator<E> iterator) {
        this.iterable = iterable;
        this.iterator = iterator;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Freezable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public final boolean isFrozen() {
        return iterable.isFrozen();
    }
    
    @Override
    public @Nonnull @Frozen ReadOnlyIterator<E> freeze() {
        iterable.freeze();
        return this;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Iterator –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }
    
    @Override
    public @Nullable E next() {
        return iterator.next();
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Operation –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Override
    @NonFrozenRecipient
    public void remove() {
        assert !isFrozen() : "This object is not frozen.";
        
        iterator.remove();
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Cloneable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableIterator<E> clone() {
        return iterable.clone().iterator();
    }
    
}
