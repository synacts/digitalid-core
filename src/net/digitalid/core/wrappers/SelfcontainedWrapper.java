package net.digitalid.core.wrappers;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.BasedOn;
import net.digitalid.core.annotations.Encoding;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Loaded;
import net.digitalid.core.annotations.Locked;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonEncoding;
import net.digitalid.core.annotations.NonNegative;
import net.digitalid.core.annotations.NonNullableElements;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.ReadOnlyArray;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identifier.Identifier;
import net.digitalid.core.identifier.IdentifierClass;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.identity.SyntacticType;
import net.digitalid.core.storable.Storable;
import net.digitalid.core.wrappers.exceptions.UnexpectedEndOfFileException;
import net.digitalid.core.wrappers.exceptions.UnsupportedBlockLengthException;

/**
 * This class wraps an {@link Block element} for encoding and decoding a block of the syntactic type {@code selfcontained@core.digitalid.net}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class SelfcontainedWrapper extends BlockWrapper<SelfcontainedWrapper> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the syntactic type {@code selfcontained@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.map("selfcontained@core.digitalid.net").load(0);
    
    /**
     * Stores the semantic type {@code default@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType DEFAULT = SemanticType.map("default@core.digitalid.net").load(TYPE);
    
    /**
     * Stores the semantic type {@code semantic.selfcontained@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SEMANTIC = SemanticType.map("semantic.selfcontained@core.digitalid.net").load(TYPE);
    
    /**
     * Stores the semantic type {@code implementation.selfcontained@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType IMPLEMENTATION = SemanticType.map("implementation.selfcontained@core.digitalid.net").load(TupleWrapper.TYPE, SemanticType.IDENTIFIER, SemanticType.UNKNOWN);
    
    @Pure
    @Override
    public @Nonnull SyntacticType getSyntacticType() {
        return TYPE;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Element –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the tuple of this wrapper.
     * 
     * @invariant tuple.getType().equals(IMPLEMENTATION) : "The tuple consists of an identifier and an element.";
     */
    private final @Nonnull Block tuple;
   
    /**
     * Stores the element of this wrapper.
     */
    private final @Nonnull Block element;
    
    /**
     * Returns the element of this wrapper.
     * 
     * @return the element of this wrapper.
     */
    @Pure
    public @Nonnull Block getElement() {
        return element;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructors –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new selfcontained wrapper with the given type and element.
     * 
     * @param type the semantic type of the new selfcontained wrapper.
     * @param element the element of the new selfcontained wrapper.
     */
    private SelfcontainedWrapper(@Nonnull @Loaded @BasedOn("selfcontained@core.digitalid.net") SemanticType type, @Nonnull @NonEncoding Block element) {
        super(type);
        
        this.tuple = TupleWrapper.encode(IMPLEMENTATION, element.getType().toBlock(SemanticType.IDENTIFIER), element);
        this.element = element;
    }
    
    /**
     * Creates a new selfcontained wrapper from the given block.
     * 
     * @param block the block that contains the identifier and the element.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated syntactic type.";
     */
    @Locked
    @NonCommitting
    private SelfcontainedWrapper(@Nonnull @NonEncoding Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(block.getType());
        
        this.tuple = Block.get(IMPLEMENTATION, block);
        final @Nonnull @NonNullableElements ReadOnlyArray<Block> elements = TupleWrapper.decode(tuple).getNonNullableElements(2);
        final @Nonnull Identifier identifier = IdentifierClass.create(elements.getNonNullable(0));
        this.element = elements.getNonNullable(1);
        element.setType(identifier.getIdentity().toSemanticType());
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Utility –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factory of this class.
     */
    private static final Factory FACTORY = new Factory(SEMANTIC);
    
    /**
     * Encodes the given element into a new non-nullable selfcontained block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param element the element to encode into the new block.
     * 
     * @return a new non-nullable selfcontained block containing the given element.
     */
    @Pure
    public static @Nonnull @NonEncoding <V extends Storable<V>> Block encodeNonNullable(@Nonnull @Loaded @BasedOn("selfcontained@core.digitalid.net") SemanticType type, @Nonnull V element) {
        return FACTORY.encodeNonNullable(new SelfcontainedWrapper(type, Block.fromNonNullable(element)));
    }
    
    /**
     * Encodes the given element into a new nullable selfcontained block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param element the element to encode into the new block.
     * 
     * @return a new nullable selfcontained block containing the given element.
     */
    @Pure
    public static @Nullable @NonEncoding <V extends Storable<V>> Block encodeNullable(@Nonnull @Loaded @BasedOn("selfcontained@core.digitalid.net") SemanticType type, @Nullable V element) {
        return element == null ? null : encodeNonNullable(type, element);
    }
    
    /**
     * Decodes the given non-nullable block. 
     * 
     * @param block the block to be decoded.
     * 
     * @return the element contained in the given block.
     */
    @Pure
    @Locked
    @NonCommitting
    public static @Nonnull @NonEncoding Block decodeNonNullable(@Nonnull @NonEncoding @BasedOn("selfcontained@core.digitalid.net") Block block) throws SQLException, IOException, PacketException, ExternalException {
        return FACTORY.decodeNonNullable(block).element;
    }
    
    /**
     * Decodes the given nullable block. 
     * 
     * @param block the block to be decoded.
     * 
     * @return the element contained in the given block.
     */
    @Pure
    @Locked
    @NonCommitting
    public static @Nullable @NonEncoding Block decodeNullable(@Nullable @NonEncoding @BasedOn("selfcontained@core.digitalid.net") Block block) throws SQLException, IOException, PacketException, ExternalException {
        return block == null ? null : decodeNonNullable(block);
    }
    
    /**
     * Reads, wraps and decodes a selfcontained block from the given input stream and optionally closes the input stream afterwards.
     * 
     * @param inputStream the input stream to read from.
     * @param close whether the input stream shall be closed.
     * 
     * @return the element of the selfcontained block from the given input stream.
     */
    @Pure
    @Locked
    @NonCommitting
    public static @Nonnull @NonEncoding Block decodeBlockFrom(@Nonnull InputStream inputStream, boolean close) throws SQLException, IOException, PacketException, ExternalException {
        return decodeNonNullable(readBlockFrom(inputStream, close));
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    protected int determineLength() {
        return tuple.getLength();
    }
    
    @Pure
    @Override
    protected void encode(@Nonnull @Encoding Block block) {
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        
        tuple.writeTo(block);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for this class.
     */
    @Immutable
    public static class Factory extends BlockWrapper.Factory<SelfcontainedWrapper> {
        
        /**
         * Creates a new factory with the given type.
         * 
         * @param type the semantic type of the wrapper.
         */
        private Factory(@Nonnull @Loaded @BasedOn("selfcontained@core.digitalid.net") SemanticType type) {
            super(type);
        }
        
        @Pure
        @Locked
        @Override
        @NonCommitting
        public @Nonnull SelfcontainedWrapper decodeNonNullable(@Nonnull @NonEncoding Block block) throws SQLException, IOException, PacketException, ExternalException {
            return new SelfcontainedWrapper(block);
        }
        
    }
    
    @Pure
    @Override
    public @Nonnull Factory getFactory() {
        return new Factory(getSemanticType());
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Reading –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Reads a selfcontained block from the given input stream and optionally closes the input stream afterwards.
     * 
     * @param inputStream the input stream to read from.
     * @param close whether the input stream shall be closed.
     * 
     * @return the selfcontained block read from the input stream.
     * 
     * @ensure return.getType().equals(DEFAULT) : "The returned block is selfcontained.";
     */
    private static @Nonnull Block readBlockFrom(@Nonnull InputStream inputStream, boolean close) throws InvalidEncodingException, IOException {
        try {
            final @Nonnull byte[] intvarOfIdentifier = new byte[8];
            read(inputStream, intvarOfIdentifier, 0, 1);
            
            final int intvarOfIdentifierLength = IntvarWrapper.decodeLength(intvarOfIdentifier);
            read(inputStream, intvarOfIdentifier, 1, intvarOfIdentifierLength - 1);
            
            final int identifierLength = longToInt(IntvarWrapper.decodeValue(intvarOfIdentifier, intvarOfIdentifierLength));
            final @Nonnull byte[] identifier = new byte[identifierLength];
            read(inputStream, identifier, 0, identifierLength);
            
            final @Nonnull byte[] intvarOfElement = new byte[8];
            read(inputStream, intvarOfElement, 0, 1);
            
            final int intvarOfElementLength = IntvarWrapper.decodeLength(intvarOfElement);
            read(inputStream, intvarOfElement, 1, intvarOfElementLength - 1);
            
            final int elementLength = longToInt(IntvarWrapper.decodeValue(intvarOfElement, intvarOfElementLength));
            final int length = longToInt((long) intvarOfIdentifierLength + (long) identifierLength + (long) intvarOfElementLength + (long) elementLength);
            final @Nonnull byte[] bytes = new byte[length];
            
            System.arraycopy(intvarOfIdentifier, 0, bytes, 0, intvarOfIdentifierLength);
            System.arraycopy(identifier, 0, bytes, intvarOfIdentifierLength, identifierLength);
            System.arraycopy(intvarOfElement, 0, bytes, intvarOfIdentifierLength + identifierLength, intvarOfElementLength);
            read(inputStream, bytes, intvarOfIdentifierLength + identifierLength + intvarOfElementLength, elementLength);
            
            return Block.get(DEFAULT, bytes);
        } finally {
            if (close) inputStream.close();
        }
    }
    
    /**
     * Reads the given amount of bytes from the input stream and stores them into the given byte array at the given offset.
     * 
     * @param inputStream the input stream to read from.
     * @param bytes the byte array into which the input is read.
     * @param offset the offset in the byte array at which the data is written.
     * @param length the number of bytes that is read from the input stream.
     * 
     * @throws UnexpectedEndOfFileException if the end of the input stream has been reached before the indicated data could be read.
     * 
     * @require offset + length <= bytes.length : "The indicated section may not exceed the byte array.";
     */
    @SuppressWarnings("AssignmentToMethodParameter")
    private static void read(final @Nonnull InputStream inputStream, final @Nonnull byte[] bytes, @NonNegative int offset, @NonNegative int length) throws IOException {
        assert offset >= 0 && length >= 0 : "The offset and length are non-negative.";
        assert offset + length <= bytes.length : "The indicated section may not exceed the byte array.";
        
        while (length > 0) {
            final int read = inputStream.read(bytes, offset, length);
            if (read == -1) throw UnexpectedEndOfFileException.get();
            offset += read;
            length -= read;
        }
    }
    
    /**
     * Casts a long to an int and throws an exception if the value is too large.
     * 
     * @param value the value to cast.
     * 
     * @return the safely casted value.
     * 
     * @throws UnsupportedBlockLengthException if the value is larger than the maximum integer.
     */
    private static int longToInt(long value) throws UnsupportedBlockLengthException {
        if (value > (long) Integer.MAX_VALUE) throw UnsupportedBlockLengthException.get();
        return (int) value;
    }
    
}
