package net.digitalid.core.conversion.xdf;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.exceptions.internal.InternalException;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;

import net.digitalid.core.conversion.Block;

import net.digitalid.core.conversion.key.NonRequestingKeyConverter;

import net.digitalid.service.core.exceptions.external.encoding.InvalidParameterValueException;

import net.digitalid.core.identity.SemanticType;

/**
 * This class implements a non-requesting XDF converter that is based on another non-requesting XDF converter.
 * 
 * @param <O> the type of the objects that this converter can encode and decode, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to decode a block, which is quite often an {@link Entity}.
 *            In case no external information is needed for the decoding of a block, declare it as an {@link Object}.
 * @param <K> the type of the objects that the other converter encodes and decodes (as a key for this converter's objects).
 * @param <D> the type of the external object that is needed to recover the key, which is quite often an {@link Entity}.
 *            In case no external information is needed for the recovery of the key, declare it as an {@link Object}.
 * 
 * @see SubtypingNonRequestingXDFConverter
 */
@Immutable
public class ChainingNonRequestingXDFConverter<O, E, K, D> extends NonRequestingXDFConverter<O, E> {
    
    /* -------------------------------------------------- Key Converter -------------------------------------------------- */
    
    /**
     * Stores the key converter used to convert and recover the object.
     */
    private final @Nonnull NonRequestingKeyConverter<O, ? super E, K, D> keyConverter;
    
    /**
     * Returns the key converter used to convert and recover the object.
     * 
     * @return the key converter used to convert and recover the object.
     */
    @Pure
    public final @Nonnull NonRequestingKeyConverter<O, ? super E, K, D> getKeyConverter() {
        return keyConverter;
    }
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * Stores the XDF converter used to encode and decode the object's key.
     */
    private final @Nonnull NonRequestingXDFConverter<K, ? super D> XDFConverter;
    
    /**
     * Returns the XDF converter used to encode and decode the object's key.
     * 
     * @return the XDF converter used to encode and decode the object's key.
     */
    @Pure
    public final @Nonnull NonRequestingXDFConverter<K, ? super D> getXDFConverter() {
        return XDFConverter;
    }
    
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
    protected ChainingNonRequestingXDFConverter(@Nonnull SemanticType type, @Nonnull NonRequestingKeyConverter<O, ? super E, K, D> keyConverter, @Nonnull NonRequestingXDFConverter<K, ? super D> XDFConverter) {
        super(type);
        
        assert type.isBasedOn(XDFConverter.getType()) : "The given type is based on the type of the XDF converter.";
        
        this.keyConverter = keyConverter;
        this.XDFConverter = XDFConverter;
    }
    
    /**
     * Returns a new chaining XDF converter with the given parameters.
     * 
     * @param type the semantic type that corresponds to the encoding class.
     * @param keyConverter the key converter used to convert and recover the object.
     * @param XDFConverter the XDF converter used to encode and decode the object's key.
     * 
     * @return a new chaining XDF converter with the given parameters.
     * 
     * @require type.isBasedOn(XDFConverter.getType()) : "The given type is based on the type of the XDF converter.";
     */
    @Pure
    public static @Nonnull <O, E, K, D> ChainingNonRequestingXDFConverter<O, E, K, D> get(@Nonnull SemanticType type, @Nonnull NonRequestingKeyConverter<O, ? super E, K, D> keyConverter, @Nonnull NonRequestingXDFConverter<K, ? super D> XDFConverter) {
        return new ChainingNonRequestingXDFConverter<>(type, keyConverter, XDFConverter);
    }
    
    /**
     * Returns a new chaining XDF converter with the given parameters.
     * 
     * @param keyConverter the key converter used to convert and recover the object.
     * @param XDFConverter the XDF converter used to encode and decode the object's key.
     * 
     * @return a new chaining XDF converter with the given parameters.
     */
    @Pure
    public static @Nonnull <O, E, K, D> ChainingNonRequestingXDFConverter<O, E, K, D> get(@Nonnull NonRequestingKeyConverter<O, ? super E, K, D> keyConverter, @Nonnull NonRequestingXDFConverter<K, ? super D> XDFConverter) {
        return new ChainingNonRequestingXDFConverter<>(XDFConverter.getType(), keyConverter, XDFConverter);
    }
    
    /* -------------------------------------------------- Conversions -------------------------------------------------- */
    
    @Pure
    @Override
    public final @Nonnull Block encodeNonNullable(@Nonnull O object) {
        return XDFConverter.encodeNonNullable(keyConverter.convert(object)).setType(getType());
    }
    
    @Pure
    @Override
    public final @Nonnull O decodeNonNullable(@Nonnull E external, @Nonnull Block block) throws InvalidEncodingException, InternalException {
        assert block.getType().isBasedOn(getType()) : "The block is based on the type of this converter.";
        
        final @Nonnull K key = XDFConverter.decodeNonNullable(keyConverter.decompose(external), block);
        if (!keyConverter.isValid(key)) { throw InvalidParameterValueException.get("key", key); }
        return keyConverter.recover(external, key);
    }
    
}
