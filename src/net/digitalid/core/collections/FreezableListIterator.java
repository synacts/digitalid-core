package net.digitalid.core.collections;

import java.util.ListIterator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.Immutable;

/**
 * This interface models a {@link ListIterator list iterator} that can be {@link Freezable frozen}.
 * As a consequence, all modifying methods may fail with an {@link AssertionError}.
 * (Please note that only the underlying list and not the iterator itself is freezable.)
 * <p>
 * <em>Important:</em> Only use freezable or immutable types for the elements!
 * (The type is not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @see FreezableList
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public class FreezableListIterator<E> extends FreezableIterableIterator<E> implements ReadOnlyListIterator<E> {
    
    /**
     * Stores a reference to the underlying iterator.
     */
    private final @Nonnull ListIterator<E> iterator;
    
    /**
     * Creates a new freezable list iterator.
     * 
     * @param iterable a reference to the underlying iterable.
     * @param iterator a reference to the underlying iterator.
     */
    protected FreezableListIterator(@Nonnull FreezableList<E> iterable, @Nonnull ListIterator<E> iterator) {
        super(iterable, iterator);
        
        this.iterator = iterator;
    }
    
    @Override
    public @Nonnull ReadOnlyListIterator<E> freeze() {
        super.freeze();
        return this;
    }
    
    
    @Pure
    @Override
    public boolean hasPrevious() {
        return iterator.hasPrevious();
    }
    
    @Override
    public @Nullable E previous() {
        return iterator.previous();
    }
    
    @Pure
    @Override
    public int nextIndex() {
        return iterator.nextIndex();
    }
    
    @Pure
    @Override
    public int previousIndex() {
        return iterator.previousIndex();
    }
    
    
    /**
     * @require !isFrozen() : "This object is not frozen.";
     */
    @Override
    public void set(@Nullable E element) {
        assert !isFrozen() : "This object is not frozen.";
        
        iterator.set(element);
    }
    
    /**
     * @require !isFrozen() : "This object is not frozen.";
     */
    @Override
    public void add(@Nullable E element) {
        assert !isFrozen() : "This object is not frozen.";
        
        iterator.add(element);
    }
    
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableListIterator<E> clone() {
        @Nonnull FreezableList<E> list = (FreezableList<E>) iterable;
        return list.clone().listIterator();
    }
    
}
