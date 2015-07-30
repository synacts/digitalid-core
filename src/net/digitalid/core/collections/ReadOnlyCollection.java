package net.digitalid.core.collections;

import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.Pure;

/**
 * This interface provides read-only access to {@link Collection collections} and should <em>never</em> be cast away (unless external code requires it).
 * <p>
 * <em>Important:</em> Only use freezable or immutable types for the elements!
 * (The type is not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @see FreezableCollection
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface ReadOnlyCollection<E> extends ReadOnlyIterable<E> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Collection –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
     * Returns whether this collection contains a single element.
     * 
     * @return whether this collection contains a single element.
     */
    @Pure
    public boolean isSingle();
    
    /**
     * @see Collection#contains(java.lang.Object)
     */
    @Pure
    public boolean contains(@Nullable Object object);
    
    /**
     * @see Collection#toArray() 
     */
    @Pure
    public @Capturable @Nonnull Object[] toArray();
    
    /**
     * @see Collection#toArray(Object[])
     */
    @Pure
    public @Capturable @Nonnull <T> T[] toArray(@Nonnull T[] array);
    
    /**
     * @see Collection#containsAll(java.util.Collection) 
     */
    @Pure
    public boolean containsAll(@Nonnull Collection<?> collection);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Conditions –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns whether this collection contains an element which is null.
     * 
     * @return {@code true} if this collection contains null, {@code false} otherwise.
     */
    @Pure
    public boolean containsNull();
    
    /**
     * Returns whether this collection contains duplicates (including null values).
     * 
     * @return {@code true} if this collection contains duplicates, {@code false} otherwise.
     */
    @Pure
    public boolean containsDuplicates();
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Conversions –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableCollection<E> clone();
    
    /**
     * Returns the elements of this collection in a freezable array.
     * 
     * @return the elements of this collection in a freezable array.
     */
    @Pure
    public @Capturable @Nonnull @NonFrozen FreezableArray<E> toFreezableArray();
    
}
