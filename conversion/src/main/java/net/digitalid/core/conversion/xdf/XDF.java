package net.digitalid.core.conversion.xdf;

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.conversion.Converter;
import net.digitalid.utility.conversion.ConverterAnnotations;
import net.digitalid.utility.generator.conversion.Convertible;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.StoringException;
import net.digitalid.utility.conversion.exceptions.StructureException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.core.conversion.Block;

/**
 * The XDF class provides a public API for converting convertible objects into the XDF format.
 */
@Stateless
public final class XDF {
    
    /* -------------------------------------------------- Format -------------------------------------------------- */
    
    /**
     * The format object of XDF, which contains the XDF converters.
     */
    public static final @Nonnull XDFFormat FORMAT = new XDFFormat();
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */

    /**
     * Recovers a nullable, convertible object from a nullable XDF block.
     */
    public static @Nullable Convertible recoverNullable(@Nullable Block block, @Nonnull Class<? extends Convertible> type) throws InvalidEncodingException, RecoveryException, InternalException  {
        return block == null ? null : recoverNonNullable(block, type);
    }

    /**
     * Recovers a non-nullable, convertible object from a non-nullable XDF block.
     */
    public static @Nonnull Convertible recoverNonNullable(@Nonnull Block block, @Nonnull Class<? extends Convertible> type) throws InvalidEncodingException, RecoveryException, InternalException  {
        Converter.Structure structure;
        try {
            structure = Converter.inferStructure(type);
        } catch (StructureException e) {
            throw InternalException.get(e.getMessage(), e);
        }
        
        ConverterAnnotations converterAnnotations = Converter.getAnnotations(type);
        
        Convertible convertible;
        switch (structure) {
            case TUPLE:
                convertible = XDFFormat.TUPLE_CONVERTER.recoverNullable(block, type, converterAnnotations);
                break;
            case SINGLE_TYPE:
                convertible = XDFFormat.SINGLE_FIELD_CONVERTER.recoverNullable(block, type, converterAnnotations);
                break;
            default:
                throw new RuntimeException("Structure '" + structure + "' is unknown. Known types are: '" + Arrays.toString(Converter.Structure.values()) + "'.");
        }
        if (!type.isInstance(convertible)) {
            throw RecoveryException.get(type, "The converter failed to recover the type '" + type + "' from XDF block.");
        }
        return type.cast(convertible);
    }
    
    /* -------------------------------------------------- Convert To -------------------------------------------------- */

    /**
     * Converts a nullable object into a nullable XDF block.
     */
    public static @Nullable Block convertNullable(@Nullable Convertible convertible, @Nonnull Class<? extends Convertible> type) throws StoringException, InternalException {
       return convertible == null ? null : convertNonNullable(convertible, type); 
    }

    /**
     * Converts a nullable object into a nullable XDF block using an optional parent name.
     */
    public static @Nullable Block convertNullable(@Nullable Convertible convertible, @Nonnull Class<? extends Convertible> type, @Nullable String parentName) throws StoringException, InternalException {
        return convertible == null ? null : convertNonNullable(convertible, type, parentName);
    }

    /**
     * Converts a non-nullable object into a non-nullable XDF block.
     *
     * @param convertible the convertible object which is converted into an XDF block.
     * @param type the type of the object which should be converted.
     * 
     * @return a non-nullable XDF block converted from a non-nullable value.
     *
     * @throws StoringException if no converter for this type could be found.
     */
    public static @Nonnull Block convertNonNullable(@Nonnull Convertible convertible, @Nonnull Class<? extends Convertible> type) throws StoringException, InternalException {
        return convertNonNullable(convertible, type, null);
    }

    /**
     * Converts a non-nullable object into a non-nullable XDF block with an optionally given parent name.
     *
     * @param convertible the convertible object which is converted into an XDF block.
     * @param type the type of the object which should be converted.
     * @param parentName the parent name of the object which should be converted.
     *
     * @return a non-nullable XDF block converted from a non-nullable value.
     *
     * @throws StoringException if no converter for this type could be found.
     */
    @SuppressWarnings("unchecked")
    public static @Nonnull Block convertNonNullable(@Nonnull Convertible convertible, @Nonnull Class<? extends Convertible> type, @Nullable String parentName) throws StoringException, InternalException {

        @Nonnull Block serializedObject;
        final @Nonnull Converter.Structure structure;
        try {
            structure = Converter.inferStructure(type);
        } catch (StructureException e) {
            throw StoringException.get(type, e.getMessage(), e);
        }
        final @Nonnull ConverterAnnotations annotations = ConverterAnnotations.get();
        switch (structure) {
            case TUPLE:
                serializedObject = XDFFormat.TUPLE_CONVERTER.convertNonNullable(convertible, type, type.getSimpleName(), parentName, annotations);
                break;
            case SINGLE_TYPE:
                serializedObject = XDFFormat.SINGLE_FIELD_CONVERTER.convertNonNullable(convertible, type, type.getSimpleName(), parentName, annotations);
                break;
            default:
                throw InternalException.get("Structure '" + structure + "' is unknown. Known types are: '" + Arrays.toString(Converter.Structure.values()) + "'.");
        }
        return serializedObject;
    }
    
}
