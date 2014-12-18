package ch.virtualid.util;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Freezable;
import ch.virtualid.interfaces.Immutable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class extends the {@link LinkedList} and makes it {@link Freezable}.
 * Be careful when treating instances of this class as normal {@link List lists} because all modifying methods may fail with an {@link AssertionError}.
 * <p>
 * <em>Important:</em> Only use freezable or immutable types for the elements!
 * (The type is not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public class FreezableLinkedList<E> extends LinkedList<E> implements FreezableList<E> {
    
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
    public @Nonnull ReadonlyList<E> freeze() {
        if (!frozen) {
            frozen = true;
            for (@Nullable E element : this) {
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
     * @see LinkedList#LinkedList()
     */
    public FreezableLinkedList() {
        super();
    }
    
    /**
     * Creates a new freezable linked list with the given element.
     * 
     * @param element the element to add to the newly created list.
     */
    public FreezableLinkedList(@Nullable E element) {
        this();
        add(element);
    }
    
    /**
     * Creates a new freezable linked list with the given elements.
     * 
     * @param elements the elements to add to the newly created list.
     */
    @SuppressWarnings("unchecked")
    public FreezableLinkedList(@Nonnull E... elements) {
        this();
        addAll(Arrays.asList(elements));
    }
    
    /**
     * @see LinkedList#LinkedList(java.util.Collection)
     */
    public FreezableLinkedList(@Nonnull Collection<? extends E> collection) {
        super(collection);
    }
    
    
    @Pure
    @Override
    public boolean isNotEmpty() {
        return !super.isEmpty();
    }
    
    
    @Pure
    @Override
    public boolean isNull(int index) {
        return get(index) == null;
    }
    
    @Pure
    @Override
    public boolean isNotNull(int index) {
        return get(index) != null;
    }
    
    @Pure
    @Override
    public @Nonnull E getNotNull(int index) {
        @Nullable E element = get(index);
        assert element != null : "The element at the given index is not null.";
        
        return element;
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
    public void add(int index, @Nullable E element) {
        assert isNotFrozen() : "This object is not frozen.";
        
        super.add(index, element);
    }
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public void addFirst(@Nullable E element) {
        assert isNotFrozen() : "This object is not frozen.";
        
        super.addFirst(element);
    }
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public void addLast(@Nullable E element) {
        assert isNotFrozen() : "This object is not frozen.";
        
        super.addLast(element);
    }
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public boolean addAll(@Nonnull Collection<? extends E> collection) {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.addAll(collection);
    }
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public boolean addAll(int index, @Nonnull Collection<? extends E> collection) {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.addAll(index, collection);
    }
    
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public boolean offer(@Nullable E element) {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.offer(element);
    }
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public boolean offerFirst(@Nullable E element) {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.offerFirst(element);
    }
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public boolean offerLast(@Nullable E element) {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.offerLast(element);
    }
    
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public @Nullable E remove() {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.remove();
    }
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public @Nullable E removeFirst() {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.removeFirst();
    }
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public @Nullable E removeLast() {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.removeLast();
    }
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public @Nullable E remove(int index) {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.remove(index);
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
    public boolean removeFirstOccurrence(@Nullable Object object) {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.removeFirstOccurrence(object);
    }
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public boolean removeLastOccurrence(@Nullable Object object) {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.removeLastOccurrence(object);
    }
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        assert isNotFrozen() : "This object is not frozen.";
        
        super.removeRange(fromIndex, toIndex);
    }
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public boolean removeAll(@Nonnull Collection<?> collection) {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.removeAll(collection);
    }
    
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public @Nullable E poll() {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.poll();
    }
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public @Nullable E pollFirst() {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.pollFirst();
    }
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public @Nullable E pollLast() {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.pollLast();
    }
    
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public void push(@Nullable E element) {
        assert isNotFrozen() : "This object is not frozen.";
        
        super.push(element);
    }
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public @Nullable E pop() {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.pop();
    }
    
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public @Nullable E set(int index, @Nullable E element) {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.set(index, element);
    }
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public void clear() {
        assert isNotFrozen() : "This object is not frozen.";
        
        super.clear();
    }
    
    /**
     * @require isNotFrozen() : "This object is not frozen.";
     */
    @Override
    public boolean retainAll(@Nonnull Collection<?> collection) {
        assert isNotFrozen() : "This object is not frozen.";
        
        return super.retainAll(collection);
    }
    
    
    @Pure
    @Override
    public @Nonnull FreezableListIterator<E> listIterator(int index) {
        return new FreezableListIterator<E>(this, super.listIterator(index));
    }
    
    @Pure
    @Override
    public @Nonnull FreezableListIterator<E> listIterator() {
        return new FreezableListIterator<E>(this, super.listIterator());
    }
    
    @Pure
    @Override
    public @Nonnull FreezableIterator<E> iterator() {
        return new FreezableIterableIterator<E>(this, super.iterator());
    }
    
    @Pure
    @Override
    public @Nonnull FreezableIterator<E> descendingIterator() {
        return new FreezableIterableIterator<E>(this, super.descendingIterator());
    }
    
    @Pure
    @Override
    public @Nonnull FreezableList<E> subList(int fromIndex, int toIndex) {
        return new BackedFreezableList<E>(this, super.subList(fromIndex, toIndex));
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
        final @Nonnull HashSet<E> set = new HashSet<E>(size());
        for (final @Nullable E element : this) {
            if (set.contains(element)) return false;
            else set.add(element);
        }
        return true;
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
    public @Capturable @Nonnull FreezableLinkedList<E> clone() {
        return new FreezableLinkedList<E>(this);
    }
    
    @Pure
    @Override
    @SuppressWarnings("unchecked")
    public @Capturable @Nonnull FreezableArray<E> toFreezableArray() {
        return new FreezableArray<E>(toArray((E[]) new Object[size()]));
    }
    
}
