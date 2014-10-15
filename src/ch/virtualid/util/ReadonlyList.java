package ch.virtualid.util;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Freezable;
import ch.virtualid.interfaces.Immutable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This interface provides readonly access to {@List lists} and should <em>never</em> be cast away (unless external code requires it).
 * <p>
 * <em>Important:</em> Only use freezable or immutable types for the elements!
 * (The type is not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @see FreezableList
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface ReadonlyList<E> extends ReadonlyCollection<E> {
    
    /**
     * @see List#get(int)
     */
    @Pure
    public @Nullable E get(int index);
    
    /**
     * Returns whether the element at the given index is null.
     * 
     * @param index the index of the element to be checked.
     * 
     * @return whether the element at the given index is null.
     * 
     * @require index >= 0 && index < size() : "The index is valid.";
     */
    @Pure
    public boolean isNull(int index);
    
    /**
     * Returns whether the element at the given index is not null.
     * 
     * @param index the index of the element to be checked.
     * 
     * @return whether the element at the given index is not null.
     * 
     * @require index >= 0 && index < size() : "The index is valid.";
     */
    @Pure
    public boolean isNotNull(int index);
    
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
    
    /**
     * @see List#indexOf(java.lang.Object)
     */
    @Pure
    public int indexOf(@Nullable Object object);
    
    /**
     * @see List#lastIndexOf(java.lang.Object)
     */
    @Pure
    public int lastIndexOf(@Nullable Object object);
    
    /**
     * @see List#listIterator()
     */
    @Pure
    public @Nonnull ReadonlyListIterator<E> listIterator();
    
    /**
     * @see List#listIterator(int)
     */
    @Pure
    public @Nonnull ReadonlyListIterator<E> listIterator(int index);
    
    /**
     * @see List#subList(int, int)
     */
    @Pure
    public @Nonnull ReadonlyList<E> subList(int fromIndex, int toIndex);
    
    
    /**
     * Returns whether the elements in this list are ascending (excluding null values).
     * 
     * @return {@code true} if the elements in this list are ascending, {@code false} otherwise.
     */
    @Pure
    public boolean isAscending();
    
    /**
     * Returns whether the elements in this list are strictly ascending (excluding null values).
     * 
     * @return {@code true} if the elements in this list are strictly ascending, {@code false} otherwise.
     */
    @Pure
    public boolean isStrictlyAscending();
    
    /**
     * Returns whether the elements in this list are descending (excluding null values).
     * 
     * @return {@code true} if the elements in this list are descending, {@code false} otherwise.
     */
    @Pure
    public boolean isDescending();
    
    /**
     * Returns whether the elements in this list are strictly descending (excluding null values).
     * 
     * @return {@code true} if the elements in this list are strictly descending, {@code false} otherwise.
     */
    @Pure
    public boolean isStrictlyDescending();
    
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableList<E> clone();
    
}
