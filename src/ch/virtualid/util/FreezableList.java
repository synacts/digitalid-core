package ch.virtualid.util;

import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Freezable;
import ch.virtualid.interfaces.Immutable;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * This interface models a {@link List list} that can be {@link Freezable frozen}.
 * As a consequence, all modifying methods may fail with an {@link AssertionError}.
 * <p>
 * <em>Important:</em> Only use freezable or immutable types for the elements!
 * (The type is not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @see FreezableArrayList
 * @see FreezableLinkedList
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface FreezableList<E> extends ReadonlyList<E>, List<E>, FreezableCollection<E> {
    
    @Override
    public @Nonnull ReadonlyList<E> freeze();
    
    @Pure
    @Override
    public FreezableListIterator<E> listIterator();
    
    @Pure
    @Override
    public FreezableListIterator<E> listIterator(int index);
    
    @Pure
    @Override
    public FreezableList<E> subList(int fromIndex, int toIndex);
    
}
