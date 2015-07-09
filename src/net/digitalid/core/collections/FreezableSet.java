package net.digitalid.core.collections;

import java.util.Set;
import javax.annotation.Nonnull;
import net.digitalid.core.interfaces.Freezable;
import net.digitalid.core.interfaces.Immutable;

/**
 * This interface models a {@link Set set} that can be {@link Freezable frozen}.
 * As a consequence, all modifying methods may fail with an {@link AssertionError}.
 * <p>
 * <em>Important:</em> Only use freezable or immutable types for the elements!
 * (The type is not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface FreezableSet<E> extends ReadOnlySet<E>, Set<E>, FreezableCollection<E> {
    
    @Override
    public @Nonnull ReadOnlySet<E> freeze();
    
}
