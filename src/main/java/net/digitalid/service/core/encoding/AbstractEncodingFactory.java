package net.digitalid.service.core.encoding;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.annotations.Loaded;
import net.digitalid.service.core.annotations.NonEncoding;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * A factory allows to encode and decode objects into and from {@link Block blocks}.
 * This factory allows file, network and database requests during {@link #decodeNonNullable(java.lang.Object, net.digitalid.service.core.wrappers.Block) decoding}.
 * 
 * @param <O> the type of the objects that this factory can encode and decode, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to decode a block, which is quite often an {@link Entity}.
 *            In case no external information is needed for the decoding of a block, declare it as an {@link Object}.
 * 
 * @see Encodable
 * @see NonRequestingEncodingFactory
 * @see FactoryBasedEncodingFactory
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public abstract class AbstractEncodingFactory<O, E> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Type –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type that corresponds to the encodable class.
     */
    private final @Nonnull @Loaded SemanticType type;
    
    /**
     * Returns the semantic type that corresponds to the encodable class.
     * 
     * @return the semantic type that corresponds to the encodable class.
     */
    @Pure
    public final @Nonnull @Loaded SemanticType getType() {
        return type;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    public abstract @Nonnull @NonEncoding Block encodeNonNullable(@Nonnull O object);
    
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
    public final @Nullable @NonEncoding Block encodeNullable(@Nullable O object) {
        return object == null ? null : encodeNonNullable(object);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encoding with Casting –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Encodes the given non-nullable object as a new block.
     * The object is casted to the type that this factory encodes.
     * 
     * @param object the non-nullable object to encode as a block.
     * 
     * @return the given non-nullable object encoded as a new block.
     * 
     * @ensure return.getType().equals(getType()) : "The returned block has the indicated type.";
     */
    @Pure
    @SuppressWarnings("unchecked")
    public final @Nonnull @NonEncoding Block encodeNonNullableWithCast(@Nonnull Object object) {
        return encodeNonNullable((O) object);
    }
    
    /**
     * Encodes the given nullable object as a new block.
     * The object is casted to the type that this factory encodes.
     * 
     * @param object the nullable object to encode as a block.
     * 
     * @return the given nullable object encoded as a new block.
     * 
     * @ensure return == null || return.getType().equals(getType()) : "The returned block is either null or has the indicated type.";
     */
    @Pure
    @SuppressWarnings("unchecked")
    public final @Nullable @NonEncoding Block encodeNullableWithCast(@Nullable Object object) {
        return encodeNullable((O) object);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Decoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Decodes the given non-nullable block.
     * 
     * @param entity the entity needed to reconstruct the object.
     * @param block the non-nullable block which is to be decoded.
     * 
     * @return the object that was encoded in the non-nullable block.
     * 
     * @require block.getType().isBasedOn(getType()) : "The block is based on the type of this factory.";
     */
    @Pure
    @Locked
    @NonCommitting
    public abstract @Nonnull O decodeNonNullable(@Nonnull E entity, @Nonnull @NonEncoding Block block) throws AbortException, PacketException, ExternalException, NetworkException;
    
    /**
     * Decodes the given nullable block.
     * 
     * @param entity the entity needed to reconstruct the object.
     * @param block the nullable block which is to be decoded.
     * 
     * @return the object that was encoded in the nullable block.
     * 
     * @require block == null || block.getType().isBasedOn(getType()) : "The block is either null or based on the indicated type.";
     */
    @Pure
    @Locked
    @NonCommitting
    public @Nullable O decodeNullable(@Nonnull E entity, @Nullable @NonEncoding Block block) throws AbortException, PacketException, ExternalException, NetworkException {
        if (block != null) return decodeNonNullable(entity, block);
        else return null;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new encoding factory with the given type.
     * 
     * @param type the semantic type that corresponds to the encodable class.
     */
    protected AbstractEncodingFactory(@Nonnull @Loaded SemanticType type) {
        this.type = type;
    }
    
}
