package net.digitalid.core.collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.Stateless;

/**
 * This class converts iterables to strings.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Stateless
public final class IterableConverter {
    
    /**
     * Converts the given iterable to a string.
     * 
     * @param iterable the iterable to convert to a string.
     * @param converter the converter applied to each element.
     * @param brackets the brackets used to embrace the list.
     * @param delimiter the delimiter between elements.
     * 
     * @return the given iterable as a string.
     */
    @Pure
    public static @Nonnull <E> String toString(@Nonnull Iterable<E> iterable, @Nonnull ElementConverter<? super E> converter, @Nullable Brackets brackets, @Nonnull String delimiter) {
        final @Nonnull StringBuilder string = new StringBuilder();
        if (brackets != null) string.append(brackets.getOpening());
        for (final @Nonnull E element : iterable) {
            if (brackets == null && string.length() > 0 || brackets != null && string.length() > 1) string.append(delimiter);
            string.append(converter.toString(element));
        }
        if (brackets != null) string.append(brackets.getClosing());
        return string.toString();
    }
    
    /**
     * Converts the given iterable to a string.
     * 
     * @param iterable the iterable to convert to a string.
     * @param converter the converter applied to each element.
     * @param brackets the brackets used to embrace the list.
     * 
     * @return the given iterable as a string.
     */
    @Pure
    public static @Nonnull <E> String toString(@Nonnull Iterable<E> iterable, @Nonnull ElementConverter<? super E> converter, @Nullable Brackets brackets) {
        return toString(iterable, converter, brackets, ", ");
    }
    
    /**
     * Converts the given iterable to a string.
     * 
     * @param iterable the iterable to convert to a string.
     * @param converter the converter applied to each element.
     * 
     * @return the given iterable as a string.
     */
    @Pure
    public static @Nonnull <E> String toString(@Nonnull Iterable<E> iterable, @Nonnull ElementConverter<? super E> converter) {
        return toString(iterable, converter, null);
    }
    
    /**
     * Converts the given iterable to a string.
     * 
     * @param iterable the iterable to convert to a string.
     * @param brackets the brackets used to embrace the list.
     * 
     * @return the given iterable as a string.
     */
    @Pure
    public static @Nonnull <E> String toString(@Nonnull Iterable<E> iterable, @Nullable Brackets brackets) {
        return toString(iterable, ElementConverter.DEFAULT, brackets);
    }
    
    /**
     * Converts the given iterable to a string.
     * 
     * @param iterable the iterable to convert to a string.
     * 
     * @return the given iterable as a string.
     */
    @Pure
    public static @Nonnull <E> String toString(@Nonnull Iterable<E> iterable) {
        return toString(iterable, (Brackets) null);
    }
    
}
