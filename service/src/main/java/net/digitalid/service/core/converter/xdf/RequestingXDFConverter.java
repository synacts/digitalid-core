package net.digitalid.service.core.converter.xdf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.annotations.NonEncoding;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.Loaded;
import net.digitalid.utility.validation.reference.Capturable;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;
import net.digitalid.utility.exceptions.external.ExternalException;
import net.digitalid.utility.exceptions.internal.InternalException;

/**
 * An XDF converter allows to encode and decode objects into and from {@link Block blocks}.
 * This converter allows file, network and database requests during {@link #decodeNonNullable(java.lang.Object, net.digitalid.service.core.block.Block) decoding}.
 * 
 * @param <O> the type of the objects that this converter can encode and decode, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to decode a block, which is quite often an {@link Entity}.
 *            In case no external information is needed for the decoding of a block, declare it as an {@link Object}.
 * 
 * @see XDF
 * @see ChainingXDFConverter
 * @see AbstractNonRequestingXDFConverter
 */
@Immutable
public abstract class RequestingXDFConverter<O, E> {
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    /**
     * Stores the semantic type that corresponds to the class that implements XDF.
     */
    private final @Nonnull SemanticType type;
    
    /**
     * Returns the semantic type that corresponds to the class that implements XDF.
     * 
     * @return the semantic type that corresponds to the class that implements XDF.
     */
    @Pure
    public final @Nonnull SemanticType getType() {
        return type;
    }
    
    /**
     * Returns an abstract XDF converter with the given type based on this converter.
     * 
     * @return an abstract XDF converter with the given type based on this converter.
     * 
     * @require type.isBasedOn(getType()) : "The given type is based on the type of this converter.";
     */
    @Pure
    public @Nonnull RequestingXDFConverter<O, E> setType(@Nonnull SemanticType type) {
        return SubtypingRequestingXDFConverter.get(type, this);
    }
    
    /* -------------------------------------------------- Encoding -------------------------------------------------- */
    
    /**
     * Encodes the given non-nullable object as a new block.
     * 
     * @param object the non-nullable object to encode as a block.
     * 
     * @return the given non-nullable object encoded as a new block.
     * 
     * @ensure return.getType().equals(getType()) : "The returned block has the indicated type.";
     */
    @Pure
    public abstract @Capturable @Nonnull @NonEncoding Block encodeNonNullable(@Nonnull O object);
    
    /**
     * Encodes the given nullable object as a new block.
     * 
     * @param object the nullable object to encode as a block.
     * 
     * @return the given nullable object encoded as a new block.
     * 
     * @ensure return == null || return.getType().equals(getType()) : "The returned block is either null or has the indicated type.";
     */
    @Pure
    public final @Capturable @Nullable @NonEncoding Block encodeNullable(@Nullable O object) {
        return object == null ? null : encodeNonNullable(object);
    }
    
    /* -------------------------------------------------- Encoding with Casting -------------------------------------------------- */
    
    /**
     * Encodes the given non-nullable object as a new block.
     * The object is casted to the type that this converter encodes.
     * 
     * @param object the non-nullable object to encode as a block.
     * 
     * @return the given non-nullable object encoded as a new block.
     * 
     * @ensure return.getType().equals(getType()) : "The returned block has the indicated type.";
     */
    @Pure
    @SuppressWarnings("unchecked")
    final @Capturable @Nonnull @NonEncoding Block encodeNonNullableWithCast(@Nonnull Object object) {
        return encodeNonNullable((O) object);
    }
    
    /**
     * Encodes the given nullable object as a new block.
     * The object is casted to the type that this converter encodes.
     * 
     * @param object the nullable object to encode as a block.
     * 
     * @return the given nullable object encoded as a new block.
     * 
     * @ensure return == null || return.getType().equals(getType()) : "The returned block is either null or has the indicated type.";
     */
    @Pure
    @SuppressWarnings("unchecked")
    final @Capturable @Nullable @NonEncoding Block encodeNullableWithCast(@Nullable Object object) {
        return encodeNullable((O) object);
    }
    
    /* -------------------------------------------------- Decoding -------------------------------------------------- */
    
    /**
     * Decodes the given non-nullable block.
     * 
     * @param external the external object needed to recover the object.
     * @param block the non-nullable block which is to be decoded.
     * 
     * @return the object that was encoded in the non-nullable block.
     * 
     * @require block.getType().isBasedOn(getType()) : "The block is based on the type of this converter.";
     */
    @Pure
    @Locked
    @NonCommitting
    public abstract @Nonnull O decodeNonNullable(@Nonnull E external, @Nonnull @NonEncoding Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException;
    
    /**
     * Decodes the given nullable block.
     * 
     * @param external the external object needed to recover the object.
     * @param block the nullable block which is to be decoded.
     * 
     * @return the object that was encoded in the nullable block.
     * 
     * @require block == null || block.getType().isBasedOn(getType()) : "The block is either null or based on the indicated type.";
     */
    @Pure
    @Locked
    @NonCommitting
    public @Nullable O decodeNullable(@Nonnull E external, @Nullable @NonEncoding Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        if (block != null) { return decodeNonNullable(external, block); }
        else { return null; }
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new abstract XDF converter with the given type.
     * 
     * @param type the semantic type that corresponds to the class that implements XDF.
     */
    protected RequestingXDFConverter(@Nonnull @Loaded SemanticType type) {
        this.type = type;
    }
    
}
