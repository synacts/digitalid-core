package net.digitalid.service.core.converter;

import javax.annotation.Nonnull;
import net.digitalid.database.core.converter.AbstractSQLConverter;
import net.digitalid.service.core.converter.xdf.AbstractNonRequestingXDFConverter;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

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
    public @Nonnull AbstractNonRequestingXDFConverter<O, E> getXDFConverter() {
        return (AbstractNonRequestingXDFConverter<O, E>) super.getXDFConverter();
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new object with the given converters.
     * 
     * @param XDFConverter the XDF converter.
     * @param SQLConverter the SQL converter.
     */
    protected NonRequestingConverters(@Nonnull AbstractNonRequestingXDFConverter<O, E> XDFConverter, @Nonnull AbstractSQLConverter<O, E> SQLConverter) {
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
    public static @Nonnull <O, E> NonRequestingConverters<O, E> get(@Nonnull AbstractNonRequestingXDFConverter<O, E> XDFConverter, @Nonnull AbstractSQLConverter<O, E> SQLConverter) {
        return new NonRequestingConverters<>(XDFConverter, SQLConverter);
    }
    
    /* -------------------------------------------------- Subtyping -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull NonRequestingConverters<O, E> setType(@Nonnull SemanticType type) {
        return new NonRequestingConverters<>(getXDFConverter().setType(type), getSQLConverter());
    }
    
}
