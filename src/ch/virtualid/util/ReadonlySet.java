package ch.virtualid.util;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Freezable;
import ch.virtualid.interfaces.Immutable;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * This interface provides readonly access to {@link Set sets} and should <em>never</em> be cast away (unless external code requires it).
 * <p>
 * <em>Important:</em> Only use freezable or immutable types for the elements!
 * (The type is not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @see FreezableSet
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface ReadonlySet<E> extends ReadonlyCollection<E> {
    
    /**
     * Returns the union of this and the given set.
     * 
     * @param set the set whose elements are added.
     * 
     * @return the union of this and the given set.
     */
    @Pure
    public @Capturable @Nonnull FreezableSet<E> add(ReadonlySet<E> set);
    
    /**
     * Returns the relative complement of the given set in this set.
     * 
     * @param set the set whose elements are subtracted.
     * 
     * @return the relative complement of the given set in this set.
     */
    @Pure
    public @Capturable @Nonnull FreezableSet<E> subtract(ReadonlySet<E> set);
    
    /**
     * Returns the intersection of this and the given set.
     * 
     * @param set the set whose elements are intersected.
     * 
     * @return the intersection of this and the given set.
     */
    @Pure
    public @Capturable @Nonnull FreezableSet<E> intersect(ReadonlySet<E> set);
    
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableSet<E> clone();
    
}
