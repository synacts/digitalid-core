package net.digitalid.core.wrappers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Encoding;
import net.digitalid.core.annotations.Frozen;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonNullableElements;
import net.digitalid.core.annotations.NullableElements;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.FreezableArrayList;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.ReadOnlyList;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.identity.SyntacticType;

/**
 * Wraps a block with the syntactic type {@code list@core.digitalid.net} for encoding and decoding.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class ListWrapper extends Wrapper {
    
    /**
     * Stores the syntactic type {@code list@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.map("list@core.digitalid.net").load(1);
    
    /**
     * Returns whether the given elements are based on the parameter of the given type.
     * 
     * @param type the semantic type of the block.
     * @param elements the elements to check.
     * 
     * @return whether the given elements are based on the parameter of the given type.
     */
    @Pure
    public static boolean basedOnParameter(@Nonnull SemanticType type, @Nonnull ReadOnlyList<Block> elements) {
        final @Nonnull SemanticType parameter = type.getParameters().getNonNullable(0);
        for (final @Nullable Block element : elements) {
            if (element != null && !element.getType().isBasedOn(parameter)) return false;
        }
        return true;
    }
    
    
    /**
     * Stores the elements of this list wrapper.
     * 
     * @invariant basedOnParameter(toBlock().getType(), elements) : "Each element is either null or based on the parameter of the block's type.";
     */
    private final @Nonnull @Frozen ReadOnlyList<Block> elements;
    
    /**
     * Encodes the given elements into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param elements the elements to encode into a block.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(TYPE) : "The given type is based on the indicated syntactic type.";
     * @require basedOnParameter(type, elements) : "Each element is either null or based on the parameter of the given type.";
     * @require elements.isFrozen() : "The elements have to be frozen.";
     */
    public ListWrapper(@Nonnull SemanticType type, @Nonnull ReadOnlyList<Block> elements) {
        super(type);
        
        assert basedOnParameter(type, elements) : "Each element is either null or based on the parameter of the given type.";
        assert elements.isFrozen() : "The elements have to be frozen.";
        
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
     * @require basedOnParameter(type, elements) : "Each element is either null or based on the parameter of the given type.";
     */
    public ListWrapper(@Nonnull SemanticType type, @Nonnull Block... elements) {
        this(type, new FreezableArrayList<>(elements).freeze());
    }
    
    /**
     * Encodes the given elements into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param elements the elements to encode into a new block.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(TYPE) : "The given type is based on the indicated syntactic type.";
     * @require basedOnParameter(type, elements.toBlock()) : "Each element is either null or based on the parameter of the given type.";
     */
    public ListWrapper(@Nonnull SemanticType type, @Nonnull Blockable... elements) {
        super(type);
        
        final @Nonnull FreezableList<Block> list = new FreezableArrayList<>(elements.length);
        for (final @Nullable Blockable blockable : elements) list.add(Block.toBlock(blockable));
        this.elements = list.freeze();
        
        assert basedOnParameter(type, this.elements) : "Each element is either null or based on the parameter of the given type.";
    }
    
    /**
     * Wraps and decodes the given block.
     * 
     * @param block the block to wrap and decode.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated syntactic type.";
     */
    public ListWrapper(@Nonnull Block block) throws InvalidEncodingException {
        super(block);
        
        final @Nonnull SemanticType parameter = block.getType().getParameters().getNonNullable(0);
        
        int offset = IntvarWrapper.decodeLength(block, 0);
        final int size = (int) IntvarWrapper.decodeValue(block, 0, offset);
        
        final @Nonnull FreezableList<Block> list = new FreezableArrayList<>(size);
        
        for (int i = 0; i < size; i++) {
            final int intvarLength = IntvarWrapper.decodeLength(block, offset);
            final int elementLength = (int) IntvarWrapper.decodeValue(block, offset, intvarLength);
            offset += intvarLength;
            if (elementLength == 0) {
                list.add(null);
            } else {
                if (offset + elementLength > block.getLength()) throw new InvalidEncodingException("The subblock may not exceed the given block.");
                list.add(new Block(parameter, block, offset, elementLength));
                offset += elementLength;
            }
        }
        
        if (offset != block.getLength()) throw new InvalidEncodingException("The end of the last element has to match the end of the block.");
        
        elements = list.freeze();
    }
    
    /**
     * Returns the elements of the wrapped block.
     * 
     * @return the elements of the wrapped block.
     * 
     * @ensure basedOnParameter(toBlock().getType(), elements) : "Each element is either null or based on the parameter of the block's type.";
     */
    @Pure
    public @Nonnull @NullableElements @Frozen ReadOnlyList<Block> getElements() {
        return elements;
    }
    
    /**
     * Returns the elements of the wrapped block.
     * 
     * @return the elements of the wrapped block.
     * 
     * @throws InvalidEncodingException if the elements contain null.
     * 
     * @ensure basedOnParameters(toBlock().getType(), elements) : "Each element is based on the parameter of the block's type.";
     */
    @Pure
    public @Nonnull @NonNullableElements @Frozen ReadOnlyList<Block> getElementsNotNull() throws InvalidEncodingException {
        final @Nonnull ReadOnlyList<Block> elements = getElements();
        
        if (elements.containsNull()) throw new InvalidEncodingException("The list contains null.");
        
        return elements;
    }
    
    
    @Pure
    @Override
    public @Nonnull SyntacticType getSyntacticType() {
        return TYPE;
    }
    
    @Pure
    @Override
    protected int determineLength() {
        int length = IntvarWrapper.determineLength(elements.size());
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
    protected void encode(@Encoding @Nonnull Block block) {
        assert block.isEncoding() : "The given block is in the process of being encoded.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        
        final int size = elements.size();
        int offset = IntvarWrapper.determineLength(size);
        IntvarWrapper.encode(block, 0, offset, size);
        
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
