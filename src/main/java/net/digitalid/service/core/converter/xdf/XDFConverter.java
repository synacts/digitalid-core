package net.digitalid.service.core.converter.xdf;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.SyntacticType;
import net.digitalid.utility.collections.freezable.FreezableHashMap;
import net.digitalid.utility.conversion.Converter;
import net.digitalid.utility.conversion.Convertible;
import net.digitalid.utility.conversion.exceptions.RestoringException;
import net.digitalid.utility.conversion.exceptions.StoringException;

/**
 * Converts {@link Convertible} objects to and from XDF blocks.
 * 
 * @param <T> The type of the object to which an XDF block is converted to or from.
 */
public abstract class XDFConverter<T> extends Converter {

    /* -------------------------------------------------- Field Converter -------------------------------------------------- */

    /**
     * Converts a nullable field from an XDF block into an object.
     * 
     * @param field the field which should be restored.
     * @param block the block which is converted.
     *              
     * @return a nullable field from an XDF block into an object.
     * 
     * @throws RestoringException if no converter for this field could be found.
     */
    protected @Nullable Object convertField(@Nonnull Field field, @Nullable Block block) throws RestoringException {
        XDFConverter<?> fieldConverter;
        fieldConverter = XDF.FORMAT.getConverter(field);
        Map<Class<? extends Annotation>, Annotation> annotations = getAnnotations(field);
        return fieldConverter.convertFromNullable(block, field.getType(), annotations);
    }
    
    /* -------------------------------------------------- Convert From -------------------------------------------------- */
    
    /**
     * Converts a nullable XDF block into a nullable object.
     * 
     * @param block the block which is converted.
     * @param type the type of the object which should be converted.
     * @param annotations the annotations of the field which should be converted.
     *              
     * @return a nullable object converted from a nullable XDF block.
     * 
     * @throws RestoringException if no converter for this type could be found.
     */
    public @Nullable T convertFromNullable(@Nullable Block block, @Nonnull Class<?> type, @Nullable Map<Class<? extends Annotation>, Annotation> annotations) throws RestoringException {
        if (annotations == null) {
            annotations = FreezableHashMap.get(); 
        }
        return block == null ? null : convertFromNonNullable(block, type, annotations);
    }
    
     /**
     * Converts a non-nullable object from a non-nullable XDF block.
     * 
     * @param block the block which is converted.
     * @param type the type of the object which should be converted.
     * @param annotations the annotations of the field which should be converted.
     * 
     * @return a non-nullable object converted from a non-nullable XDF block.
     *
     * @throws RestoringException if no converter for this type could be found.
     */   
    protected abstract @Nonnull T convertFromNonNullable(@Nonnull Block block, @Nonnull Class<?> type, @Nonnull Map<Class<? extends Annotation>, Annotation> annotations) throws RestoringException;
    
    /* -------------------------------------------------- Convert To -------------------------------------------------- */
    
    /**
     * Converts a nullable object into an XDF block.
     * 
     * @param value the object which is converted.
     * @param type the type of the object which should be converted.
     *              
     * @return a nullable XDF block converted from a nullable value.
     * 
     * @throws StoringException if no converter for this type could be found.
     */ 
    public @Nullable Block convertToNullable(@Nullable Object value, @Nonnull Class<?> type, @Nonnull String fieldName, @Nullable String parentName, @Nullable Map<Class<? extends Annotation>, Annotation> annotations) throws StoringException {
        if (annotations == null) {
            annotations = FreezableHashMap.get();
        }
        return value == null ? null : convertToNonNullable(value, type, fieldName, parentName, annotations);
    }

    /**
     * Converts a non-nullable object into an XDF block.
     *
     * @param value the object which is converted.
     * @param type the type of the object which should be converted.
     * @param fieldName the field name of the object which should be converted.
     * @param parentName the field name of the object which should be converted.
     * @param annotations the field`s annotations.
     *  
     * @return a non-nullable XDF block converted from a non-nullable value.
     *
     * @throws StoringException if no converter for this type could be found.
     */
    public abstract @Nonnull Block convertToNonNullable(@Nonnull Object value, @Nonnull Class<?> type, @Nonnull String fieldName, @Nullable String parentName, @Nonnull Map<Class<? extends Annotation>, Annotation> annotations) throws StoringException;

    /* --------------------------------------------------  -------------------------------------------------- */
    
    /**
     * Generates a semantic type identifier string on the field`s or type's name and the optional parent's name.
     * 
     * @param name the name of the field or type.
     * @param parentName optionally, the name of the type holding the field.
     *                   
     * @return a semantic type identifier string on the field`s or type's name and the optional parent's name.
     */
    private String generateSemanticTypeIdentifier(@Nonnull String name, @Nullable String parentName) {
        if (parentName == null) {
            parentName = "";
        }
        if (parentName.length() > 0) {
            parentName = "." + parentName;
        }       
        return name + parentName + "@core.digitalid.net";
    }
    
    /**
     * Generates a semantic type based on the field`s or type's name and the optional parent's name.
     * 
     * @param name the name of the field or type.
     * @param parentName optionally, the name of the type holding the field.
     * @param semanticBase The semantic type on which this semantic type should be based on.
     * 
     * @return a semantic type based on the field`s name and the optional parent's name.
     */
    protected @Nonnull SemanticType generateSemanticType(@Nonnull String name, @Nullable String parentName, @Nonnull SemanticType semanticBase) {
        return SemanticType.map(generateSemanticTypeIdentifier(name, parentName)).load(semanticBase);
    }
    
    /**
     * Generates a semantic type based on the field`s or type's name and the optional parent's name.
     * 
     * @param name the name of the field or type.
     * @param parentName optionally, the name of the type holding the field.
     * @param syntacticType The syntactic type on which this semantic type should be based on.
     * 
     * @return a semantic type based on the field`s name and the optional parent's name.
     */
    protected @Nonnull SemanticType generateSemanticType(@Nonnull String name, @Nullable String parentName, @Nonnull SyntacticType syntacticType) {
        return SemanticType.map(generateSemanticTypeIdentifier(name, parentName)).load(syntacticType);
    }
}
