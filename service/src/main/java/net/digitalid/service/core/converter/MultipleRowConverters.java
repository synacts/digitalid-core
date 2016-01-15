package net.digitalid.service.core.converter;

import javax.annotation.Nonnull;
import net.digitalid.database.core.converter.sql.MultipleRowSQLConverter;
import net.digitalid.service.core.converter.xdf.RequestingXDFConverter;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;

/**
 * This class allows to store several multiple-row converters in a single object.
 * 
 * @param <O> the type of the objects that the converters can convert, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to recover an object, which is quite often an {@link Entity}.
 *            In case no external information is needed for the recovery of an object, declare it as an {@link Object}.
 */
@Immutable
public final class MultipleRowConverters<O, E> extends Converters<O, E> {
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull MultipleRowSQLConverter<O, E> getSQLConverter() {
        return (MultipleRowSQLConverter<O, E>) super.getSQLConverter();
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new object with the given converters.
     * 
     * @param XDFConverter the XDF converter.
     * @param SQLConverter the SQL converter.
     */
    protected MultipleRowConverters(@Nonnull RequestingXDFConverter<O, E> XDFConverter, @Nonnull MultipleRowSQLConverter<O, E> SQLConverter) {
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
    public static @Nonnull <O, E> MultipleRowConverters<O, E> get(@Nonnull RequestingXDFConverter<O, E> XDFConverter, @Nonnull MultipleRowSQLConverter<O, E> SQLConverter) {
        return new MultipleRowConverters<>(XDFConverter, SQLConverter);
    }
    
    /* -------------------------------------------------- Subtyping -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull MultipleRowConverters<O, E> setType(@Nonnull SemanticType type) {
        return new MultipleRowConverters<>(getXDFConverter().setType(type), getSQLConverter());
    }
    
}
