package net.digitalid.core.conversion.wrappers.structure;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.ownership.Captured;
import net.digitalid.utility.collections.list.FreezableArrayList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.conversion.None;
import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.elements.NullableElements;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.VariableInteger;
import net.digitalid.core.conversion.annotations.Encoding;
import net.digitalid.core.conversion.annotations.NonEncoding;
import net.digitalid.core.conversion.exceptions.InvalidBlockLengthException;
import net.digitalid.core.conversion.exceptions.InvalidBlockOffsetException;
import net.digitalid.core.conversion.exceptions.InvalidNullElementException;
import net.digitalid.core.conversion.wrappers.AbstractWrapper;
import net.digitalid.core.conversion.wrappers.BlockBasedWrapper;

import net.digitalid.service.core.converter.xdf.Encode;
import net.digitalid.service.core.converter.xdf.XDF;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.SyntacticType;
import net.digitalid.service.core.identity.annotations.BasedOn;
import net.digitalid.service.core.identity.annotations.Loaded;

/**
 * This class wraps a {@link ReadOnlyList list} for encoding and decoding a block of the syntactic type {@code list@core.digitalid.net}.
 */
@Immutable
public final class ListWrapper extends BlockBasedWrapper<ListWrapper> {
    
    /* -------------------------------------------------- Parameters -------------------------------------------------- */
    
    /**
     * Returns whether the given elements are based on the parameter of the given type.
     * 
     * @param type the semantic type of the block.
     * @param elements the elements to check.
     * 
     * @return whether the given elements are based on the parameter of the given type.
     */
    @Pure
    public static boolean basedOnParameter(@Nonnull SemanticType type, @Nonnull @NullableElements ReadOnlyList<Block> elements) {
        final @Nonnull SemanticType parameter = type.getParameters().getNonNullable(0);
        for (final @Nullable Block element : elements) {
            if (element != null && !element.getType().isBasedOn(parameter)) { return false; }
        }
        return true;
    }
    
    /* -------------------------------------------------- Elements -------------------------------------------------- */
    
    /**
     * Stores the nullable elements of this list wrapper.
     * 
     * @invariant basedOnParameter(getSemanticType(), elements) : "Each element is either null or based on the parameter of the semantic type.";
     */
    private final @Nonnull @NullableElements @Frozen ReadOnlyList<Block> elements;
    
    /**
     * Returns the nullable elements of this list wrapper.
     * 
     * @return the nullable elements of this list wrapper.
     * 
     * @ensure basedOnParameter(getSemanticType(), elements) : "Each element is either null or based on the parameter of the semantic type.";
     */
    @Pure
    public @Nonnull @NullableElements @Frozen ReadOnlyList<Block> getNullableElements() {
        return elements;
    }
    
    /**
     * Returns the non-nullable elements of this list wrapper.
     * 
     * @return the non-nullable elements of this list wrapper.
     * 
     * @throws InvalidNullElementException if the elements contain null.
     * 
     * @ensure basedOnParameter(getSemanticType(), elements) : "Each element is based on the parameter of the semantic type.";
     */
    @Pure
    public @Nonnull @NonNullableElements @Frozen ReadOnlyList<Block> getNonNullableElements() throws InvalidNullElementException {
        final @Nonnull ReadOnlyList<Block> elements = getNullableElements();
        if (elements.containsNull()) { throw InvalidNullElementException.get(); }
        return elements;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new list wrapper with the given type and elements.
     * 
     * @param type the semantic type of the new list wrapper.
     * @param elements the elements of the new list wrapper.
     * 
     * @require basedOnParameter(type, elements) : "Each element is either null or based on the parameter of the semantic type.";
     */
    private ListWrapper(@Nonnull @Loaded @BasedOn("list@core.digitalid.net") SemanticType type, @Nonnull @NullableElements @Frozen ReadOnlyList<Block> elements) {
        super(type);
        
        Require.that(elements.isFrozen()).orThrow("The elements are frozen.");
        Require.that(basedOnParameter(type, elements)).orThrow("Each element is either null or based on the parameter of the semantic type.");
        
        this.elements = elements;
    }
    
    /* -------------------------------------------------- Encoding -------------------------------------------------- */
    
    @Pure
    @Override
    public int determineLength() {
        int length = VariableInteger.determineLength(elements.size());
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
        
        final int size = elements.size();
        int offset = VariableInteger.determineLength(size);
        VariableInteger.encode(block, 0, offset, size);
        
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
     * Stores the syntactic type {@code list@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType XDF_TYPE = SyntacticType.map("list@core.digitalid.net").load(1);
    
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
    public static final class XDFConverter extends AbstractWrapper.XDFConverter<ListWrapper> {
        
        /**
         * Creates a new XDF converter with the given type.
         * 
         * @param type the semantic type of the encoded blocks and decoded wrappers.
         */
        private XDFConverter(@Nonnull @Loaded @BasedOn("list@core.digitalid.net") SemanticType type) {
            super(type);
        }
        
        @Pure
        @Override
        public @Nonnull ListWrapper decodeNonNullable(@Nonnull Object none, @Nonnull @NonEncoding @BasedOn("list@core.digitalid.net") Block block) throws InvalidEncodingException, InternalException {
            final @Nonnull SemanticType parameter = block.getType().getParameters().getNonNullable(0);
            
            int offset = VariableInteger.decodeLength(block, 0);
            final int size = (int) VariableInteger.decodeValue(block, 0, offset);
            
            final @Nonnull FreezableList<Block> elements = FreezableArrayList.getWithCapacity(size);
            
            for (int i = 0; i < size; i++) {
                final int intvarLength = VariableInteger.decodeLength(block, offset);
                final int elementLength = (int) VariableInteger.decodeValue(block, offset, intvarLength);
                offset += intvarLength;
                if (elementLength == 0) {
                    elements.add(null);
                } else {
                    if (offset + elementLength > block.getLength()) { throw InvalidBlockOffsetException.get(offset, elementLength, block); }
                    elements.add(Block.get(parameter, block, offset, elementLength));
                    offset += elementLength;
                }
            }
            
            if (offset != block.getLength()) { throw InvalidBlockLengthException.get(offset, block.getLength()); }
            
            return new ListWrapper(block.getType(), elements.freeze());
        }
        
    }
    
    @Pure
    @Override
    public @Nonnull XDFConverter getXDFConverter() {
        return new XDFConverter(getSemanticType());
    }
    
    /* -------------------------------------------------- XDF Utility -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code semantic.list@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SEMANTIC = SemanticType.map("semantic.list@core.digitalid.net").load(XDF_TYPE);
    
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
     * @require basedOnParameter(type, elements) : "Each element is either null or based on the parameter of the given type.";
     */
    @Pure
    public static @Nonnull @NonEncoding Block encode(@Nonnull @Loaded @BasedOn("list@core.digitalid.net") SemanticType type, @Nonnull @NullableElements @Frozen ReadOnlyList<Block> elements) {
        return XDF_CONVERTER.encodeNonNullable(new ListWrapper(type, elements));
    }
    
    /**
     * Encodes the given elements into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param elements the elements to encode into the new block.
     * 
     * @return a new block containing the given elements.
     * 
     * @require basedOnParameter(type, elements) : "Each element is either null or based on the parameter of the given type.";
     */
    @Pure
    public static @Nonnull @NonEncoding Block encode(@Nonnull @Loaded @BasedOn("list@core.digitalid.net") SemanticType type, @Captured @Nonnull @NonEncoding Block... elements) {
        return encode(type, FreezableArrayList.get(elements).freeze());
    }
    
    /**
     * Encodes the given elements into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param elements the elements to encode into the new block.
     * 
     * @return a new block containing the given elements.
     * 
     * @require basedOnParameter(type, elements.toBlock()) : "Each element is either null or based on the parameter of the given type.";
     */
    @Pure
    @SuppressWarnings("unchecked")
    public static @Nonnull @NonEncoding <V extends XDF<V, ?>> Block encode(@Nonnull @Loaded @BasedOn("list@core.digitalid.net") SemanticType type, @Captured @Nonnull V... elements) {
        final @Nonnull FreezableList<Block> list = FreezableArrayList.getWithCapacity(elements.length);
        for (final @Nullable V element : elements) { list.add(Encode.nullable(element)); }
        return encode(type, list.freeze());
    }
    
    /**
     * Decodes the given block. 
     * 
     * @param block the block to be decoded.
     * 
     * @return the nullable elements contained in the given block.
     */
    @Pure
    public static @Nonnull @NullableElements @Frozen ReadOnlyList<Block> decodeNullableElements(@Nonnull @NonEncoding @BasedOn("list@core.digitalid.net") Block block) throws InvalidEncodingException, InternalException {
        return XDF_CONVERTER.decodeNonNullable(None.OBJECT, block).getNullableElements();
    }
    
    /**
     * Decodes the given block. 
     * 
     * @param block the block to be decoded.
     * 
     * @return the non-nullable elements contained in the given block.
     */
    @Pure
    public static @Nonnull @NonNullableElements @Frozen ReadOnlyList<Block> decodeNonNullableElements(@Nonnull @NonEncoding @BasedOn("list@core.digitalid.net") Block block) throws InvalidEncodingException, InternalException {
        return XDF_CONVERTER.decodeNonNullable(None.OBJECT, block).getNonNullableElements();
    }
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull SQLConverter<ListWrapper> getSQLConverter() {
        return new SQLConverter<>(getXDFConverter());
    }
    
}
