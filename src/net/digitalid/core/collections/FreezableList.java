package net.digitalid.core.collections;

import java.util.List;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Frozen;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.ValidIndex;
import net.digitalid.core.annotations.ValidIndexForInsertion;

/**
 * This interface models a {@link List list} that can be {@link Freezable frozen}.
 * As a consequence, all modifying methods may fail with an {@link AssertionError}.
 * <p>
 * <em>Important:</em> Only use freezable or immutable types for the elements!
 * (The type is not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @see FreezableArrayList
 * @see FreezableLinkedList
 * @see BackedFreezableList
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface FreezableList<E> extends ReadOnlyList<E>, List<E>, FreezableCollection<E> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Freezable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Override
    public @Nonnull @Frozen ReadOnlyList<E> freeze();
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– List –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Nonnull FreezableListIterator<E> listIterator();
    
    @Pure
    @Override
    public @Nonnull FreezableListIterator<E> listIterator(@ValidIndexForInsertion int index);
    
    @Pure
    @Override
    public @Nonnull FreezableList<E> subList(@ValidIndex int fromIndex, @ValidIndexForInsertion int toIndex);
    
}
