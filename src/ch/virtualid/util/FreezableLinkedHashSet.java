package ch.virtualid.util;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Freezable;
import ch.virtualid.interfaces.Immutable;
import java.util.Collection;
import java.util.LinkedHashSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class extends the {@link LinkedHashSet} and makes it {@link Freezable}.
 * Be careful when treating instances of this class as normal {@link Set sets} because all modifying methods may fail with an {@link AssertionError}.
 * <p>
 * <em>Important:</em> Only use {@link Immutable immutable} or {@link Freezable frozen} objects as elements!
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public class FreezableLinkedHashSet<E> extends LinkedHashSet<E> implements FreezableSet<E> {
    
    /**
     * Stores whether this object is frozen.
     */
    private boolean frozen = false;
    
    @Pure
    @Override
    public final boolean isFrozen() {
        return frozen;
    }
    
    @Pure
    @Override
    public final boolean isNotFrozen() {
        return !frozen;
    }
    
    @Override
    public @Nonnull ReadonlySet<E> freeze() {
        if (!frozen) {
            frozen = true;
            for (E element : this) {
                if (element instanceof Freezable) {
                    ((Freezable) element).freeze();
                } else {
                    break;
                }
            }
        }
        return this;
    }
    
    
    /**
     * @see LinkedHashSet#LinkedHashSet()
     */
    public FreezableLinkedHashSet() {
        super();
    }
    
    /**
     * Creates a new freezable linked hash set with the given element.
     * 
     * @param element the element to add to the newly created hash set.
     */
    public FreezableLinkedHashSet(@Nullable E element) {
        this();
        add(element);
    }
    
    /**
     * @see LinkedHashSet#LinkedHashSet(int)
     */
    public FreezableLinkedHashSet(int initialCapacity) {
        super(initialCapacity);
    }
    
    /**
     * @see LinkedHashSet#LinkedHashSet(int, float)
     */
    public FreezableLinkedHashSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }
    
    /**
     * @see LinkedHashSet#LinkedHashSet(java.util.Collection)
     */
    public FreezableLinkedHashSet(@Nonnull Collection<? extends E> collection) {
        super(collection);
    }
    
    
    @Pure
    @Override
    public boolean isNotEmpty() {
        return !super.isEmpty();
    }
    
    @Pure
    @Override
    public @Nonnull FreezableIterator<E> iterator() {
        return new FreezableIterableIterator<E>(this, super.iterator());
    }
    
    
    @Pure
    @Override
    public boolean doesNotContainNull() {
        for (final @Nullable E element : this) {
            if (element == null) return false;
        }
        return true;
    }
    
    @Pure
    @Override
    public boolean doesNotContainDuplicates() {
        return true;
    }
    
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public boolean add(@Nullable E element) {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.add(element);
    }
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public boolean addAll(@Nonnull Collection<? extends E> c) {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.addAll(c);
    }
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public boolean remove(@Nullable Object object) {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.remove(object);
    }
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public boolean removeAll(@Nonnull Collection<?> c) {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.removeAll(c);
    }
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public boolean retainAll(@Nonnull Collection<?> c) {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.retainAll(c);
    }
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public void clear() {
        assert isNotFrozen() : "This object is not frozen.";
        
        super.clear();
    }
    
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableLinkedHashSet<E> clone() {
        return new FreezableLinkedHashSet<E>(this);
    }
    
    @Pure
    @Override
    @SuppressWarnings("unchecked")
    public @Capturable @Nonnull FreezableArray<E> toFreezableArray() {
        return new FreezableArray<E>(toArray((E[]) new Object[size()]));
    }
    
}
