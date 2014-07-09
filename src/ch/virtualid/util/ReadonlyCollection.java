package ch.virtualid.util;

import ch.virtualid.annotation.Capturable;
import ch.virtualid.annotation.Pure;
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
    public <T> @Capturable @Nonnull T[] toArray(T[] array);
    
    /**
     * @see Collection#containsAll(java.util.Collection) 
     */
    @Pure
    public boolean containsAll(Collection<?> collection);
    
    /**
     * @see Collection#containsAll(java.util.Collection) 
     */
    @Pure
    public boolean containsAll(ReadonlyCollection<?> collection);
    
    
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
