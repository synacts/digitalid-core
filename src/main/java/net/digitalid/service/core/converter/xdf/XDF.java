package net.digitalid.service.core.converter.xdf;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.utility.annotations.state.Stateless;
import net.digitalid.utility.conversion.Converter;
import net.digitalid.utility.conversion.Convertible;
import net.digitalid.utility.conversion.exceptions.RestoringException;
import net.digitalid.utility.conversion.exceptions.StoringException;
import net.digitalid.utility.conversion.exceptions.StructureException;

/**
 * Objects of classes that implement this interface can be encoded as a {@link Block block}.
 * 
 * @param <O> the type of the objects that the converter can encode and decode, which is typically the declaring class itself.
 * @param <E> the type of the external object that is needed to decode an object, which is quite often an {@link Entity}.
 *            In case no external information is needed for the decoding of an object, declare it as an {@link Object}.
 */
@Stateless
public final class XDF {
    
    /**
     * 
     */
    public static final @Nonnull XDFFormat FORMAT = new XDFFormat();
    
    /* -------------------------------------------------- Convert From -------------------------------------------------- */
    
    /**
     * Converts a nullable object from a nullable XDF block.
     * 
     * @param block the block which is converted to an object.
     * @param type the type of the object which should be converted.
     *              
     * @return a nullable object converted from a nullable block.
     * 
     * @throws RestoringException if no converter for this type could be found.
     */    
    public static @Nullable Convertible convertFrom(@Nullable Block block, @Nonnull Class<? extends Convertible> type) throws RestoringException {
        Converter.Structure structure;
        try {
            structure = inferStructure(type);
        } catch (StructureException e) {
            throw RestoringException.get(type, e.getMessage(), e);
        }
        
        // TODO: Extract into a separate method with https://docs.oracle.com/javase/7/docs/api/java/lang/reflect/AnnotatedElement.html
        Map<Class<? extends Annotation>, Annotation> metaData = Converter.getAnnotations(type);
        
        Convertible convertible;
        switch (structure) {
            case TUPLE:
                convertible = TUPLE_CONVERTER.convertFromNullable(block, type, metaData);
                break;
            case SINGLE_TYPE:
                convertible = SINGLE_FIELD_CONVERTER.convertFromNullable(block, type, metaData);
                break;
            default:
                throw new RuntimeException("Structure '" + structure + "' is unknown. Known types are: '" + Arrays.toString(Converter.Structure.values()) + "'.");
        }
        if (!type.isInstance(convertible)) {
            throw RestoringException.get(type, "The converter failed to convert the XDF block into the type '" + type + "'");
        }
        return type.cast(convertible);
    }
    
    /* -------------------------------------------------- Convert To -------------------------------------------------- */
    
    /**
     * Converts a nullable object into an XDF block.
     *
     * @param convertible the convertible object which is converted into an XDF block.
     * @param type the type of the object which should be converted.
     * 
     * @return a non-nullable XDF block converted from a non-nullable value.
     *
     * @throws StoringException if no converter for this type could be found.
     */
    public static @Nullable Block convertTo(@Nullable Convertible convertible, @Nonnull Class<? extends Convertible> type) throws StoringException {
        return convertTo(convertible, type, null);
    }

    /**
     * Converts a nullable object into an XDF block.
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
    public static @Nullable Block convertTo(@Nullable Convertible convertible, @Nonnull Class<? extends Convertible> type, @Nullable String parentName) throws StoringException {

        Block serializedObject;
        Converter.Structure structure;
        try {
            structure = inferStructure(type);
        } catch (StructureException e) {
            throw StoringException.get(type, e.getMessage(), e);
        }
        switch (structure) {
            case TUPLE:
                serializedObject = TUPLE_CONVERTER.convertToNullable(convertible, type, type.getSimpleName(), parentName, null);
                break;
            case SINGLE_TYPE:
                serializedObject = SINGLE_FIELD_CONVERTER.convertToNullable(convertible, type, type.getSimpleName(), parentName, null);
                break;
            default:
                throw new RuntimeException("Structure '" + structure + "' is unknown. Known types are: '" + Arrays.toString(Converter.Structure.values()) + "'.");
        }
        return serializedObject;
    }
    
}
