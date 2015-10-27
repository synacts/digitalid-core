package net.digitalid.service.core.factory.encoding;

import net.digitalid.service.core.block.Block;

import net.digitalid.service.core.identity.annotations.Loaded;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.identity.SemanticType;
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
    
    /**
     * Returns the given non-null encodable as a block. This method should be used if the generic types of the encodable cannot be inferred, e.g. if we are handing an encodable instance using its super type as a parameter.
     * The method will use the correct encoding factory and cast the encodable to the generic type of this encoding factory before encoding it into a block.
     * 
     * @param encodable the encodable object that lost information about its generic types.
     * 
     * @return the given non-null encodable as a block.
     */
    @Pure
	public static @Nullable <O extends Encodable<?, ?>> Block nonNullableWithCast(@Nonnull O encodable) {
    	return encodable.getEncodingFactory().encodeNonNullableWithCast(encodable);
    }
    
    /**
     * Returns the given nullable encodable as a block. This method should be used if the generic types of the encodable cannot be inferred, e.g. if we are handing an encodable instance using its super type as a parameter.
     * The method will use the correct encoding factory and cast the encodable to the generic type of this encoding factory before encoding it into a block.
     * 
     * @param encodable the encodable object that lost information about its generic types.
     * 
     * @return the given nullable encodable as a block or null if the encodable was null.
     */
    @Pure
	public static @Nullable <O extends Encodable<?, ?>> Block nullableWithCast(@Nullable O encodable) {
		return encodable == null ? null : nonNullableWithCast(encodable);
	}

    /**
     * Returns the given non-null encodable as a block set to a given semantic type. This method should be used if the generic types of the encodable cannot be inferred, e.g. if we are handing an encodable instance using its super type as a parameter.
     * The method will use the correct encoding factory and cast the encodable to the generic type of this encoding factory before encoding it into a block.
     * 
     * @param encodable the encodable object that lost information about its generic types.
     * @param type the type which is to be set for the returned block.
     * 
     * @return the given non-null encodable as a block of the given type.
     */
    @Pure
	public static @Nullable <O extends Encodable<?, ?>> Block nonNullableWithCast(@Nullable O encodable, @Nonnull @Loaded SemanticType type) {
    	return nonNullableWithCast(encodable).setType(type);
    }
    
    /**
     * Returns the given nullable encodable as a block set to a given semantic type. This method should be used if the generic types of the encodable cannot be inferred, e.g. if we are handing an encodable instance using its super type as a parameter.
     * The method will use the correct encoding factory and cast the encodable to the generic type of this encoding factory before encoding it into a block.
     * 
     * @param encodable the encodable object that lost information about its generic types.
     * @param type the type which is to be set for the returned block.
     * 
     * @return the given nullable encodable as a block of the given type or null if the encodable was null.
     */
    @Pure
	public static @Nullable <O extends Encodable<?, ?>> Block nullableWithCast(@Nullable O encodable, @Nonnull @Loaded SemanticType type) {
		return encodable == null ? null : nonNullableWithCast(encodable, type);
	}
    
}
