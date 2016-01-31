package net.digitalid.core.conversion.xdf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.validation.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.core.conversion.Block;

import net.digitalid.core.identity.SemanticType;

import net.digitalid.core.identity.annotations.Loaded;

/**
 * This is a utility class to encode objects that implement {@link XDF}.
 */
@Stateless
public final class Encode {
    
    /* -------------------------------------------------- Normal Encoding -------------------------------------------------- */
    
    /**
     * Returns the given non-nullable object as a block.
     * 
     * @param object the non-nullable object to convert.
     * 
     * @return the given non-nullable object as a block.
     */
    @Pure
    public static @Nonnull <O extends XDF<O, ?>> Block nonNullable(@Nonnull O object) {
        return object.getXDFConverter().encodeNonNullable(object);
    }
    
    /**
     * Returns the given nullable object as a block.
     * 
     * @param object the nullable object to convert.
     * 
     * @return the given nullable object as a block.
     */
    @Pure
    public static @Nullable <O extends XDF<O, ?>> Block nullable(@Nullable O object) {
        return object == null ? null : nonNullable(object);
    }
    
    /**
     * Returns the given non-nullable object as a block of the given type.
     * 
     * @param type the type which is to be set for the returned block.
     * @param object the non-nullable object to be converted to a block.
     * 
     * @return the given non-nullable object as a block of the given type.
     * 
     * @require type.isBasedOn(object.getXDFConverter().getType()) : "The given type is based on the type of the object object.";
     */
    @Pure
    public static @Nonnull <O extends XDF<O, ?>> Block nonNullable(@Nonnull @Loaded SemanticType type, @Nonnull O object) {
        return nonNullable(object).setType(type);
    }
    
    /**
     * Returns the given nullable object as a block of the given type.
     * 
     * @param object the nullable object to be converted to a block.
     * @param type the type which is to be set for the returned block.
     * 
     * @return the given nullable object as a block of the given type.
     * 
     * @require object == null || type.isBasedOn(object.getXDFConverter().getType()) : "If the object object is not null, the given type is based on its type.";
     */
    @Pure
    public static @Nullable <O extends XDF<O, ?>> Block nullable(@Nullable O object, @Nonnull @Loaded SemanticType type) {
        return object == null ? null : nonNullable(object).setType(type);
    }
    
    /* -------------------------------------------------- Casted Encoding -------------------------------------------------- */
    
    /**
     * Returns the given non-null object as a block.
     * This method should be used if the generic types of the object cannot be inferred, e.g. if we are handing an object using its supertype as a parameter.
     * The method will use the correct XDF converter and cast the object to the generic type of this XDF converter before encoding it into a block.
     * 
     * @param object the object object that lost the information about its generic types.
     * 
     * @return the given non-null object as a block.
     */
    @Pure
    public static @Nonnull <O extends XDF<?, ?>> Block nonNullableWithCast(@Nonnull O object) {
        return object.getXDFConverter().encodeNonNullableWithCast(object);
    }
    
    /**
     * Returns the given nullable object as a block.
     * This method should be used if the generic types of the object cannot be inferred, e.g. if we are handing an object using its supertype as a parameter.
     * The method will use the correct XDF converter and cast the object to the generic type of this XDF converter before encoding it into a block.
     * 
     * @param object the object object that lost the information about its generic types.
     * 
     * @return the given nullable object as a block or null if the object was null.
     */
    @Pure
    public static @Nullable <O extends XDF<?, ?>> Block nullableWithCast(@Nullable O object) {
        return object == null ? null : nonNullableWithCast(object);
    }

    /**
     * Returns the given non-null object as a block set to a given semantic type.
     * This method should be used if the generic types of the object cannot be inferred, e.g. if we are handing an object using its supertype as a parameter.
     * The method will use the correct XDF converter and cast the object to the generic type of this XDF converter before encoding it into a block.
     * 
     * @param object the object object that lost the information about its generic types.
     * @param type the type which is to be set for the returned block.
     * 
     * @return the given non-null object as a block of the given type.
     * 
     * @require type.isBasedOn(object.getXDFConverter().getType()) : "The given type is based on the type of the object object.";
     */
    @Pure
    public static @Nonnull <O extends XDF<?, ?>> Block nonNullableWithCast(@Nonnull O object, @Nonnull @Loaded SemanticType type) {
        return nonNullableWithCast(object).setType(type);
    }
    
    /**
     * Returns the given nullable object as a block set to a given semantic type.
     * This method should be used if the generic types of the object cannot be inferred, e.g. if we are handing an object using its supertype as a parameter.
     * The method will use the correct XDF converter and cast the object to the generic type of this XDF converter before encoding it into a block.
     * 
     * @param object the object object that lost the information about its generic types.
     * @param type the type which is to be set for the returned block.
     * 
     * @return the given nullable object as a block of the given type or null if the object was null.
     * 
     * @require object == null || type.isBasedOn(object.getXDFConverter().getType()) : "If the object object is not null, the given type is based on its type.";
     */
    @Pure
    public static @Nullable <O extends XDF<?, ?>> Block nullableWithCast(@Nullable O object, @Nonnull @Loaded SemanticType type) {
        return object == null ? null : nonNullableWithCast(object, type);
    }
    
}
