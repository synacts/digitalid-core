package net.digitalid.core.conversion.sql;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.state.Matching;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.core.Database;
import net.digitalid.database.core.converter.sql.ChainingSQLConverter;
import net.digitalid.database.core.declaration.Declaration;

import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.key.XDFBasedKeyConverter;
import net.digitalid.core.conversion.xdf.RequestingXDFConverter;
import net.digitalid.core.conversion.xdf.XDF;
import net.digitalid.core.identification.identity.SemanticType;

/**
 * This class implements the methods that all SQL converters which store their data as a {@link Block block} in the {@link Database database} share.
 * 
 * @param <O> the type of the objects that this converter can store and restore, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to restore an object, which is quite often an {@link Entity}.
 *            In case no external information is needed for the restoration of an object, declare it as an {@link Object}.
 */
@Immutable
public final class XDFBasedSQLConverter<O, E> extends ChainingSQLConverter<O, E, Block, SemanticType> {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new XDF-based SQL converter with the given XDF converter.
     * 
     * @param declaration the declaration of the new chaining SQL converter.
     * @param XDFConverter the XDF converter used to encode and decode the block.
     */
    private XDFBasedSQLConverter(@Nonnull @Matching Declaration declaration, @Nonnull RequestingXDFConverter<O, E> XDFConverter) {
        super(declaration, XDFBasedKeyConverter.get(XDFConverter), Block.SQL_CONVERTER);
    }
    
    /**
     * Returns a new XDF-based SQL converter with the given XDF converter.
     * 
     * @param declaration the declaration of the new chaining SQL converter.
     * @param XDFConverter the XDF converter used to encode and decode the block.
     * 
     * @return a new XDF-based SQL converter with the given XDF converter.
     */
    @Pure
    public static @Nonnull <O extends XDF<O, E>, E> XDFBasedSQLConverter<O, E> get(@Nonnull @Matching Declaration declaration, @Nonnull RequestingXDFConverter<O, E> XDFConverter) {
        return new XDFBasedSQLConverter<>(declaration, XDFConverter);
    }
    
    /**
     * Returns a new XDF-based SQL converter with the given XDF converter.
     * 
     * @param XDFConverter the XDF converter used to encode and decode the block.
     * 
     * @return a new XDF-based SQL converter with the given XDF converter.
     */
    @Pure
    public static @Nonnull <O extends XDF<O, E>, E> XDFBasedSQLConverter<O, E> get(@Nonnull RequestingXDFConverter<O, E> XDFConverter) {
        return new XDFBasedSQLConverter<>(Block.DECLARATION, XDFConverter);
    }
    
}
