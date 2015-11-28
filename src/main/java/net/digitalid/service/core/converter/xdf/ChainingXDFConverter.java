package net.digitalid.service.core.converter.xdf;

import javax.annotation.Nonnull;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.converter.key.AbstractKeyConverter;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.encoding.InvalidParameterValueException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.system.exceptions.InternalException;

/**
 * This class implements an XDF converter that converts an object to its key and this key to XDF with another converter.
 * 
 * @param <O> the type of the objects that this converter can encode and decode, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to decode a block, which is quite often an {@link Entity}.
 *            In case no external information is needed for the decoding of a block, declare it as an {@link Object}.
 * @param <K> the type of the objects that the other converter encodes and decodes (as a key for this converter's objects).
 * @param <D> the type of the external object that is needed to recover the key, which is quite often an {@link Entity}.
 *            In case no external information is needed for the recovery of the key, declare it as an {@link Object}.
 * 
 * @see SubtypingXDFConverter
 */
@Immutable
public class ChainingXDFConverter<O, E, K, D> extends AbstractXDFConverter<O, E> {
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the key converter used to convert and recover the object.
     */
    private final @Nonnull AbstractKeyConverter<O, ? super E, K, D> keyConverter;
    
    /**
     * Stores the XDF converter used to encode and decode the object's key.
     */
    private final @Nonnull AbstractXDFConverter<K, ? super D> XDFConverter;
    
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
    protected ChainingXDFConverter(@Nonnull SemanticType type, @Nonnull AbstractKeyConverter<O, ? super E, K, D> keyConverter, @Nonnull AbstractXDFConverter<K, ? super D> XDFConverter) {
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
    public static @Nonnull <O, E, K, D> ChainingXDFConverter<O, E, K, D> get(@Nonnull AbstractKeyConverter<O, ? super E, K, D> keyConverter, @Nonnull AbstractXDFConverter<K, ? super D> XDFConverter) {
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
    public final @Nonnull O decodeNonNullable(@Nonnull E external, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        assert block.getType().isBasedOn(getType()) : "The block is based on the type of this converter.";
        
        final @Nonnull K key = XDFConverter.decodeNonNullable(keyConverter.decompose(external), block);
        if (!keyConverter.isValid(key)) { throw InvalidParameterValueException.get("key", key); }
        return keyConverter.recover(external, key);
    }
    
}
