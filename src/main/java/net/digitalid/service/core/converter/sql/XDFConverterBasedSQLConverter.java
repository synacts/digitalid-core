package net.digitalid.service.core.converter.sql;

import javax.annotation.Nonnull;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.converter.key.BlockConverter;
import net.digitalid.service.core.converter.xdf.AbstractXDFConverter;
import net.digitalid.service.core.converter.xdf.XDF;
import net.digitalid.service.core.entity.annotations.Matching;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.configuration.Database;
import net.digitalid.utility.database.declaration.Declaration;

/**
 * This class implements the methods that all SQL converters which store their data as a {@link Block block} in the {@link Database database} share.
 * 
 * @param <O> the type of the objects that this converter can store and restore, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to restore an object, which is quite often an {@link Entity}.
 *            In case no external information is needed for the restoration of an object, declare it as an {@link Object}.
 */
@Immutable
public final class XDFConverterBasedSQLConverter<O, E> extends ChainingSQLConverter<O, E, Block, SemanticType> {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new XDF-based SQL converter with the given XDF converter.
     * 
     * @param declaration the declaration of the new chaining SQL converter.
     * @param XDFConverter the XDF converter used to encode and decode the block.
     */
    private XDFConverterBasedSQLConverter(@Nonnull @Matching Declaration declaration, @Nonnull AbstractXDFConverter<O, E> XDFConverter) {
        super(declaration, BlockConverter.get(XDFConverter), Block.SQL_CONVERTER);
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
    public static @Nonnull <O extends XDF<O, E>, E> XDFConverterBasedSQLConverter<O, E> get(@Nonnull @Matching Declaration declaration, @Nonnull AbstractXDFConverter<O, E> XDFConverter) {
        return new XDFConverterBasedSQLConverter<>(declaration, XDFConverter);
    }
    
    /**
     * Returns a new XDF-based SQL converter with the given XDF converter.
     * 
     * @param XDFConverter the XDF converter used to encode and decode the block.
     * 
     * @return a new XDF-based SQL converter with the given XDF converter.
     */
    @Pure
    public static @Nonnull <O extends XDF<O, E>, E> XDFConverterBasedSQLConverter<O, E> get(@Nonnull AbstractXDFConverter<O, E> XDFConverter) {
        return new XDFConverterBasedSQLConverter<>(Block.DECLARATION, XDFConverter);
    }
    
}
