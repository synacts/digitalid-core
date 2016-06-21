package net.digitalid.core.conversion;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.core.converter.sql.SQLConverter;

import net.digitalid.core.conversion.xdf.RequestingXDFConverter;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.identification.identity.SemanticType;

/**
 * This class allows to store several converters in a single object.
 * 
 * @param <O> the type of the objects that the converters can convert, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to recover an object, which is quite often an {@link Entity}.
 *            In case no external information is needed for the recovery of an object, declare it as an {@link Object}.
 * 
 * @see NonRequestingConverters
 */
@Immutable
public class Converters<O, E> {
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * Stores the XDF converter.
     */
    private final @Nonnull RequestingXDFConverter<O, E> XDFConverter;
    
    /**
     * Returns the XDF converter.
     * 
     * @return the XDF converter.
     */
    @Pure
    public @Nonnull RequestingXDFConverter<O, E> getXDFConverter() {
        return XDFConverter;
    }
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the SQL converter.
     */
    private final @Nonnull SQLConverter<O, E> SQLConverter;
    
    /**
     * Returns the SQL converter.
     * 
     * @return the SQL converter.
     */
    @Pure
    public @Nonnull SQLConverter<O, E> getSQLConverter() {
        return SQLConverter;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new object with the given converters.
     * 
     * @param XDFConverter the XDF converter.
     * @param SQLConverter the SQL converter.
     */
    protected Converters(@Nonnull RequestingXDFConverter<O, E> XDFConverter, @Nonnull SQLConverter<O, E> SQLConverter) {
        this.XDFConverter = XDFConverter;
        this.SQLConverter = SQLConverter;
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
    public static @Nonnull <O, E> Converters<O, E> get(@Nonnull RequestingXDFConverter<O, E> XDFConverter, @Nonnull SQLConverter<O, E> SQLConverter) {
        return new Converters<>(XDFConverter, SQLConverter);
    }
    
    /* -------------------------------------------------- Subtyping -------------------------------------------------- */
    
    /**
     * Returns converters with the XDF converter subtyping to the given type.
     * 
     * @return converters with the XDF converter subtyping to the given type.
     * 
     * @require type.isBasedOn(getXDFConverter().getType()) : "The given type is based on the type of the XDF converter.";
     */
    @Pure
    public @Nonnull Converters<O, E> setType(@Nonnull SemanticType type) {
        return new Converters<>(getXDFConverter().setType(type), getSQLConverter());
    }
    
}
