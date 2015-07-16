package net.digitalid.core.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonFrozenRecipient;
import net.digitalid.core.annotations.Pure;

/**
 * This class implements a {@link Set set} that can be {@link Freezable frozen}.
 * As a consequence, all modifying methods may fail with an {@link AssertionError}.
 * The implementation is backed by an ordinary {@link List list}. 
 * <p>
 * <em>Important:</em> Only use freezable or immutable types for the elements!
 * (The type is not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
class BackedFreezableList<E> extends BackedFreezableCollection<E> implements FreezableList<E> {
    
    /**
     * Stores a reference to the list.
     */
    private final @Nonnull List<E> list;
    
    /**
     * Creates a new freezable sublist.
     * 
     * @param freezable a reference to the underlying freezable.
     * @param list a reference to the underlying list.
     */
    protected BackedFreezableList(@Nonnull Freezable freezable, @Nonnull List<E> list) {
        super(freezable, list);
        
        this.list = list;
    }
    
    @Override
    public @Nonnull ReadOnlyList<E> freeze() {
        super.freeze();
        return this;
    }
    
    
    @Pure
    @Override
    public @Nullable E get(int index) {
        return list.get(index);
    }
    
    @Pure
    @Override
    public boolean isNull(int index) {
        return get(index) == null;
    }
    
    @Pure
    @Override
    public @Nonnull E getNotNull(int index) {
        final @Nullable E element = get(index);
        assert element != null : "The element at the given index is not null.";
        
        return element;
    }
    
    @Pure
    @Override
    public int indexOf(@Nullable Object object) {
        return list.indexOf(object);
    }
    
    @Pure
    @Override
    public int lastIndexOf(@Nullable Object object) {
        return list.lastIndexOf(object);
    }
    
    @Pure
    @Override
    public @Nonnull FreezableListIterator<E> listIterator() {
        return new FreezableListIterator<>(this, list.listIterator());
    }
    
    @Pure
    @Override
    public @Nonnull FreezableListIterator<E> listIterator(int index) {
        return new FreezableListIterator<>(this, list.listIterator(index));
    }
    
    @Pure
    @Override
    public @Nonnull FreezableList<E> subList(int fromIndex, int toIndex) {
        return new BackedFreezableList<>(this, list.subList(fromIndex, toIndex));
    }

    
    @Override
    @NonFrozenRecipient
    public @Nullable E set(int index, @Nullable E element) {
        assert !isFrozen() : "This object is not frozen.";
        
        return list.set(index, element);
    }
    
    @Override
    @NonFrozenRecipient
    public void add(int index, @Nullable E element) {
        assert !isFrozen() : "This object is not frozen.";
        
        list.add(index, element);
    }
    
    @Override
    @NonFrozenRecipient
    public E remove(int index) {
        assert !isFrozen() : "This object is not frozen.";
        
        return list.remove(index);
    }
    
    @Override
    @NonFrozenRecipient
    public boolean addAll(int index, @Nonnull Collection<? extends E> collection) {
        assert !isFrozen() : "This object is not frozen.";
        
        return list.addAll(index, collection);
    }
    
    
    @Pure
    @Override
    public boolean containsNull() {
        for (final @Nullable Object element : this) {
            if (element == null) return true;
        }
        return false;
    }
    
    @Pure
    @Override
    public boolean containsDuplicates() {
        final @Nonnull HashSet<E> set = new HashSet<>(size());
        for (final @Nullable E element : this) {
            if (set.contains(element)) return true;
            else set.add(element);
        }
        return false;
    }
    
    
    /**
     * Returns whether the elements in this list are ordered (excluding null values).
     * 
     * @param strictly whether the ordering is strict (i.e. without equal values).
     * @param ascending whether the ordering is ascending (true) or descending (false).
     * 
     * @return {@code true} if the elements in this list are ordered, {@code false} otherwise.
     */
    @Pure
    @SuppressWarnings("unchecked")
    private boolean isOrdered(boolean strictly, boolean ascending) {
        @Nullable E lastElement = null;
        for (final @Nullable E element : this) {
            if (element == null) continue;
            if (lastElement != null) {
                if (element instanceof Comparable) {
                    if (((Comparable) element).compareTo(lastElement) * (ascending ? 1 : -1) < (strictly ? 1 : 0)) return false;
                }
            }
            lastElement = element;
        }
        return true;
    }
    
    @Pure
    @Override
    public boolean isAscending() {
        return isOrdered(false, true);
    }
    
    @Pure
    @Override
    public boolean isStrictlyAscending() {
        return isOrdered(true, true);
    }
    
    @Pure
    @Override
    public boolean isDescending() {
        return isOrdered(false, false);
    }
    
    @Pure
    @Override
    public boolean isStrictlyDescending() {
        return isOrdered(true, false);
    }
    
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableList<E> clone() {
        return new FreezableArrayList<>(list);
    }
    
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return IterableConverter.toString(this, Brackets.SQUARE);
    }
    
}
