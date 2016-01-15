package net.digitalid.service.core.converter.key;

import javax.annotation.Nonnull;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.converter.xdf.RequestingXDFConverter;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;
import net.digitalid.utility.exceptions.external.ExternalException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.exceptions.external.MaskingInvalidEncodingException;
import net.digitalid.utility.exceptions.internal.InternalException;

/**
 * This class allows to convert an object to its {@link Block block} and recover it again.
 * 
 * @param <O> the type of the objects that this converter can convert and recover.
 * @param <E> the type of the external object that is needed to recover an object.
 *            In case no external information is needed for the recovery of an object, declare it as an {@link Object}.
 */
@Immutable
public final class XDFBasedKeyConverter<O, E> extends NonRequestingKeyConverter<O, E, Block, SemanticType> {
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * Stores the XDF converter used to encode and decode the block.
     */
    private final @Nonnull RequestingXDFConverter<O, E> XDFConverter;
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new object-block converter with the given XDF converter.
     * 
     * @param XDFConverter the XDF converter used to encode and decode the block.
     */
    private XDFBasedKeyConverter(@Nonnull RequestingXDFConverter<O, E> XDFConverter) {
        this.XDFConverter = XDFConverter;
    }
    
    /**
     * Returns a new object-block converter with the given XDF converter.
     * 
     * @param XDFConverter the XDF converter used to encode and decode the block.
     * 
     * @return a new object-block converter with the given XDF converter.
     */
    @Pure
    public static @Nonnull <O, E> XDFBasedKeyConverter<O, E> get(@Nonnull RequestingXDFConverter<O, E> XDFConverter) {
        return new XDFBasedKeyConverter<>(XDFConverter);
    }
    
    /* -------------------------------------------------- Conversions -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull SemanticType decompose(@Nonnull E external) {
        return XDFConverter.getType();
    }
    
    @Pure
    @Override
    public @Nonnull Block convert(@Nonnull O object) {
        return XDFConverter.encodeNonNullable(object);
    }
    
    @Pure
    @Override
    public @Nonnull O recover(@Nonnull E external, @Nonnull Block block) throws InvalidEncodingException, InternalException {
        try {
            return XDFConverter.decodeNonNullable(external, block);
        } catch (@Nonnull DatabaseException | NetworkException | ExternalException | RequestException exception) {
            throw MaskingInvalidEncodingException.get(exception);
        }
    }
    
}
