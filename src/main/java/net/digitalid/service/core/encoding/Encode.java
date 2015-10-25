package net.digitalid.service.core.encoding;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.annotations.Loaded;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Stateless;

/**
 * This is a utility class to encode encodable objects.
 */
@Stateless
public final class Encode {
    
    /**
     * Returns the given non-nullable encodable as a block.
     * 
     * @param encodable the non-nullable object to convert.
     * 
     * @return the given non-nullable encodable as a block.
     */
    @Pure
    public static @Nonnull <O extends Encodable<O, ?>> Block nonNullable(@Nonnull O encodable) {
        return encodable.getEncodingFactory().encodeNonNullable(encodable);
    }
    
    /**
     * Returns the given nullable encodable as a block.
     * 
     * @param encodable the nullable object to convert.
     * 
     * @return the given nullable encodable as a block.
     */
    @Pure
    public static @Nullable <O extends Encodable<O, ?>> Block nullable(@Nullable O encodable) {
        return encodable == null ? null : nonNullable(encodable);
    }
    
    /**
     * Returns the given non-nullable encodable as a block of the given type.
     * 
     * @param encodable the non-nullable object to be converted to a block.
     * @param type the type which is to be set for the returned block.
     * 
     * @return the given non-nullable encodable as a block of the given type.
     * 
     * @require type.isBasedOn(encodable.getFactory().getType()) : "The given type is based on its type.";
     */
    @Pure
    public static @Nonnull <O extends Encodable<O, ?>> Block nonNullable(@Nonnull O encodable, @Nonnull @Loaded SemanticType type) {
        return nonNullable(encodable).setType(type);
    }
    
    /**
     * Returns the given nullable encodable as a block of the given type.
     * 
     * @param encodable the nullable object to be converted to a block.
     * @param type the type which is to be set for the returned block.
     * 
     * @return the given nullable encodable as a block of the given type.
     * 
     * @require encodable == null || type.isBasedOn(encodable.getFactory().getType()) : "If the encodable instance is not null, the given type is based on its type.";
     */
    @Pure
    public static @Nullable <O extends Encodable<O, ?>> Block nullable(@Nullable O encodable, @Nonnull @Loaded SemanticType type) {
        return encodable == null ? null : nonNullable(encodable).setType(type);
    }
    
}
