package net.digitalid.core.collections;

import java.util.Arrays;
import java.util.HashSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Captured;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.interfaces.Freezable;
import net.digitalid.core.interfaces.FreezableObject;
import net.digitalid.core.interfaces.Immutable;

/**
 * This class models {@link Freezable freezable} arrays.
 * <p>
 * <em>Important:</em> Only use freezable or immutable types for the elements!
 * (The type is not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public class FreezableArray<E> extends FreezableObject implements ReadonlyArray<E>, FreezableIterable<E> {
    
    /**
     * Stores the elements in an array.
     */
    private final @Nonnull E[] array;
    
    /**
     * Creates a new freezable array with the given size.
     * 
     * @param size the size of the newly created array.
     */
    @SuppressWarnings("unchecked")
    public FreezableArray(int size) {
        array = (E[]) new Object[size];
    }
    
    /**
     * Creates a new freezable array from the given array.
     * 
     * @param array the elements of the new array.
     */
    @SafeVarargs
    public FreezableArray(@Captured @Nonnull E... array) {
        this.array = array;
    }
    
    
    @Override
    public @Nonnull ReadonlyArray<E> freeze() {
        if (isNotFrozen()) {
            super.freeze();
            for (final @Nullable E element : array) {
                if (element instanceof Freezable) {
                    ((Freezable) element).freeze();
                } else {
                    break;
                }
            }
        }
        return this;
    }
    
    
    @Pure
    @Override
    public int size() {
        return array.length;
    }
    
    @Pure
    @Override
    public @Nullable E get(int index) {
        assert index >= 0 && index < size() : "The index is valid.";
        
        return array[index];
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
     * Sets the element at the given index to the new value.
     * 
     * @param index the index of the element to be set.
     * @param element the new value to replace the element with.
     * 
     * @require isNotFrozen() : "This object is not frozen.";
     * @require index >= 0 && index < size() : "The index is valid.";
     */
    public void set(int index, @Nullable E element) {
        assert isNotFrozen() : "This object is not frozen.";
        assert index >= 0 && index < size() : "The index is valid.";
        
        array[index] = element;
    }
    
    @Pure
    @Override
    public @Nonnull FreezableArrayIterator<E> iterator() {
        return new FreezableArrayIterator<>(this);
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
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null) return false;
        if (object instanceof FreezableArray) return Arrays.equals(array, ((FreezableArray) object).array);
        if (object instanceof Object[]) return Arrays.equals(array, (Object[]) object);
        return false;
    }
    
    @Pure
    @Override
    public int hashCode() {
        return Arrays.deepHashCode(array);
    }
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableArray<E> clone() {
        return new FreezableArray<>(array.clone());
    }
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableList<E> toFreezableList() {
        final @Nonnull FreezableList<E> freezableList = new FreezableArrayList<>(array.length);
        for (final @Nullable E element : array) freezableList.add(element);
        return freezableList;
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        final @Nonnull StringBuilder string = new StringBuilder("[");
        for (final @Nullable E element : array) {
            if (string.length() != 1) string.append(", ");
            string.append(element == null ? "null" : element.toString());
        }
        string.append("]");
        return string.toString();
    }
    
}
