package net.digitalid.core.wrappers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Exposed;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.FreezableArray;
import net.digitalid.core.collections.ReadOnlyArray;
import net.digitalid.core.collections.ReadOnlyList;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.identity.SyntacticType;
import net.digitalid.core.interfaces.Blockable;
import net.digitalid.core.interfaces.Immutable;

/**
 * Wraps a block with the syntactic type {@code tuple@core.digitalid.net} for encoding and decoding.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class TupleWrapper extends BlockWrapper implements Immutable {
    
    /**
     * Stores the syntactic type {@code tuple@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.create("tuple@core.digitalid.net").load(-1);
    
    /**
     * Returns whether the given elements are based on the corresponding parameter of the given type.
     * 
     * @param type the semantic type of the block.
     * @param elements the elements to check.
     * 
     * @return whether the given elements are based on the corresponding parameter of the given type.
     */
    @Pure
    public static boolean basedOnParameters(@Nonnull SemanticType type, @Nonnull ReadOnlyArray<Block> elements) {
        final @Nonnull ReadOnlyList<SemanticType> parameters = type.getParameters();
        if (elements.size() == 0 || elements.size() > parameters.size()) return false;
        for (int i = 0; i < elements.size(); i++) {
            final @Nullable Block element = elements.get(i);
            if (element != null && !element.getType().isBasedOn(parameters.getNotNull(i))) return false;
        }
        return true;
    }
    
    
    /**
     * Stores the elements of this tuple wrapper.
     * 
     * @invariant elements.isFrozen() : "The elements are frozen.";
     * @invariant basedOnParameters(getType(), elements) : "Each element is either null or based on the corresponding parameter of the block's type.";
     */
    private final @Nonnull ReadOnlyArray<Block> elements;
    
    /**
     * Encodes the given elements into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param elements the elements to encode into a new block.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(TYPE) : "The given type is based on the indicated syntactic type.";
     * @require basedOnParameters(type, elements) : "Each element is either null or based on the corresponding parameter of the given type.";
     * @require elements.isFrozen() : "The elements are frozen.";
     */
    public TupleWrapper(@Nonnull SemanticType type, @Nonnull ReadOnlyArray<Block> elements) {
        super(type);
        
        assert basedOnParameters(type, elements) : "Each element is either null or based on the corresponding parameter of the given type.";
        assert elements.isFrozen() : "The elements are frozen.";
        
        this.elements = elements;
    }
    
    /**
     * Encodes the given elements into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param elements the elements to encode into a new block.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(TYPE) : "The given type is based on the indicated syntactic type.";
     * @require basedOnParameters(type, elements) : "Each element is either null or based on the corresponding parameter of the given type.";
     */
    public TupleWrapper(@Nonnull SemanticType type, @Nonnull Block... elements) {
        this(type, new FreezableArray<>(elements).freeze());
    }
    
    /**
     * Encodes the given elements into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param elements the elements to encode into a new block.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(TYPE) : "The given type is based on the indicated syntactic type.";
     * @require basedOnParameters(type, elements.toBlock()) : "Each element is either null or based on the corresponding parameter of the given type.";
     */
    public TupleWrapper(@Nonnull SemanticType type, @Nonnull Blockable... elements) {
        super(type);
        
        final @Nonnull FreezableArray<Block> array = new FreezableArray<>(elements.length);
        for (int i = 0; i < elements.length; i++) array.set(i, Block.toBlock(elements[i]));
        this.elements = array.freeze();
        
        assert basedOnParameters(type, this.elements) : "Each element is either null or based on the corresponding parameter of the given type.";
    }
    
    /**
     * Wraps and decodes the given block.
     * 
     * @param block the block to wrap and decode.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated syntactic type.";
     */
    public TupleWrapper(@Nonnull Block block) throws InvalidEncodingException {
        super(block);
        
        final @Nonnull ReadOnlyList<SemanticType> parameters = block.getType().getParameters();
        final int size = parameters.size();
        final @Nonnull FreezableArray<Block> array = new FreezableArray<>(size);
        
        int offset = 0;
        final int length = block.getLength();
        
        for (int i = 0; i < size; i++) {
            if (offset >= length) break;
            int intvarLength = IntvarWrapper.decodeLength(block, offset);
            int elementLength = (int) IntvarWrapper.decodeValue(block, offset, intvarLength);
            offset += intvarLength;
            if (elementLength > 0) {
                if (offset + elementLength > block.getLength()) throw new InvalidEncodingException("The subblock may not exceed the given block.");
                array.set(i, new Block(parameters.getNotNull(i), block, offset, elementLength));
                offset += elementLength;
            }
        }
        
        elements = array.freeze();
    }
    
    
    /**
     * Returns the elements of the wrapped block.
     * 
     * @return the elements of the wrapped block.
     * 
     * @ensure elements.isFrozen() : "The elements are frozen.";
     * @ensure basedOnParameters(getType(), elements) : "Each element is either null or based on the corresponding parameter of the block's type.";
     */
    @Pure
    public @Nonnull ReadOnlyArray<Block> getElements() {
        return elements;
    }
    
    /**
     * Returns the elements of the wrapped block.
     * 
     * @param length the minimum number of elements.
     * 
     * @return the elements of the wrapped block.
     * 
     * @throws InvalidEncodingException if the number of elements is less than the given length.
     * 
     * @require length > 0 : "The length is positive.";
     * 
     * @ensure elements.isFrozen() : "The elements are frozen.";
     * @ensure elements.size() >= length : "The number of elements is at least the given length.";
     * @ensure basedOnParameters(getType(), elements) : "Each element is either null or based on the corresponding parameter of the block's type.";
     */
    @Pure
    public @Nonnull ReadOnlyArray<Block> getElements(int length) throws InvalidEncodingException {
        assert length > 0 : "The length is positive.";
        
        if (elements.size() < length) throw new InvalidEncodingException("The tuple contains not enough elements.");
        return elements;
    }
    
    /**
     * Returns the elements of the wrapped block.
     * 
     * @param length the minimum number of elements.
     * 
     * @return the elements of the wrapped block.
     * 
     * @throws InvalidEncodingException if the elements contain null or their number is less than the given length.
     * 
     * @require length > 0 : "The length is positive.";
     * 
     * @ensure elements.isFrozen() : "The elements are frozen.";
     * @ensure elements.doesNotContainNull() : "The elements do not contain null.";
     * @ensure elements.size() >= length : "The number of elements is at least the given length.";
     * @ensure basedOnParameters(getType(), elements) : "Each element is based on the corresponding parameter of the block's type.";
     */
    @Pure
    public @Nonnull ReadOnlyArray<Block> getElementsNotNull(int length) throws InvalidEncodingException {
        final @Nonnull ReadOnlyArray<Block> elements = getElements(length);
        if (!elements.doesNotContainNull()) throw new InvalidEncodingException("The tuple contains null.");
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
     * 
     * @require index >= 0 : "The index is non-negative.";
     */
    @Pure
    public boolean isElementNull(int index) throws InvalidEncodingException {
        assert index >= 0 : "The index is non-negative.";
        
        if (index >= elements.size()) throw new InvalidEncodingException("The tuple contains not enough elements.");
        return elements.get(index) == null;
    }
    
    /**
     * Returns whether the element at the given index is not null.
     * 
     * @param index the index of the element to be checked.
     * 
     * @return whether the element at the given index is not null.
     * 
     * @throws InvalidEncodingException if the number of elements is less than or equal to the given index.
     * 
     * @require index >= 0 : "The index is non-negative.";
     */
    @Pure
    public boolean isElementNotNull(int index) throws InvalidEncodingException {
        return !isElementNull(index);
    }
    
    /**
     * Returns the element at the given index.
     * 
     * @param index the index of the element to be returned.
     * 
     * @return the element at the given index.
     * 
     * @throws InvalidEncodingException if the number of elements is less than or equal to the given index.
     * 
     * @require index >= 0 : "The index is non-negative.";
     * 
     * @ensure element == null || element.getType().isBasedOn(getType().getParameters().getNotNull(index)) : "The element is either null or based on the corresponding parameter of the block's type.";
     */
    @Pure
    public @Nullable Block getElement(int index) throws InvalidEncodingException {
        assert index >= 0 : "The index is non-negative.";
        
        if (index >= elements.size()) throw new InvalidEncodingException("The tuple contains not enough elements.");
        return elements.get(index);
    }
    
    /**
     * Returns the element at the given index.
     * 
     * @param index the index of the element to be returned.
     * 
     * @return the element at the given index.
     * 
     * @throws InvalidEncodingException if the number of elements is less than or equal to the given index or the accessed element is null.
     * 
     * @require index >= 0 : "The index is non-negative.";
     * 
     * @ensure element.getType().isBasedOn(getType().getParameters().getNotNull(index)) : "The element is based on the corresponding parameter of the block's type.";
     */
    @Pure
    public @Nonnull Block getElementNotNull(int index) throws InvalidEncodingException {
        final @Nullable Block element = getElement(index);
        if (element == null) throw new InvalidEncodingException("The element at the given index is null.");
        return element;
    }
    
    /**
     * Returns the element of the given type.
     * 
     * @param type the type of the element which is to be returned.
     * 
     * @return the element of the given type.
     * 
     * @require toBlock().getType().getParameters().contains(type) : "The parameters of this tuple have to contain the given type.";
     * 
     * @ensure return == null || return.getType().isBasedOn(type) : "The returned block is either null or based on the given type.";
     */
    @Pure
    public @Nullable Block getElement(@Nonnull SemanticType type) {
        final @Nonnull ReadOnlyList<SemanticType> parameters = toBlock().getType().getParameters();
        assert parameters.contains(type) : "The parameters of this tuple have to contain the given type.";
        
        final @Nullable Block element = elements.get(parameters.indexOf(type));
        
        assert element == null || element.getType().isBasedOn(type) : "The returned block is either null or based on the given type.";
        
        return element;
    }
    
    /**
     * Returns the element of the given type.
     * 
     * @param type the type of the element which is to be returned.
     * 
     * @return the element of the given type.
     * 
     * @throws InvalidEncodingException if the element of the given type is null.
     * 
     * @require toBlock().getType().getParameters().contains(type) : "The parameters of this tuple have to contain the given type.";
     * 
     * @ensure return.getType().isBasedOn(type) : "The returned block is based on the given type.";
     */
    @Pure
    public @Nonnull Block getElementNotNull(@Nonnull SemanticType type) throws InvalidEncodingException {
        final @Nullable Block element = getElement(type);
        if (element == null) throw new InvalidEncodingException("The element of the given type is null.");
        return element;
    }
    
    
    @Pure
    @Override
    public @Nonnull SyntacticType getSyntacticType() {
        return TYPE;
    }
    
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
    protected void encode(@Exposed @Nonnull Block block) {
        assert block.isEncoding() : "The given block is in the process of being encoded.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        
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
    
}
