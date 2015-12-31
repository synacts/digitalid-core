package net.digitalid.service.core.converter.xdf;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.converter.xdf.serializer.iterable.XDFArrayConverter;
import net.digitalid.service.core.converter.xdf.serializer.iterable.XDFCollectionConverter;
import net.digitalid.service.core.converter.xdf.serializer.iterable.XDFMapConverter;
import net.digitalid.service.core.converter.xdf.serializer.structure.XDFSingleFieldConverter;
import net.digitalid.service.core.converter.xdf.serializer.structure.XDFTupleConverter;
import net.digitalid.service.core.converter.xdf.serializer.value.XDFBooleanConverter;
import net.digitalid.service.core.converter.xdf.serializer.value.XDFObjectConverter;
import net.digitalid.service.core.converter.xdf.serializer.value.binary.XDFBinaryConverter;
import net.digitalid.service.core.converter.xdf.serializer.value.integer.XDFInteger08Converter;
import net.digitalid.service.core.converter.xdf.serializer.value.integer.XDFInteger16Converter;
import net.digitalid.service.core.converter.xdf.serializer.value.integer.XDFInteger32Converter;
import net.digitalid.service.core.converter.xdf.serializer.value.integer.XDFInteger64Converter;
import net.digitalid.service.core.converter.xdf.serializer.value.integer.XDFIntegerConverter;
import net.digitalid.service.core.converter.xdf.serializer.value.string.XDFCharacterConverter;
import net.digitalid.service.core.converter.xdf.serializer.value.string.XDFStringConverter;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.SyntacticType;
import net.digitalid.utility.collections.freezable.FreezableHashMap;
import net.digitalid.utility.system.converter.Converter;
import net.digitalid.utility.system.converter.Convertible;
import net.digitalid.utility.system.converter.TypeToTypeMapper;
import net.digitalid.utility.system.converter.exceptions.FieldConverterException;
import net.digitalid.utility.system.converter.exceptions.RestoringException;
import net.digitalid.utility.system.converter.exceptions.StoringException;
import net.digitalid.utility.system.converter.exceptions.StructureException;

/**
 * Converts {@link Convertible} objects to and from XDF blocks.
 * 
 * @param <T> The type of the object to which an XDF block is converted to or from.
 */
public abstract class XDFConverter<T> extends Converter<XDFConverter<?>> {

    /* -------------------------------------------------- Field Converter -------------------------------------------------- */

    /**
     * Returns a map of meta data which carries information such as generic types of a field.
     * 
     * @param field the field from which the meta data is extracted from.
     *              
     * @return a map of meta data which carries information such as generic types of a field.
     */
    protected @Nonnull Map<Class<? extends Annotation>, Object> getFieldMetaData(@Nonnull Field field) {
        Map<Class<? extends Annotation>, Object> fieldMetaData = FreezableHashMap.get();
        for (Annotation annotation : field.getDeclaredAnnotations()) {
            Object annotationObject = field.getAnnotation(annotation.annotationType());
            fieldMetaData.put(annotation.annotationType(), annotationObject);
        }
        return fieldMetaData;
    }

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
        try {
            fieldConverter = getFieldConverter(field);
        } catch (FieldConverterException e) {
            throw RestoringException.get(field.getType(), e);
        }
        Map<Class<? extends Annotation>, Object> fieldMetaData = getFieldMetaData(field);
        return fieldConverter.convertFromNullable(block, field.getType(), fieldMetaData);
    }
    
    /* -------------------------------------------------- Convert From -------------------------------------------------- */
    
    /**
     * Converts a nullable XDF block into a non-nullable object.
     * 
     * @param block the block which is converted.
     * @param type the type of the object which should be converted.
     * @param metaData the metadata of the field which should be converted.
     *              
     * @return a nullable object converted from a nullable XDF block.
     * 
     * @throws RestoringException if no converter for this type could be found.
     */
    public @Nullable T convertFromNullable(@Nullable Block block, @Nonnull Class<?> type, @Nullable Map<Class<? extends Annotation>, Object> metaData) throws RestoringException {
        if (metaData == null) {
            metaData = FreezableHashMap.get(); 
        }
        return block == null ? null : convertFromNonNullable(block, type, metaData);
    }
    
     /**
     * Converts a non-nullable object from a non-nullable XDF block.
     * 
     * @param block the block which is converted.
     * @param type the type of the object which should be converted.
     * @param metaData the metadata of the field which should be converted.
     * 
     * @return a non-nullable object converted from a non-nullable XDF block.
     *
     * @throws RestoringException if no converter for this type could be found.
     */   
    protected abstract @Nonnull T convertFromNonNullable(@Nonnull Block block, @Nonnull Class<?> type, @Nonnull Map<Class<? extends Annotation>, Object> metaData) throws RestoringException;
    
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
        Structure structure;
        try {
            structure = inferStructure(type);
        } catch (StructureException e) {
            throw RestoringException.get(type, e.getMessage(), e);
        }
        Map<Class<? extends Annotation>, Object> metaData = FreezableHashMap.get();
        for (Annotation annotation : type.getDeclaredAnnotations()) {
            Object annotationObject = type.getAnnotation(annotation.annotationType());
            metaData.put(annotation.annotationType(), annotationObject);
        }
        
        Convertible convertible;
        switch (structure) {
            case TUPLE:
                convertible = TUPLE_CONVERTER.convertFromNullable(block, type, metaData);
                break;
            case SINGLE_TYPE:
                convertible = SINGLE_FIELD_CONVERTER.convertFromNullable(block, type, metaData);
                break;
            default:
                throw new RuntimeException("Structure '" + structure + "' is unknown. Known types are: '" + Arrays.toString(Structure.values()) + "'.");
        }
        return convertible;
    }

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
    public @Nullable Block convertToNullable(@Nullable Object value, @Nonnull Class<?> type, @Nonnull String fieldName, @Nullable String parentName, @Nullable Map<Class<? extends Annotation>, Object> metaData) throws StoringException {
        if (metaData == null) {
            metaData = FreezableHashMap.get();
        }
        return value == null ? null : convertToNonNullable(value, type, fieldName, parentName, metaData);
    }

    /**
     * Converts a non-nullable object into an XDF block.
     *
     * @param value the object which is converted.
     * @param type the type of the object which should be converted.
     * @param fieldName the field name of the object which should be converted.
     * @param parentName the field name of the object which should be converted.
     * @param metaData the field's meta data.
     *  
     * @return a non-nullable XDF block converted from a non-nullable value.
     *
     * @throws StoringException if no converter for this type could be found.
     */
    public abstract @Nonnull Block convertToNonNullable(@Nonnull Object value, @Nonnull Class<?> type, @Nonnull String fieldName, @Nullable String parentName, @Nonnull Map<Class<? extends Annotation>, Object> metaData) throws StoringException;

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
        Structure structure;
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
                throw new RuntimeException("Structure '" + structure + "' is unknown. Known types are: '" + Arrays.toString(Structure.values()) + "'.");
        }
        return serializedObject;
    }

    /* -------------------------------------------------- Type Converters -------------------------------------------------- */

    private final static XDFConverter<Boolean> BOOLEAN_CONVERTER = new XDFBooleanConverter();
    
    private final static XDFConverter<String> STRING_CONVERTER = new XDFStringConverter();
    
    private final static XDFConverter<Character> CHARACTER_CONVERTER = new XDFCharacterConverter();
    
    private final static XDFConverter<Byte[]> BINARY_CONVERTER = new XDFBinaryConverter();

    private final static XDFConverter<Byte> INTEGER08_CONVERTER = new XDFInteger08Converter();
    private final static XDFConverter<Short> INTEGER16_CONVERTER = new XDFInteger16Converter();
    private final static XDFConverter<Integer> INTEGER32_CONVERTER = new XDFInteger32Converter();
    private final static XDFConverter<Long> INTEGER64_CONVERTER = new XDFInteger64Converter();
    
    private final static XDFConverter<BigInteger> INTEGER_CONVERTER = new XDFIntegerConverter();
    
    private final static XDFConverter<Convertible> CONVERTIBLE_CONVERTER = new XDFObjectConverter();
    private final static XDFConverter<? extends Collection<?>> COLLECTION_CONVERTER = new XDFCollectionConverter<>();
    private final static XDFConverter<? extends Object[]> ARRAY_CONVERTER = new XDFArrayConverter();
    private final static XDFConverter<? extends Map<?, ?>> MAP_CONVERTER = new XDFMapConverter<>();
    
    private final static XDFConverter<? extends Convertible> TUPLE_CONVERTER = new XDFTupleConverter<>();
    private final static XDFConverter<? extends Convertible> SINGLE_FIELD_CONVERTER = new XDFSingleFieldConverter<>();

    @Override
    protected XDFConverter<Boolean> getBooleanConverter() {
        return BOOLEAN_CONVERTER;
    }

    @Override
    protected XDFConverter<Character> getCharacterConverter() {
        return CHARACTER_CONVERTER;
    }

    @Override
    protected XDFConverter<String> getStringConverter() {
        return STRING_CONVERTER;
    }

    @Override
    protected XDFConverter<Byte[]> getBinaryConverter() {
        return BINARY_CONVERTER;
    }

    @Override
    protected XDFConverter<BigInteger> getIntegerConverter() {
        return INTEGER_CONVERTER;
    }

    @Override
    protected XDFConverter<Byte> getInteger08Converter() {
        return INTEGER08_CONVERTER;
    }

    @Override
    protected XDFConverter<Short> getInteger16Converter() {
        return INTEGER16_CONVERTER;
    }

    @Override
    protected XDFConverter<Integer> getInteger32Converter() {
        return INTEGER32_CONVERTER;
    }

    @Override
    protected XDFConverter<Long> getInteger64Converter() {
        return INTEGER64_CONVERTER;
    }
    
    @Override
    protected XDFConverter<Convertible> getConvertibleConverter() {
        return CONVERTIBLE_CONVERTER;
    }

    @Override
    protected XDFConverter<? extends Collection<?>> getCollectionConverter() {
        return COLLECTION_CONVERTER;
    }

    @Override
    protected XDFConverter<? extends Object[]> getArrayConverter() {
        return ARRAY_CONVERTER;
    }
    
    @Override
    protected XDFConverter<? extends Map<?, ?>> getMapConverter() {
        return MAP_CONVERTER;
    }

    @Override
    protected XDFConverter<?> getTypeToTypeConverter(TypeToTypeMapper<?, ?> typeToTypeMapper) {
        return XDFKeyConverter.get(typeToTypeMapper);
    }

    /**
     * Generates a semantic type identifier string on the field's or type's name and the optional parent's name.
     * 
     * @param name the name of the field or type.
     * @param parentName optionally, the name of the type holding the field.
     *                   
     * @return a semantic type identifier string on the field's or type's name and the optional parent's name.
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
     * Generates a semantic type based on the field's or type's name and the optional parent's name.
     * 
     * @param name the name of the field or type.
     * @param parentName optionally, the name of the type holding the field.
     * @param semanticBase The semantic type on which this semantic type should be based on.
     * 
     * @return a semantic type based on the field's name and the optional parent's name.
     */
    protected @Nonnull SemanticType generateSemanticType(@Nonnull String name, @Nullable String parentName, @Nonnull SemanticType semanticBase) {
        return SemanticType.map(generateSemanticTypeIdentifier(name, parentName)).load(semanticBase);
    }
    
    /**
     * Generates a semantic type based on the field's or type's name and the optional parent's name.
     * 
     * @param name the name of the field or type.
     * @param parentName optionally, the name of the type holding the field.
     * @param syntacticType The syntactic type on which this semantic type should be based on.
     * 
     * @return a semantic type based on the field's name and the optional parent's name.
     */
    protected @Nonnull SemanticType generateSemanticType(@Nonnull String name, @Nullable String parentName, @Nonnull SyntacticType syntacticType) {
        return SemanticType.map(generateSemanticTypeIdentifier(name, parentName)).load(syntacticType);
    }
}
