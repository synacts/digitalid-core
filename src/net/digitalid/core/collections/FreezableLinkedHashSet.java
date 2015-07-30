package net.digitalid.core.collections;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Frozen;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.NonFrozenRecipient;
import net.digitalid.core.annotations.Pure;

/**
 * This class extends the {@link LinkedHashSet} and makes it {@link Freezable}.
 * Be careful when treating instances of this class as normal {@link Set sets} because all modifying methods may fail with an {@link AssertionError}.
 * <p>
 * <em>Important:</em> Only use {@link Immutable immutable} or {@link Freezable frozen} objects as elements!
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public class FreezableLinkedHashSet<E> extends LinkedHashSet<E> implements FreezableSet<E> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Freezable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores whether this object is frozen.
     */
    private boolean frozen = false;
    
    @Pure
    @Override
    public boolean isFrozen() {
        return frozen;
    }
    
    @Override
    public @Nonnull @Frozen ReadOnlySet<E> freeze() {
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructors –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Collection –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public boolean isSingle() {
        return size() == 1;
    }
    
    @Pure
    @Override
    public @Nonnull FreezableIterator<E> iterator() {
        return new FreezableIterableIterator<>(this, super.iterator());
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Conditions –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public boolean containsNull() {
        for (final @Nullable E element : this) {
            if (element == null) return true;
        }
        return false;
    }
    
    @Pure
    @Override
    public boolean containsDuplicates() {
        return false;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Operations –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Override
    @NonFrozenRecipient
    public boolean add(@Nullable E element) {
        assert !isFrozen() : "This object is not frozen.";
        
        return super.add(element);
    }
    
    @Override
    @NonFrozenRecipient
    public boolean addAll(@Nonnull Collection<? extends E> c) {
        assert !isFrozen() : "This object is not frozen.";
        
        return super.addAll(c);
    }
    
    @Override
    @NonFrozenRecipient
    public boolean remove(@Nullable Object object) {
        assert !isFrozen() : "This object is not frozen.";
        
        return super.remove(object);
    }
    
    @Override
    @NonFrozenRecipient
    public boolean removeAll(@Nonnull Collection<?> c) {
        assert !isFrozen() : "This object is not frozen.";
        
        return super.removeAll(c);
    }
    
    @Override
    @NonFrozenRecipient
    public boolean retainAll(@Nonnull Collection<?> c) {
        assert !isFrozen() : "This object is not frozen.";
        
        return super.retainAll(c);
    }
    
    @Override
    @NonFrozenRecipient
    public void clear() {
        assert !isFrozen() : "This object is not frozen.";
        
        super.clear();
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– ReadOnlySet –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableSet<E> add(@Nonnull ReadOnlySet<E> set) {
        final @Nonnull FreezableSet<E> clone = clone();
        clone.addAll((FreezableSet<E>) set);
        return clone;
    }
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableSet<E> subtract(@Nonnull ReadOnlySet<E> set) {
        final @Nonnull FreezableSet<E> clone = clone();
        clone.removeAll((FreezableSet<E>) set);
        return clone;
    }
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableSet<E> intersect(@Nonnull ReadOnlySet<E> set) {
        final @Nonnull FreezableSet<E> clone = clone();
        clone.retainAll((FreezableSet<E>) set);
        return clone;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Conversions –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableLinkedHashSet<E> clone() {
        return new FreezableLinkedHashSet<>(this);
    }
    
    @Pure
    @Override
    @SuppressWarnings("unchecked")
    public @Capturable @Nonnull @NonFrozen FreezableArray<E> toFreezableArray() {
        return new FreezableArray<>(toArray((E[]) new Object[size()]));
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Object –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return IterableConverter.toString(this, Brackets.CURLY);
    }
    
}
