package ch.virtualid.util;

import ch.virtualid.annotation.Capturable;
import ch.virtualid.annotation.Pure;
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
    public @Capturable @Nonnull FreezableSet<E> clone() {
        return new FreezableHashSet<E>(set);
    }
    
}
