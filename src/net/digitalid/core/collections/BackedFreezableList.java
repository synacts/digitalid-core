package net.digitalid.core.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Frozen;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.NonFrozenRecipient;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.ValidIndex;
import net.digitalid.core.annotations.ValidIndexForInsertion;

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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Field –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores a reference to the list.
     */
    private final @Nonnull List<E> list;
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new freezable sublist.
     * 
     * @param freezable a reference to the underlying freezable.
     * @param list a reference to the underlying list.
     */
    BackedFreezableList(@Nonnull Freezable freezable, @Nonnull List<E> list) {
        super(freezable, list);
        
        this.list = list;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Freezable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Override
    public @Nonnull @Frozen ReadOnlyList<E> freeze() {
        super.freeze();
        return this;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– List –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Nullable E get(@ValidIndex int index) {
        return list.get(index);
    }
    
    @Pure
    @Override
    public @Nullable E getNullable(@ValidIndex int index) {
        return list.get(index);
    }
    
    @Pure
    @Override
    public boolean isNull(@ValidIndex int index) {
        return get(index) == null;
    }
    
    @Pure
    @Override
    public @Nonnull E getNonNullable(@ValidIndex int index) {
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
    public @Nonnull FreezableListIterator<E> listIterator(@ValidIndexForInsertion int index) {
        return new FreezableListIterator<>(this, list.listIterator(index));
    }
    
    @Pure
    @Override
    public @Nonnull FreezableList<E> subList(@ValidIndex int fromIndex, @ValidIndexForInsertion int toIndex) {
        return new BackedFreezableList<>(this, list.subList(fromIndex, toIndex));
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Operations –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Override
    @NonFrozenRecipient
    public @Nullable E set(@ValidIndex int index, @Nullable E element) {
        assert !isFrozen() : "This object is not frozen.";
        
        return list.set(index, element);
    }
    
    @Override
    @NonFrozenRecipient
    public void add(@ValidIndexForInsertion int index, @Nullable E element) {
        assert !isFrozen() : "This object is not frozen.";
        
        list.add(index, element);
    }
    
    @Override
    @NonFrozenRecipient
    public @Nullable E remove(@ValidIndex int index) {
        assert !isFrozen() : "This object is not frozen.";
        
        return list.remove(index);
    }
    
    @Override
    @NonFrozenRecipient
    public boolean addAll(@ValidIndexForInsertion int index, @Nonnull Collection<? extends E> collection) {
        assert !isFrozen() : "This object is not frozen.";
        
        return list.addAll(index, collection);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Conditions –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Ordering –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Cloneable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableList<E> clone() {
        return new FreezableArrayList<>(list);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Object –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return IterableConverter.toString(this, Brackets.SQUARE);
    }
    
}
