package net.digitalid.service.core.converter.xdf;

import javax.annotation.Nonnull;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.converter.key.AbstractKeyConverter;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This class implements an XDF converter that converts an object to its key and this key to XDF with another converter.
 * 
 * @param <O> the type of the objects that this converter can encode and decode, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to decode a block, which is quite often an {@link Entity}.
 *            In case no external information is needed for the decoding of a block, declare it as an {@link Object}.
 * @param <K> the type of the objects that the other converter encodes and decodes (usually as a key for the objects of this converter).
 * 
 * @see SubtypingXDFConverter
 */
@Immutable
public class ChainingXDFConverter<O, E, K> extends AbstractXDFConverter<O, E> {
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the key converter used to convert and recover the object.
     */
    private final @Nonnull AbstractKeyConverter<O, ? super E, K> keyConverter;
    
    /**
     * Stores the XDF converter used to encode and decode the object's key.
     */
    private final @Nonnull AbstractXDFConverter<K, E> XDFConverter;
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new chaining XDF converter with the given parameters.
     * 
     * @param type the semantic type that corresponds to the encoding class.
     * @param keyConverter the key converter used to convert and recover the object.
     * @param XDFConverter the XDF converter used to encode and decode the object's key.
     * 
     * @require type.isBasedOn(XDFConverter.getType()) : "The given type is based on the type of the XDF converter.";
     */
    protected ChainingXDFConverter(@Nonnull SemanticType type, @Nonnull AbstractKeyConverter<O, ? super E, K> keyConverter, @Nonnull AbstractXDFConverter<K, E> XDFConverter) {
        super(type);
        
        assert type.isBasedOn(XDFConverter.getType()) : "The given type is based on the type of the XDF converter.";
        
        this.keyConverter = keyConverter;
        this.XDFConverter = XDFConverter;
    }
    
    /**
     * Creates a new chaining XDF converter with the given parameters.
     * 
     * @param keyConverter the key converter used to convert and recover the object.
     * @param XDFConverter the XDF converter used to encode and decode the object's key.
     * 
     * @return a new chaining XDF converter with the given parameters.
     */
    @Pure
    public static @Nonnull <O, E, K> ChainingXDFConverter<O, E, K> get(@Nonnull AbstractKeyConverter<O, ? super E, K> keyConverter, @Nonnull AbstractXDFConverter<K, E> XDFConverter) {
        return new ChainingXDFConverter<>(XDFConverter.getType(), keyConverter, XDFConverter);
    }
    
    /* -------------------------------------------------- Methods -------------------------------------------------- */
    
    @Pure
    @Override
    public final @Nonnull Block encodeNonNullable(@Nonnull O object) {
        return XDFConverter.encodeNonNullable(keyConverter.convert(object)).setType(getType());
    }
    
    @Pure
    @Override
    public final @Nonnull O decodeNonNullable(@Nonnull E external, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException {
        assert block.getType().isBasedOn(getType()) : "The block is based on the type of this converter.";
        
        final @Nonnull K key = XDFConverter.decodeNonNullable(external, block);
        if (!keyConverter.isValid(key)) { throw new InvalidEncodingException("The decoded key '" + key + "' is invalid."); }
        return keyConverter.recover(external, key);
    }
    
}
