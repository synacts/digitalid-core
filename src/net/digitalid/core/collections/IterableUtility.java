package net.digitalid.core.collections;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Stateless;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.0
 */
@Stateless
public final class IterableUtility {
    
    public static @Nonnull <E> String convertToString(@Nonnull Iterable<E> iterable, @Nonnull Bracket bracket, @Nonnull StringConverter<E> stringifier) {
        return "TODO";
    }
    
}
