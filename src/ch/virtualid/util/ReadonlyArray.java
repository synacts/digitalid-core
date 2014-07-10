package ch.virtualid.util;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Freezable;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This interface provides readonly access to arrays and should <em>never</em> be cast away.
 * <p>
 * <em>Important:</em> Only use freezable or immutable types for the elements!
 * (The type is not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @see FreezableArray
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface ReadonlyArray<E> extends ReadonlyIterable<E> {
    
    /**
     * Returns the size of this array.
     * 
     * @return the size of this array.
     */
    @Pure
    public int size();
    
    /**
     * Returns the element at the given index.
     * 
     * @param index the index of the element to be returned.
     * 
     * @return the element at the given index.
     * 
     * @require index >= 0 && index < size() : "The index is valid.";
     */
    @Pure
    public @Nullable E get(int index);
    
    /**
     * Returns the element at the given index.
     * 
     * @param index the index of the element to be returned.
     * 
     * @return the element at the given index.
     * 
     * @require index >= 0 && index < size() : "The index is valid.";
     * @require get(index) != null : "The element at the given index is not null.";
     */
    @Pure
    public @Nonnull E getNotNull(int index);
    
    @Pure
    @Override
    public @Nonnull ReadonlyArrayIterator<E> iterator();
    
    
    /**
     * Returns whether this array does not contain an element which is null.
     * If it does not, {@link #get(int)} is guaranteed to return not null for every valid index.
     * 
     * @return {@code true} if this array does not contain null, {@code false} otherwise.
     */
    @Pure
    public boolean doesNotContainNull();
    
    /**
     * Returns whether this array does not contain duplicates (including null values).
     * 
     * @return {@code true} if this array does not contain duplicates, {@code false} otherwise.
     */
    @Pure
    public boolean doesNotContainDuplicates();
    
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableArray<E> clone();
    
    /**
     * Returns the elements of this array in a freezable list.
     * 
     * @return the elements of this array in a freezable list.
     */
    @Pure
    public @Capturable @Nonnull FreezableList<E> toFreezableList();
    
}
