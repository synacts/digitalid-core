package net.digitalid.service.core.encoding;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.annotations.Loaded;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Stateless;

/**
 * This is a utility class to encode blockable objects.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Stateless
public final class Encode {
    
    /**
     * Returns the given non-nullable blockable as a block.
     * 
     * @param blockable the non-nullable object to convert.
     * 
     * @return the given non-nullable blockable as a block.
     */
    @Pure
    public static @Nonnull <O extends Encodable<O, ?>> Block nonNullable(@Nonnull O blockable) {
        return blockable.getEncodingFactory().encodeNonNullable(blockable);
    }
    
    /**
     * Returns the given nullable blockable as a block.
     * 
     * @param blockable the nullable object to convert.
     * 
     * @return the given nullable blockable as a block.
     */
    @Pure
    public static @Nullable <O extends Encodable<O, ?>> Block nullable(@Nullable O blockable) {
        return blockable == null ? null : nonNullable(blockable);
    }
    
    /**
     * Returns the given non-nullable blockable as a block of the given type.
     * 
     * @param blockable the non-nullable object to be converted to a block.
     * @param type the type which is to be set for the returned block.
     * 
     * @return the given non-nullable blockable as a block of the given type.
     * 
     * @require type.isBasedOn(blockable.getFactory().getType()) : "The given type is based on its type.";
     */
    @Pure
    public static @Nonnull <O extends Encodable<O, ?>> Block nonNullable(@Nonnull O blockable, @Nonnull @Loaded SemanticType type) {
        return nonNullable(blockable).setType(type);
    }
    
    /**
     * Returns the given nullable blockable as a block of the given type.
     * 
     * @param blockable the nullable object to be converted to a block.
     * @param type the type which is to be set for the returned block.
     * 
     * @return the given nullable blockable as a block of the given type.
     * 
     * @require blockable == null || type.isBasedOn(blockable.getFactory().getType()) : "If the blockable instance is not null, the given type is based on its type.";
     */
    @Pure
    public static @Nullable <O extends Encodable<O, ?>> Block nullable(@Nullable O blockable, @Nonnull @Loaded SemanticType type) {
        return blockable == null ? null : nonNullable(blockable).setType(type);
    }
    
}
