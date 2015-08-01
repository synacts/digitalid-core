package net.digitalid.core.collections;

import java.util.NoSuchElementException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Frozen;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.NonFrozenRecipient;
import net.digitalid.core.annotations.Pure;

/**
 * This interface models an array iterator that can be {@link Freezable frozen}.
 * As a consequence, all modifying methods may fail with an {@link AssertionError}.
 * (Please note that only the underlying array and not the iterator itself is freezable.)
 * <p>
 * <em>Important:</em> Only use freezable or immutable types for the elements!
 * (The type is not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @see FreezableArray
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public class FreezableArrayIterator<E> implements ReadOnlyArrayIterator<E>, FreezableIterator<E> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Fields –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores a reference to the underlying array.
     */
    private final @Nonnull FreezableArray<E> array;
    
    /**
     * Stores the current index of this iterator.
     */
    private int index = 0;
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new freezable iterator.
     * 
     * @param array a reference to the underlying array.
     */
    protected FreezableArrayIterator(@Nonnull FreezableArray<E> array) {
        this.array = array;
    }
    
    /**
     * Creates a new freezable iterator.
     * 
     * @param array a reference to the underlying array.
     */
    @Pure
    protected static @Capturable @Nonnull <E> FreezableArrayIterator<E> get(@Nonnull FreezableArray<E> array) {
        return new FreezableArrayIterator<>(array);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Freezable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public final boolean isFrozen() {
        return array.isFrozen();
    }
    
    @Override
    public @Nonnull @Frozen ReadOnlyArrayIterator<E> freeze() {
        array.freeze();
        return this;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Iterator –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public boolean hasNext() {
        return index < array.size();
    }
    
    @Override
    public @Nullable E next() {
        if (!hasNext()) throw new NoSuchElementException();
        return array.getNullable(index++);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– ArrayIterator –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public boolean hasPrevious() {
        return index > 0;
    }
    
    @Override
    public @Nullable E previous() {
        if (!hasPrevious()) throw new NoSuchElementException();
        return array.getNullable(--index);
    }
    
    @Pure
    @Override
    public int nextIndex() {
        return index;
    }
    
    @Pure
    @Override
    public int previousIndex() {
        return index - 1;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Operation –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Override
    @NonFrozenRecipient
    public void remove() {
        assert !isFrozen() : "This object is not frozen.";
        
        array.set(index, null);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Cloneable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableArrayIterator<E> clone() {
        return new FreezableArrayIterator<>(array.clone());
    }
    
}
