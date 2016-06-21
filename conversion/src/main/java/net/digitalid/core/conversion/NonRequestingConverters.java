package net.digitalid.core.conversion;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.core.converter.sql.SQLConverter;

import net.digitalid.core.conversion.xdf.NonRequestingXDFConverter;
import net.digitalid.core.identification.identity.SemanticType;

/**
 * This class allows to store several non-requesting converters in a single object.
 * 
 * @param <O> the type of the objects that the converters can convert, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to recover an object, which is quite often an {@link Entity}.
 *            In case no external information is needed for the recovery of an object, declare it as an {@link Object}.
 */
@Immutable
public final class NonRequestingConverters<O, E> extends Converters<O, E> {
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull NonRequestingXDFConverter<O, E> getXDFConverter() {
        return (NonRequestingXDFConverter<O, E>) super.getXDFConverter();
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new object with the given converters.
     * 
     * @param XDFConverter the XDF converter.
     * @param SQLConverter the SQL converter.
     */
    protected NonRequestingConverters(@Nonnull NonRequestingXDFConverter<O, E> XDFConverter, @Nonnull SQLConverter<O, E> SQLConverter) {
        super(XDFConverter, SQLConverter);
    }
    
    /**
     * Creates a new object with the given converters.
     * 
     * @param XDFConverter the XDF converter.
     * @param SQLConverter the SQL converter.
     * 
     * @return a new object with the given converters.
     */
    @Pure
    public static @Nonnull <O, E> NonRequestingConverters<O, E> get(@Nonnull NonRequestingXDFConverter<O, E> XDFConverter, @Nonnull SQLConverter<O, E> SQLConverter) {
        return new NonRequestingConverters<>(XDFConverter, SQLConverter);
    }
    
    /* -------------------------------------------------- Subtyping -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull NonRequestingConverters<O, E> setType(@Nonnull SemanticType type) {
        return new NonRequestingConverters<>(getXDFConverter().setType(type), getSQLConverter());
    }
    
}
