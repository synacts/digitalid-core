package net.digitalid.core.collections;

import java.util.Collection;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Immutable;

/**
 * This interface models a {@link Collection collection} that can be {@link Freezable frozen}.
 * As a consequence, all modifying methods may fail with an {@link AssertionError}.
 * <p>
 * <em>Important:</em> Only use freezable or immutable types for the elements!
 * (The type is not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @see FreezableList
 * @see FreezableSet
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface FreezableCollection<E> extends ReadOnlyCollection<E>, Collection<E>, FreezableIterable<E> {
    
    @Override
    public @Nonnull ReadOnlyCollection<E> freeze();
    
}
