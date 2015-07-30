package net.digitalid.core.collections;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.ValidIndex;
import net.digitalid.core.annotations.ValidIndexForInsertion;

/**
 * This interface provides read-only access to {@link List lists} and should <em>never</em> be cast away (unless external code requires it).
 * <p>
 * <em>Important:</em> Only use freezable or immutable types for the elements!
 * (The type is not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @see FreezableList
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface ReadOnlyList<E> extends ReadOnlyCollection<E> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– List –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * @see List#get(int)
     */
    @Pure
    public @Nullable E getNullable(@ValidIndex int index);
    
    /**
     * Returns whether the element at the given index is null.
     * 
     * @param index the index of the element to be checked.
     * 
     * @return whether the element at the given index is null.
     */
    @Pure
    public boolean isNull(@ValidIndex int index);
    
    /**
     * Returns the element at the given index.
     * 
     * @param index the index of the element to be returned.
     * 
     * @return the element at the given index.
     * 
     * @require !isNull(index) : "The element at the given index is not null.";
     */
    @Pure
    public @Nonnull E getNonNullable(@ValidIndex int index);
    
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
    public @Nonnull ReadOnlyListIterator<E> listIterator();
    
    /**
     * @see List#listIterator(int)
     */
    @Pure
    public @Nonnull ReadOnlyListIterator<E> listIterator(@ValidIndexForInsertion int index);
    
    /**
     * @see List#subList(int, int)
     */
    @Pure
    public @Nonnull ReadOnlyList<E> subList(@ValidIndex int fromIndex, @ValidIndexForInsertion int toIndex);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Ordering –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Cloneable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableList<E> clone();
    
}
