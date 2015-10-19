package net.digitalid.service.core.wrappers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.annotations.BasedOn;
import net.digitalid.service.core.annotations.Encoding;
import net.digitalid.service.core.annotations.Loaded;
import net.digitalid.service.core.annotations.NonEncoding;
import net.digitalid.service.core.blockable.Blockable;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.SyntacticType;
import net.digitalid.utility.annotations.math.NonNegative;
import net.digitalid.utility.annotations.math.Positive;
import net.digitalid.utility.annotations.reference.Captured;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.elements.NullableElements;
import net.digitalid.utility.collections.annotations.freezable.Frozen;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.collections.readonly.ReadOnlyList;

/**
 * This class wraps a {@link ReadOnlyArray tuple} for encoding and decoding a block of the syntactic type {@code tuple@core.digitalid.net}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public final class TupleWrapper extends BlockBasedWrapper<TupleWrapper> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the syntactic type {@code tuple@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.map("tuple@core.digitalid.net").load(-1);
    
    /**
     * Stores the semantic type {@code semantic.tuple@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SEMANTIC = SemanticType.map("semantic.tuple@core.digitalid.net").load(TYPE);
    
    @Pure
    @Override
    public @Nonnull SyntacticType getSyntacticType() {
        return TYPE;
    }
    
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
        if (elements.size() == 0 || elements.size() > parameters.size()) return false;
        for (int i = 0; i < elements.size(); i++) {
            final @Nullable Block element = elements.getNullable(i);
            if (element != null && !element.getType().isBasedOn(parameters.getNonNullable(i))) return false;
        }
        return true;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Elements –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    public @Nonnull @NonNullableElements @Frozen ReadOnlyArray<Block> getNonNullableElements() throws InvalidEncodingException {
        if (elements.containsNull()) throw new InvalidEncodingException("The tuple contains null.");
        return elements;
    }
    
    /**
     * Returns the nullable elements of this tuple wrapper.
     * 
     * @param length the minimum number of elements.
     * 
     * @return the nullable elements of this tuple wrapper.
     * 
     * @throws InvalidEncodingException if the number of elements is less than the given length.
     * 
     * @ensure elements.size() >= length : "The number of elements is at least the given length.";
     * @ensure basedOnParameters(getSemanticType(), elements) : "Each element is either null or based on the corresponding parameter of the semantic type.";
     */
    @Pure
    public @Nonnull @NullableElements @Frozen ReadOnlyArray<Block> getNullableElements(@Positive int length) throws InvalidEncodingException {
        assert length > 0 : "The length is positive.";
        
        if (elements.size() < length) throw new InvalidEncodingException("The tuple contains not enough elements.");
        return elements;
    }
    
    /**
     * Returns the non-nullable elements of this tuple wrapper.
     * 
     * @param length the minimum number of elements.
     * 
     * @return the non-nullable elements of this tuple wrapper.
     * 
     * @throws InvalidEncodingException if the elements contain null or their number is less than the given length.
     * 
     * @ensure elements.size() >= length : "The number of elements is at least the given length.";
     * @ensure basedOnParameters(getSemanticType(), elements) : "Each element is based on the corresponding parameter of the semantic type.";
     */
    @Pure
    public @Nonnull @NonNullableElements @Frozen ReadOnlyArray<Block> getNonNullableElements(@Positive int length) throws InvalidEncodingException {
        final @Nonnull ReadOnlyArray<Block> elements = getNullableElements(length);
        if (elements.containsNull()) throw new InvalidEncodingException("The tuple contains null.");
        return elements;
    }
    
    /**
     * Returns whether the element at the given index is null.
     * 
     * @param index the index of the element to be checked.
     * 
     * @return whether the element at the given index is null.
     * 
     * @throws InvalidEncodingException if the number of elements is less than or equal to the given index.
     */
    @Pure
    public boolean isElementNull(@NonNegative int index) throws InvalidEncodingException {
        assert index >= 0 : "The index is non-negative.";
        
        if (index >= elements.size()) throw new InvalidEncodingException("The tuple contains not enough elements.");
        return elements.getNullable(index) == null;
    }
    
    /**
     * Returns the nullable element at the given index.
     * 
     * @param index the index of the element to be returned.
     * 
     * @return the nullable element at the given index.
     * 
     * @throws InvalidEncodingException if the number of elements is less than or equal to the given index.
     * 
     * @ensure element == null || element.getType().isBasedOn(getSemanticType().getParameters().getNonNullable(index)) : "The element is either null or based on the corresponding parameter of the semantic type.";
     */
    @Pure
    public @Nullable Block getNullableElement(@NonNegative int index) throws InvalidEncodingException {
        assert index >= 0 : "The index is non-negative.";
        
        if (index >= elements.size()) throw new InvalidEncodingException("The tuple contains not enough elements.");
        return elements.getNullable(index);
    }
    
    /**
     * Returns the non-nullable element at the given index.
     * 
     * @param index the index of the element to be returned.
     * 
     * @return the non-nullable element at the given index.
     * 
     * @throws InvalidEncodingException if the number of elements is less than or equal to the given index or the accessed element is null.
     * 
     * @ensure element.getType().isBasedOn(getSemanticType().getParameters().getNonNullable(index)) : "The element is based on the corresponding parameter of the semantic type.";
     */
    @Pure
    public @Nonnull Block getNonNullableElement(@NonNegative int index) throws InvalidEncodingException {
        final @Nullable Block element = TupleWrapper.this.getNullableElement(index);
        if (element == null) throw new InvalidEncodingException("The element at the given index is null.");
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
        assert parameters.contains(type) : "The parameters of this tuple contain the given type.";
        
        final @Nullable Block element = elements.getNullable(parameters.indexOf(type));
        
        assert element == null || element.getType().isBasedOn(type) : "The returned block is either null or based on the given type.";
        
        return element;
    }
    
    /**
     * Returns the non-nullable element of the given type.
     * 
     * @param type the type of the element which is to be returned.
     * 
     * @return the non-nullable element of the given type.
     * 
     * @throws InvalidEncodingException if the element of the given type is null.
     * 
     * @require getSemanticType().getParameters().contains(type) : "The parameters of this tuple contain the given type.";
     * 
     * @ensure return.getType().isBasedOn(type) : "The returned block is based on the given type.";
     */
    @Pure
    public @Nonnull Block getNonNullableElement(@Nonnull SemanticType type) throws InvalidEncodingException {
        final @Nullable Block element = getNullableElement(type);
        if (element == null) throw new InvalidEncodingException("The element of the given type is null.");
        return element;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
        
        assert elements.isFrozen() : "The elements are frozen.";
        assert basedOnParameters(type, elements) : "Each element is either null or based on the corresponding parameter of the given type.";
        
        this.elements = elements;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Utility –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factory of this class.
     */
    private static final @Nonnull Factory FACTORY = new Factory(SEMANTIC);
    
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
        return FACTORY.encodeNonNullable(new TupleWrapper(type, elements));
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
    public static @Nonnull @NonEncoding Block encode(@Nonnull @Loaded @BasedOn("tuple@core.digitalid.net") SemanticType type, @Nonnull Blockable<?, ?>... elements) {
        final @Nonnull FreezableArray<Block> array = FreezableArray.get(elements.length);
        for (int i = 0; i < elements.length; i++) array.set(i, elements[i].getFactory().encodeNullableWithCast(elements[i]));
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
    public static @Nonnull TupleWrapper decode(@Nonnull @NonEncoding @BasedOn("tuple@core.digitalid.net") Block block) throws InvalidEncodingException {
        return FACTORY.decodeNonNullable(block);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    protected int determineLength() {
        int length = 0;
        for (final @Nullable Block element : elements) {
            if (element == null) {
                length += 1;
            } else {
                final int elementLength = element.getLength();
                final int intvarLength = IntvarWrapper.determineLength(elementLength);
                length += intvarLength + elementLength;
            }
        }
        return length;
    }
    
    @Pure
    @Override
    protected void encode(@Nonnull @Encoding Block block) {
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        
        int offset = 0;
        
        for (final @Nullable Block element : elements) {
            if (element == null) {
                offset += 1;
            } else {
                final int elementLength = element.getLength();
                final int intvarLength = IntvarWrapper.determineLength(elementLength);
                
                IntvarWrapper.encode(block, offset, intvarLength, elementLength);
                offset += intvarLength;
                element.writeTo(block, offset, elementLength);
                offset += elementLength;
            }
        }
        
        assert offset == block.getLength() : "The whole block should now be encoded.";
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for this class.
     */
    @Immutable
    public static final class Factory extends BlockBasedWrapper.Factory<TupleWrapper> {
        
        /**
         * Creates a new factory with the given type.
         * 
         * @param type the semantic type of the wrapper.
         */
        private Factory(@Nonnull @Loaded @BasedOn("tuple@core.digitalid.net") SemanticType type) {
            super(type);
        }
        
        @Pure
        @Override
        public @Nonnull TupleWrapper decodeNonNullable(@Nonnull @NonEncoding @BasedOn("tuple@core.digitalid.net") Block block) throws InvalidEncodingException {
            final @Nonnull @NonNullableElements ReadOnlyList<SemanticType> parameters = block.getType().getParameters();
            final int size = parameters.size();
            final @Nonnull FreezableArray<Block> elements = FreezableArray.get(size);
            
            int offset = 0;
            final int length = block.getLength();
            
            for (int i = 0; i < size; i++) {
                if (offset >= length) break;
                int intvarLength = IntvarWrapper.decodeLength(block, offset);
                int elementLength = (int) IntvarWrapper.decodeValue(block, offset, intvarLength);
                offset += intvarLength;
                if (elementLength > 0) {
                    if (offset + elementLength > block.getLength()) throw new InvalidEncodingException("The subblock may not exceed the given block.");
                    elements.set(i, Block.get(parameters.getNonNullable(i), block, offset, elementLength));
                    offset += elementLength;
                }
            }
            
            return new TupleWrapper(block.getType(), elements.freeze());
        }
        
    }
    
    @Pure
    @Override
    public @Nonnull Factory getFactory() {
        return new Factory(getSemanticType());
    }
    
}
