package ch.virtualid.util;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Freezable;
import ch.virtualid.interfaces.Immutable;
import java.util.Collection;
import javax.annotation.Nonnull;

/**
 * This interface provides readonly access to {@link Collection collections} and should <em>never</em> be cast away (unless external code requires it).
 * <p>
 * <em>Important:</em> Only use freezable or immutable types for the elements!
 * (The type is not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @see FreezableCollection
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface ReadonlyCollection<E> extends ReadonlyIterable<E> {
    
    /**
     * @see Collection#size()
     */
    @Pure
    public int size();
    
    /**
     * @see Collection#isEmpty()
     */
    @Pure
    public boolean isEmpty();
    
    /**
     * Returns whether this collection is not empty.
     * 
     * @return whether this collection is not empty.
     */
    @Pure
    public boolean isNotEmpty();
    
    /**
     * @see Collection#contains(java.lang.Object)
     */
    @Pure
    public boolean contains(Object object);
    
    /**
     * @see Collection#toArray() 
     */
    @Pure
    public @Capturable @Nonnull Object[] toArray();
    
    /**
     * @see Collection#toArray(T[])
     */
    @Pure
    public @Capturable @Nonnull <T> T[] toArray(T[] array);
    
    /**
     * @see Collection#containsAll(java.util.Collection) 
     */
    @Pure
    public boolean containsAll(Collection<?> collection);
    
    
    /**
     * Returns whether this collection does not contain an element which is null.
     * 
     * @return {@code true} if this collection does not contain null, {@code false} otherwise.
     */
    @Pure
    public boolean doesNotContainNull();
    
    /**
     * Returns whether this collection does not contain duplicates (including null values).
     * 
     * @return {@code true} if this collection does not contain duplicates, {@code false} otherwise.
     */
    @Pure
    public boolean doesNotContainDuplicates();
    
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableCollection<E> clone();
    
    /**
     * Returns the elements of this collection in a freezable array.
     * 
     * @return the elements of this collection in a freezable array.
     */
    @Pure
    public @Capturable @Nonnull FreezableArray<E> toFreezableArray();
    
}
