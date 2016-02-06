package net.digitalid.core.conversion.wrappers.structure;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.elements.NullableElements;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.freezable.Frozen;
import net.digitalid.utility.validation.annotations.math.NonNegative;
import net.digitalid.utility.validation.annotations.math.Positive;
import net.digitalid.utility.validation.annotations.reference.Captured;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.method.Pure;

import net.digitalid.utility.conversion.None;

import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.VariableInteger;

import net.digitalid.core.conversion.annotations.Encoding;
import net.digitalid.core.conversion.annotations.NonEncoding;

import net.digitalid.core.conversion.wrappers.AbstractWrapper;
import net.digitalid.core.conversion.wrappers.BlockBasedWrapper;

import net.digitalid.service.core.converter.xdf.Encode;
import net.digitalid.service.core.converter.xdf.XDF;

import net.digitalid.core.conversion.exceptions.InvalidBlockOffsetException;
import net.digitalid.core.conversion.exceptions.InvalidCollectionSizeException;
import net.digitalid.core.conversion.exceptions.InvalidNullElementException;

import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.SyntacticType;
import net.digitalid.service.core.identity.annotations.BasedOn;
import net.digitalid.service.core.identity.annotations.Loaded;

/**
 * This class wraps a {@link ReadOnlyArray tuple} for encoding and decoding a block of the syntactic type {@code tuple@core.digitalid.net}.
 */
@Immutable
public final class TupleWrapper extends BlockBasedWrapper<TupleWrapper> {
    
    /* -------------------------------------------------- Parameters -------------------------------------------------- */
    
    /**
     * Returns whether the given elements are based on the corresponding parameter of the given type.
     * 
     * @param type the semantic type of the block.
     * @param elements the elements to check.
     * 
     * @return whether the given elements are based on the corresponding parameter of the given type.
     */
    @Pure
    public static boolean basedOnParameters(@Nonnull SemanticType type, @Nonnull @NullableElements ReadOnlyArray<Block> elements) {
        final @Nonnull ReadOnlyList<SemanticType> parameters = type.getParameters();
        if (elements.size() == 0 || elements.size() > parameters.size()) { return false; }
        for (int i = 0; i < elements.size(); i++) {
            final @Nullable Block element = elements.getNullable(i);
            if (element != null && !element.getType().isBasedOn(parameters.getNonNullable(i))) { return false; }
        }
        return true;
    }
    
    /* -------------------------------------------------- Elements -------------------------------------------------- */
    
    /**
     * Stores the nullable elements of this tuple wrapper.
     * 
     * @invariant basedOnParameters(getSemanticType(), elements) : "Each element is either null or based on the corresponding parameter of the semantic type.";
     */
    private final @Nonnull @NullableElements @Frozen ReadOnlyArray<Block> elements;
    
    /**
     * Returns the nullable elements of this tuple wrapper.
     * 
     * @return the nullable elements of this tuple wrapper.
     * 
     * @ensure basedOnParameters(getSemanticType(), elements) : "Each element is either null or based on the corresponding parameter of the semantic type.";
     */
    @Pure
    public @Nonnull @NullableElements @Frozen ReadOnlyArray<Block> getNullableElements() {
        return elements;
    }
    
    /**
     * Returns the non-nullable elements of this tuple wrapper.
     * 
     * @return the non-nullable elements of this tuple wrapper.
     * 
     * @ensure basedOnParameters(getSemanticType(), elements) : "Each element is based on the corresponding parameter of the semantic type.";
     */
    @Pure
    public @Nonnull @NonNullableElements @Frozen ReadOnlyArray<Block> getNonNullableElements() throws InvalidNullElementException {
        if (elements.containsNull()) { throw InvalidNullElementException.get(); }
        return elements;
    }
    
    /**
     * Returns the nullable elements of this tuple wrapper.
     * 
     * @param length the minimum number of elements.
     * 
     * @return the nullable elements of this tuple wrapper.
     * 
     * @throws InvalidCollectionSizeException if the number of elements is less than the given length.
     * 
     * @ensure elements.size() >= length : "The number of elements is at least the given length.";
     * @ensure basedOnParameters(getSemanticType(), elements) : "Each element is either null or based on the corresponding parameter of the semantic type.";
     */
    @Pure
    public @Nonnull @NullableElements @Frozen ReadOnlyArray<Block> getNullableElements(@Positive int length) throws InvalidCollectionSizeException {
        Require.that(length > 0).orThrow("The length is positive.");
        
        if (elements.size() < length) { throw InvalidCollectionSizeException.get(length, elements.size()); }
        return elements;
    }
    
    /**
     * Returns the non-nullable elements of this tuple wrapper.
     * 
     * @param length the minimum number of elements.
     * 
     * @return the non-nullable elements of this tuple wrapper.
     * 
     * @throws InvalidCollectionSizeException if the number of elements is less than the given length.
     * @throws InvalidNullElementException if the elements contain null.
     * 
     * @ensure elements.size() >= length : "The number of elements is at least the given length.";
     * @ensure basedOnParameters(getSemanticType(), elements) : "Each element is based on the corresponding parameter of the semantic type.";
     */
    @Pure
    public @Nonnull @NonNullableElements @Frozen ReadOnlyArray<Block> getNonNullableElements(@Positive int length) throws InvalidCollectionSizeException, InvalidNullElementException {
        final @Nonnull ReadOnlyArray<Block> elements = getNullableElements(length);
        if (elements.containsNull()) { throw InvalidNullElementException.get(); }
        return elements;
    }
    
    /**
     * Returns whether the element at the given index is null.
     * 
     * @param index the index of the element to be checked.
     * 
     * @return whether the element at the given index is null.
     * 
     * @throws InvalidCollectionSizeException if the number of elements is less than or equal to the given index.
     */
    @Pure
    public boolean isElementNull(@NonNegative int index) throws InvalidCollectionSizeException {
        Require.that(index >= 0).orThrow("The index is non-negative.");
        
        if (index >= elements.size()) { throw InvalidCollectionSizeException.get(index, elements.size()); }
        return elements.getNullable(index) == null;
    }
    
    /**
     * Returns the nullable element at the given index.
     * 
     * @param index the index of the element to be returned.
     * 
     * @return the nullable element at the given index.
     * 
     * @throws InvalidCollectionSizeException if the number of elements is less than or equal to the given index.
     * 
     * @ensure element == null || element.getType().isBasedOn(getSemanticType().getParameters().getNonNullable(index)) : "The element is either null or based on the corresponding parameter of the semantic type.";
     */
    @Pure
    public @Nullable Block getNullableElement(@NonNegative int index) throws InvalidCollectionSizeException {
        Require.that(index >= 0).orThrow("The index is non-negative.");
        
        if (index >= elements.size()) { throw InvalidCollectionSizeException.get(index, elements.size()); }
        return elements.getNullable(index);
    }
    
    /**
     * Returns the non-nullable element at the given index.
     * 
     * @param index the index of the element to be returned.
     * 
     * @return the non-nullable element at the given index.
     * 
     * @throws InvalidCollectionSizeException if the number of elements is less than or equal to the given index.
     * @throws InvalidNullElementException if the accessed element is null.
     * 
     * @ensure element.getType().isBasedOn(getSemanticType().getParameters().getNonNullable(index)) : "The element is based on the corresponding parameter of the semantic type.";
     */
    @Pure
    public @Nonnull Block getNonNullableElement(@NonNegative int index) throws InvalidCollectionSizeException, InvalidNullElementException {
        final @Nullable Block element = TupleWrapper.this.getNullableElement(index);
        if (element == null) { throw InvalidNullElementException.get(); }
        return element;
    }
    
    /**
     * Returns the nullable element of the given type.
     * 
     * @param type the type of the element which is to be returned.
     * 
     * @return the nullable element of the given type.
     * 
     * @require getSemanticType().getParameters().contains(type) : "The parameters of this tuple contain the given type.";
     * 
     * @ensure return == null || return.getType().isBasedOn(type) : "The returned block is either null or based on the given type.";
     */
    @Pure
    public @Nullable Block getNullableElement(@Nonnull SemanticType type) {
        final @Nonnull ReadOnlyList<SemanticType> parameters = getSemanticType().getParameters();
        Require.that(parameters.contains(type)).orThrow("The parameters of this tuple contain the given type.");
        
        final @Nullable Block element = elements.getNullable(parameters.indexOf(type));
        
        Require.that(element == null || element.getType().isBasedOn(type)).orThrow("The returned block is either null or based on the given type.");
        
        return element;
    }
    
    /**
     * Returns the non-nullable element of the given type.
     * 
     * @param type the type of the element which is to be returned.
     * 
     * @return the non-nullable element of the given type.
     * 
     * @throws InvalidNullElementException if the element of the given type is null.
     * 
     * @require getSemanticType().getParameters().contains(type) : "The parameters of this tuple contain the given type.";
     * 
     * @ensure return.getType().isBasedOn(type) : "The returned block is based on the given type.";
     */
    @Pure
    public @Nonnull Block getNonNullableElement(@Nonnull SemanticType type) throws InvalidNullElementException {
        final @Nullable Block element = getNullableElement(type);
        if (element == null) { throw InvalidNullElementException.get(); }
        return element;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new tuple wrapper with the given type and elements.
     * 
     * @param type the semantic type of the new tuple wrapper.
     * @param elements the elements of the new tuple wrapper.
     * 
     * @require basedOnParameters(type, elements) : "Each element is either null or based on the corresponding parameter of the given type.";
     */
    private TupleWrapper(@Nonnull @Loaded @BasedOn("tuple@core.digitalid.net") SemanticType type, @Nonnull @NullableElements @Frozen ReadOnlyArray<Block> elements) {
        super(type);
        
        Require.that(elements.isFrozen()).orThrow("The elements are frozen.");
        Require.that(basedOnParameters(type, elements)).orThrow("Each element is either null or based on the corresponding parameter of the given type.");
        
        this.elements = elements;
    }
    
    /* -------------------------------------------------- Encoding -------------------------------------------------- */
    
    @Pure
    @Override
    public int determineLength() {
        int length = 0;
        for (final @Nullable Block element : elements) {
            if (element == null) {
                length += 1;
            } else {
                final int elementLength = element.getLength();
                final int intvarLength = VariableInteger.determineLength(elementLength);
                length += intvarLength + elementLength;
            }
        }
        return length;
    }
    
    @Pure
    @Override
    public void encode(@Nonnull @Encoding Block block) {
        Require.that(block.getLength() == determineLength()).orThrow("The block's length has to match the determined length.");
        Require.that(block.getType().isBasedOn(getSyntacticType())).orThrow("The block is based on the indicated syntactic type.");
        
        int offset = 0;
        
        for (final @Nullable Block element : elements) {
            if (element == null) {
                offset += 1;
            } else {
                final int elementLength = element.getLength();
                final int intvarLength = VariableInteger.determineLength(elementLength);
                
                VariableInteger.encode(block, offset, intvarLength, elementLength);
                offset += intvarLength;
                element.writeTo(block, offset, elementLength);
                offset += elementLength;
            }
        }
        
        Require.that(offset == block.getLength()).orThrow("The whole block should now be encoded.");
    }
    
    /* -------------------------------------------------- Syntactic Type -------------------------------------------------- */
    
    /**
     * Stores the syntactic type {@code tuple@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType XDF_TYPE = SyntacticType.map("tuple@core.digitalid.net").load(-1);
    
    @Pure
    @Override
    public @Nonnull SyntacticType getSyntacticType() {
        return XDF_TYPE;
    }
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * The XDF converter for this class.
     */
    @Immutable
    public static final class XDFConverter extends AbstractWrapper.XDFConverter<TupleWrapper> {
        
        /**
         * Creates a new XDF converter with the given type.
         * 
         * @param type the semantic type of the encoded blocks and decoded wrappers.
         */
        private XDFConverter(@Nonnull @Loaded @BasedOn("tuple@core.digitalid.net") SemanticType type) {
            super(type);
        }
        
        @Pure
        @Override
        public @Nonnull TupleWrapper decodeNonNullable(@Nonnull Object none, @Nonnull @NonEncoding @BasedOn("tuple@core.digitalid.net") Block block) throws InvalidEncodingException, InternalException {
            final @Nonnull @NonNullableElements ReadOnlyList<SemanticType> parameters = block.getType().getParameters();
            final int size = parameters.size();
            final @Nonnull FreezableArray<Block> elements = FreezableArray.get(size);
            
            int offset = 0;
            final int length = block.getLength();
            
            for (int i = 0; i < size; i++) {
                if (offset >= length) { break; }
                int intvarLength = VariableInteger.decodeLength(block, offset);
                int elementLength = (int) VariableInteger.decodeValue(block, offset, intvarLength);
                offset += intvarLength;
                if (elementLength > 0) {
                    if (offset + elementLength > block.getLength()) { throw InvalidBlockOffsetException.get(offset, elementLength, block); }
                    elements.set(i, Block.get(parameters.getNonNullable(i), block, offset, elementLength));
                    offset += elementLength;
                }
            }
            
            return new TupleWrapper(block.getType(), elements.freeze());
        }
        
    }
    
    @Pure
    @Override
    public @Nonnull XDFConverter getXDFConverter() {
        return new XDFConverter(getSemanticType());
    }
    
    /* -------------------------------------------------- XDF Utility -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code semantic.tuple@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SEMANTIC = SemanticType.map("semantic.tuple@core.digitalid.net").load(XDF_TYPE);
    
    /**
     * Stores a static XDF converter for performance reasons.
     */
    private static final @Nonnull XDFConverter XDF_CONVERTER = new XDFConverter(SEMANTIC);

    /**
     * Encodes the given elements into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param elements the elements to encode into the new block.
     * 
     * @return a new block containing the given elements.
     * 
     * @require basedOnParameters(type, elements) : "Each element is either null or based on the corresponding parameter of the given type.";
     */
    @Pure
    public static @Nonnull @NonEncoding Block encode(@Nonnull @Loaded @BasedOn("tuple@core.digitalid.net") SemanticType type, @Nonnull @NullableElements @Frozen ReadOnlyArray<Block> elements) {
        return XDF_CONVERTER.encodeNonNullable(new TupleWrapper(type, elements));
    }
    
    /**
     * Encodes the given elements into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param elements the elements to encode into the new block.
     * 
     * @return a new block containing the given elements.
     * 
     * @require basedOnParameters(type, elements) : "Each element is either null or based on the corresponding parameter of the given type.";
     */
    @Pure
    public static @Nonnull @NonEncoding Block encode(@Nonnull @Loaded @BasedOn("tuple@core.digitalid.net") SemanticType type, @Captured @Nonnull @NonEncoding Block... elements) {
        return encode(type, FreezableArray.getNonNullable(elements).freeze());
    }
    
    /**
     * Encodes the given elements into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param elements the elements to encode into the new block.
     * 
     * @return a new block containing the given elements.
     * 
     * @require basedOnParameters(type, elements.toBlock()) : "Each element is either null or based on the corresponding parameter of the given type.";
     */
    @Pure
    public static @Nonnull @NonEncoding Block encode(@Nonnull @Loaded @BasedOn("tuple@core.digitalid.net") SemanticType type, @Nonnull XDF<?, ?>... elements) {
        final @Nonnull FreezableArray<Block> array = FreezableArray.get(elements.length);
        for (int i = 0; i < elements.length; i++) { array.set(i, Encode.nullableWithCast(elements[i])); }
        return encode(type, array.freeze());
    }
    
    /**
     * Decodes the given block. 
     * 
     * @param block the block to be decoded.
     * 
     * @return the tuple contained in the given block.
     */
    @Pure
    public static @Nonnull TupleWrapper decode(@Nonnull @NonEncoding @BasedOn("tuple@core.digitalid.net") Block block) throws InvalidEncodingException, InternalException {
        return XDF_CONVERTER.decodeNonNullable(None.OBJECT, block);
    }
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull SQLConverter<TupleWrapper> getSQLConverter() {
        return new SQLConverter<>(getXDFConverter());
    }
    
}
