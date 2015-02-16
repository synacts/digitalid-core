package ch.virtualid.collections;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Freezable;
import ch.virtualid.interfaces.Immutable;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * This class implements a {@link Set set} that can be {@link Freezable frozen}.
 * As a consequence, all modifying methods may fail with an {@link AssertionError}.
 * The implementation is backed by an ordinary {@link Set set}. 
 * <p>
 * <em>Important:</em> Only use freezable or immutable types for the elements!
 * (The type is not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
class BackedFreezableSet<E> extends BackedFreezableCollection<E> implements FreezableSet<E> {
    
    /**
     * Stores a reference to the set.
     */
    private final @Nonnull Set<E> set;
    
    /**
     * Creates a new backed freezable set.
     * 
     * @param freezable a reference to the underlying freezable.
     * @param set a reference to the underlying set.
     */
    protected BackedFreezableSet(@Nonnull Freezable freezable, @Nonnull Set<E> set) {
        super(freezable, set);
        
        this.set = set;
    }
    
    @Override
    public @Nonnull ReadonlySet<E> freeze() {
        super.freeze();
        return this;
    }
    
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableSet<E> add(ReadonlySet<E> set) {
        final @Nonnull FreezableSet<E> clone = clone();
        clone.addAll((FreezableSet<E>) set);
        return clone;
    }
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableSet<E> subtract(ReadonlySet<E> set) {
        final @Nonnull FreezableSet<E> clone = clone();
        clone.removeAll((FreezableSet<E>) set);
        return clone;
    }
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableSet<E> intersect(ReadonlySet<E> set) {
        final @Nonnull FreezableSet<E> clone = clone();
        clone.retainAll((FreezableSet<E>) set);
        return clone;
    }
    
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableSet<E> clone() {
        return new FreezableHashSet<E>(set);
    }
    
}
