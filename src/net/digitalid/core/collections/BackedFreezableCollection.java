package net.digitalid.core.collections;

import java.util.Collection;
import java.util.HashSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.NonFrozenRecipient;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.interfaces.Freezable;
import net.digitalid.core.annotations.Immutable;

/**
 * This class implements a {@link Collection collection} that can be {@link Freezable frozen}.
 * As a consequence, all modifying methods may fail with an {@link AssertionError}.
 * The implementation is backed by an ordinary {@link Collection collection}. 
 * <p>
 * <em>Important:</em> Only use freezable or immutable types for the elements!
 * (The type is not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
class BackedFreezableCollection<E> implements FreezableCollection<E> {
    
    /**
     * Stores a reference to the underlying freezable.
     */
    private final @Nonnull Freezable freezable;
    
    /**
     * Stores a reference to the underlying collection.
     */
    private final @Nonnull Collection<E> collection;
    
    /**
     * Creates a new backed freezable collection.
     * 
     * @param freezable a reference to the underlying freezable.
     * @param collection a reference to the underlying collection.
     */
    protected BackedFreezableCollection(@Nonnull Freezable freezable, @Nonnull Collection<E> collection) {
        this.freezable = freezable;
        this.collection = collection;
    }
    
    
    @Pure
    @Override
    public boolean isFrozen() {
        return freezable.isFrozen();
    }
    
    @Pure
    @Override
    public boolean isNotFrozen() {
        return freezable.isNotFrozen();
    }
    
    @Override
    public @Nonnull ReadOnlyCollection<E> freeze() {
        freezable.freeze();
        return this;
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        return collection.equals(object);
    }
    
    @Pure
    @Override
    public int hashCode() {
        return collection.hashCode();
    }
    
    
    @Pure
    @Override
    public int size() {
        return collection.size();
    }
    
    @Pure
    @Override
    public boolean isEmpty() {
        return collection.isEmpty();
    }
    
    @Pure
    @Override
    public boolean isNotEmpty() {
        return !collection.isEmpty();
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
    public boolean contains(@Nullable Object object) {
        return collection.contains(object);
    }
    
    @Pure
    @Override
    public @Capturable @Nonnull Object[] toArray() {
        return collection.toArray();
    }
    
    @Pure
    @Override
    public @Capturable @Nonnull <T> T[] toArray(@Nonnull T[] array) {
        return collection.toArray(array);
    }
    
    @Pure
    @Override
    public boolean containsAll(@Nonnull Collection<?> c) {
        return collection.containsAll(c);
    }
    
    @Pure
    @Override
    public @Nonnull FreezableIterator<E> iterator() {
        return new FreezableIterableIterator<>(this, collection.iterator());
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
        final @Nonnull HashSet<E> set = new HashSet<>(size());
        for (final @Nullable E element : this) {
            if (set.contains(element)) return false;
            else set.add(element);
        }
        return true;
    }
    
    
    @Override
    @NonFrozenRecipient
    public boolean add(@Nullable E element) {
        assert isNotFrozen() : "This object is not frozen.";
        
        return collection.add(element);
    }
    
    @Override
    @NonFrozenRecipient
    public boolean addAll(@Nonnull Collection<? extends E> c) {
        assert isNotFrozen() : "This object is not frozen.";
        
        return collection.addAll(c);
    }
    
    @Override
    @NonFrozenRecipient
    public boolean remove(@Nullable Object object) {
        assert isNotFrozen() : "This object is not frozen.";
        
        return collection.remove(object);
    }
    
    @Override
    @NonFrozenRecipient
    public boolean removeAll(@Nonnull Collection<?> c) {
        assert isNotFrozen() : "This object is not frozen.";
        
        return collection.removeAll(c);
    }
    
    @Override
    @NonFrozenRecipient
    public boolean retainAll(@Nonnull Collection<?> c) {
        assert isNotFrozen() : "This object is not frozen.";
        
        return collection.retainAll(c);
    }
    
    @Override
    @NonFrozenRecipient
    public void clear() {
        assert isNotFrozen() : "This object is not frozen.";
        
        collection.clear();
    }
    
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableCollection<E> clone() {
        return new FreezableArrayList<>(collection);
    }
    
    @Pure
    @Override
    @SuppressWarnings("unchecked")
    public @Capturable @Nonnull FreezableArray<E> toFreezableArray() {
        return new FreezableArray<>(toArray((E[]) new Object[size()]));
    }
    
}
