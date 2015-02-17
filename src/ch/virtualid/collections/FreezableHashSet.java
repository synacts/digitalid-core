package ch.virtualid.collections;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.NonFrozenRecipient;
import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Freezable;
import ch.virtualid.interfaces.Immutable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class extends the {@link HashSet} and makes it {@link Freezable}.
 * Be careful when treating instances of this class as normal {@link Set sets} because all modifying methods may fail with an {@link AssertionError}.
 * <p>
 * <em>Important:</em> Only use {@link Immutable immutable} or {@link Freezable frozen} objects as elements!
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public class FreezableHashSet<E> extends HashSet<E> implements FreezableSet<E> {
    
    /**
     * Stores whether this object is frozen.
     */
    private boolean frozen = false;
    
    @Pure
    @Override
    public boolean isFrozen() {
        return frozen;
    }
    
    @Pure
    @Override
    public boolean isNotFrozen() {
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
     * @see HashSet#HashSet()
     */
    public FreezableHashSet() {
        super();
    }
    
    /**
     * Creates a new freezable hash set with the given element.
     * 
     * @param element the element to add to the newly created hash set.
     */
    public FreezableHashSet(@Nullable E element) {
        this();
        add(element);
    }
    
    /**
     * @see HashSet#HashSet(int)
     */
    public FreezableHashSet(int initialCapacity) {
        super(initialCapacity);
    }
    
    /**
     * @see HashSet#HashSet(int, float)
     */
    public FreezableHashSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }
    
    /**
     * @see HashSet#HashSet(java.util.Collection)
     */
    public FreezableHashSet(@Nonnull Collection<? extends E> collection) {
        super(collection);
    }
    
    
    @Pure
    @Override
    public boolean isNotEmpty() {
        return !super.isEmpty();
    }
    
    @Pure
    @Override
    public boolean isSingle() {
        return size() == 1;
    }
    
    @Pure
    @Override
    public boolean isNotSingle() {
        return size() != 1;
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
    
    
    @Override
    @NonFrozenRecipient
    public boolean add(@Nullable E element) {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.add(element);
    }
    
    @Override
    @NonFrozenRecipient
    public boolean addAll(@Nonnull Collection<? extends E> c) {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.addAll(c);
    }
    
    @Override
    @NonFrozenRecipient
    public boolean remove(@Nullable Object object) {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.remove(object);
    }
    
    @Override
    @NonFrozenRecipient
    public boolean removeAll(@Nonnull Collection<?> c) {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.removeAll(c);
    }
    
    @Override
    @NonFrozenRecipient
    public boolean retainAll(@Nonnull Collection<?> c) {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.retainAll(c);
    }
    
    @Override
    @NonFrozenRecipient
    public void clear() {
        assert isNotFrozen() : "This object is not frozen.";
        
        super.clear();
    }
    
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableSet<E> add(ReadonlySet<E> set) {
        final @Nonnull FreezableSet<E> clone = clone();
        clone.addAll((FreezableSet<E>) set);
        return clone;
    }
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableSet<E> subtract(ReadonlySet<E> set) {
        final @Nonnull FreezableSet<E> clone = clone();
        clone.removeAll((FreezableSet<E>) set);
        return clone;
    }
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableSet<E> intersect(ReadonlySet<E> set) {
        final @Nonnull FreezableSet<E> clone = clone();
        clone.retainAll((FreezableSet<E>) set);
        return clone;
    }
    
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableHashSet<E> clone() {
        return new FreezableHashSet<E>(this);
    }
    
    @Pure
    @Override
    @SuppressWarnings("unchecked")
    public @Capturable @Nonnull FreezableArray<E> toFreezableArray() {
        return new FreezableArray<E>(toArray((E[]) new Object[size()]));
    }
    
}
