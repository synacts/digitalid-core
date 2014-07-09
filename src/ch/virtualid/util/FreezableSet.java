package ch.virtualid.util;

import ch.virtualid.interfaces.Freezable;
import ch.virtualid.interfaces.Immutable;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * This interface models a {@link Set set} that can be {@link Freezable frozen}.
 * As a consequence, all modifying methods may fail with an {@link AssertionError}.
 * <p>
 * <em>Important:</em> Only use freezable or immutable types for the elements!
 * (The type is not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface FreezableSet<E> extends ReadonlySet<E>, Set<E>, FreezableCollection<E> {
    
    @Override
    public @Nonnull ReadonlySet<E> freeze();
    
}
