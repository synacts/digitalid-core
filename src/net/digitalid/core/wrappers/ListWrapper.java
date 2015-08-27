package net.digitalid.core.wrappers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.BasedOn;
import net.digitalid.core.annotations.Encoding;
import net.digitalid.core.annotations.Frozen;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Loaded;
import net.digitalid.core.annotations.NonEncoding;
import net.digitalid.core.annotations.NonNullableElements;
import net.digitalid.core.annotations.NullableElements;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.FreezableArrayList;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.ReadOnlyList;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.identity.SyntacticType;
import net.digitalid.core.storable.Storable;

/**
 * This class wraps a {@link ReadOnlyList list} for encoding and decoding a block of the syntactic type {@code list@core.digitalid.net}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class ListWrapper extends BlockBasedWrapper<ListWrapper> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the syntactic type {@code list@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.map("list@core.digitalid.net").load(1);
    
    /**
     * Stores the semantic type {@code semantic.list@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SEMANTIC = SemanticType.map("semantic.list@core.digitalid.net").load(TYPE);
    
    @Pure
    @Override
    public @Nonnull SyntacticType getSyntacticType() {
        return TYPE;
    }
    
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
            if (element != null && !element.getType().isBasedOn(parameter)) return false;
        }
        return true;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Elements –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
     * @throws InvalidEncodingException if the elements contain null.
     * 
     * @ensure basedOnParameter(getSemanticType(), elements) : "Each element is based on the parameter of the semantic type.";
     */
    @Pure
    public @Nonnull @NonNullableElements @Frozen ReadOnlyList<Block> getNonNullableElements() throws InvalidEncodingException {
        final @Nonnull ReadOnlyList<Block> elements = getNullableElements();
        if (elements.containsNull()) throw new InvalidEncodingException("The list contains null.");
        return elements;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
        
        assert elements.isFrozen() : "The elements are frozen.";
        assert basedOnParameter(type, elements) : "Each element is either null or based on the parameter of the semantic type.";
        
        this.elements = elements;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Utility –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factory of this class.
     */
    private static final Factory FACTORY = new Factory(SEMANTIC);
    
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
        return FACTORY.encodeNonNullable(new ListWrapper(type, elements));
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
    public static @Nonnull @NonEncoding Block encode(@Nonnull @Loaded @BasedOn("list@core.digitalid.net") SemanticType type, @Nonnull @NonEncoding Block... elements) {
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
    public static @Nonnull @NonEncoding <V extends Storable<V>> Block encode(@Nonnull @Loaded @BasedOn("list@core.digitalid.net") SemanticType type, @Nonnull V... elements) {
        final @Nonnull FreezableList<Block> list = FreezableArrayList.getWithCapacity(elements.length);
        for (final @Nullable V element : elements) list.add(Block.fromNullable(element));
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
    public static @Nonnull @NullableElements @Frozen ReadOnlyList<Block> decodeNullableElements(@Nonnull @NonEncoding @BasedOn("list@core.digitalid.net") Block block) throws InvalidEncodingException {
        return FACTORY.decodeNonNullable(block).getNullableElements();
    }
    
    /**
     * Decodes the given block. 
     * 
     * @param block the block to be decoded.
     * 
     * @return the non-nullable elements contained in the given block.
     */
    @Pure
    public static @Nonnull @NonNullableElements @Frozen ReadOnlyList<Block> decodeNonNullableElements(@Nonnull @NonEncoding @BasedOn("list@core.digitalid.net") Block block) throws InvalidEncodingException {
        return FACTORY.decodeNonNullable(block).getNonNullableElements();
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    protected void encode(@Nonnull @Encoding Block block) {
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for this class.
     */
    @Immutable
    public static class Factory extends BlockBasedWrapper.Factory<ListWrapper> {
        
        /**
         * Creates a new factory with the given type.
         * 
         * @param type the semantic type of the wrapper.
         */
        private Factory(@Nonnull @Loaded @BasedOn("list@core.digitalid.net") SemanticType type) {
            super(type);
        }
        
        @Pure
        @Override
        public @Nonnull ListWrapper decodeNonNullable(@Nonnull @NonEncoding Block block) throws InvalidEncodingException {
            final @Nonnull SemanticType parameter = block.getType().getParameters().getNonNullable(0);
            
            int offset = IntvarWrapper.decodeLength(block, 0);
            final int size = (int) IntvarWrapper.decodeValue(block, 0, offset);
            
            final @Nonnull FreezableList<Block> elements = FreezableArrayList.getWithCapacity(size);
            
            for (int i = 0; i < size; i++) {
                final int intvarLength = IntvarWrapper.decodeLength(block, offset);
                final int elementLength = (int) IntvarWrapper.decodeValue(block, offset, intvarLength);
                offset += intvarLength;
                if (elementLength == 0) {
                    elements.add(null);
                } else {
                    if (offset + elementLength > block.getLength()) throw new InvalidEncodingException("The subblock may not exceed the given block.");
                    elements.add(Block.get(parameter, block, offset, elementLength));
                    offset += elementLength;
                }
            }
            
            if (offset != block.getLength()) throw new InvalidEncodingException("The end of the last element has to match the end of the block.");
            
            return new ListWrapper(block.getType(), elements.freeze());
        }
        
    }
    
    @Pure
    @Override
    public @Nonnull Factory getFactory() {
        return new Factory(getSemanticType());
    }
    
}
