package net.digitalid.core.wrappers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Exposed;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.FreezableArray;
import net.digitalid.core.collections.ReadOnlyArray;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identifier.Identifier;
import net.digitalid.core.identifier.IdentifierClass;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.identity.SyntacticType;
import net.digitalid.core.wrappers.exceptions.UnexpectedEndOfFileException;
import net.digitalid.core.wrappers.exceptions.UnsupportedBlockLengthException;

/**
 * Wraps a block with the syntactic type {@code selfcontained@core.digitalid.net} for encoding and decoding.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class SelfcontainedWrapper extends BlockWrapper {
    
    /**
     * Stores the syntactic type {@code selfcontained@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.create("selfcontained@core.digitalid.net").load(0);
    
    /**
     * Stores the semantic type {@code default@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType DEFAULT = SemanticType.create("default@core.digitalid.net").load(TYPE);
    
    /**
     * Stores the semantic type {@code implementation.selfcontained@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType IMPLEMENTATION = SemanticType.create("implementation.selfcontained@core.digitalid.net").load(TupleWrapper.TYPE, SemanticType.IDENTIFIER, SemanticType.UNKNOWN);
    
    
    /**
     * Returns the given block in a selfcontained wrapper of the given type or null if the given block is null.
     * 
     * @param type the type of the returned selfcontained wrapper.
     * @param block the block to return in a selfcontained wrapper.
     * 
     * @return the given block in a selfcontained wrapper of the given type or null if the given block is null.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(TYPE) : "The given type is based on the indicated syntactic type.";
     */
    @Pure
    public static @Nullable Block toBlock(@Nonnull SemanticType type, @Nullable Block block) {
        return block == null ? null : new SelfcontainedWrapper(type, block).toBlock();
    }
    
    /**
     * Returns the element contained in the given block or null if the given block is null.
     * 
     * @param block the block containing the element which is to be returned.
     * 
     * @return the element contained in the given block or null if the given block is null.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated syntactic type.";
     */
    @Pure
    @NonCommitting
    public static @Nullable Block toElement(@Nullable Block block) throws SQLException, IOException, PacketException, ExternalException {
        return block == null ? null : new SelfcontainedWrapper(block).getElement();
    }
    
    
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
     * Encodes the given element into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param element the element to encode into a new block.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(TYPE) : "The given type is based on the indicated syntactic type.";
     */
    public SelfcontainedWrapper(@Nonnull SemanticType type, @Nonnull Block element) {
        super(type);
        
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<>(element.getType().toBlock(SemanticType.IDENTIFIER), element);
        this.tuple = new TupleWrapper(IMPLEMENTATION, elements.freeze()).toBlock();
        this.element = element;
    }
    
    /**
     * Encodes the given element into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param element the element to encode into a new block.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(TYPE) : "The given type is based on the indicated syntactic type.";
     */
    public SelfcontainedWrapper(@Nonnull SemanticType type, @Nonnull Blockable element) {
        this(type, element.toBlock());
    }
    
    /**
     * Wraps and decodes the given block.
     * 
     * @param block the block to wrap and decode.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated syntactic type.";
     */
    @NonCommitting
    public SelfcontainedWrapper(@Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(block);
        
        this.tuple = new Block(IMPLEMENTATION, block);
        final @Nonnull ReadOnlyArray<Block> elements = new TupleWrapper(tuple).getElementsNotNull(2);
        final @Nonnull Identifier identifier = IdentifierClass.create(elements.getNonNullable(0));
        this.element = elements.getNonNullable(1);
        element.setType(identifier.getIdentity().toSemanticType());
    }
    
    /**
     * Reads, wraps and decodes a selfcontained block from the given input stream and optionally closes the input stream afterwards.
     * 
     * @param inputStream the input stream to read from.
     * @param close whether the input stream shall be closed.
     * 
     * @ensure getType().equals(SELFCONTAINED) : "The type of the new wrapper is selfcontained.";
     */
    @NonCommitting
    public SelfcontainedWrapper(@Nonnull InputStream inputStream, boolean close) throws SQLException, IOException, PacketException, ExternalException {
        this(read(inputStream, close));
    }
    
    /**
     * Returns the element of the wrapped block.
     * 
     * @return the element of the wrapped block.
     */
    @Pure
    public @Nonnull Block getElement() {
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
        return tuple.getLength();
    }
    
    @Pure
    @Override
    protected void encode(@Exposed @Nonnull Block block) {
        assert block.isEncoding() : "The given block is in the process of being encoded.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        
        tuple.writeTo(block);
    }
    
    
    /**
     * Writes this block to the given output stream and optionally closes the output stream afterwards.
     * 
     * @param outputStream the output stream to writeTo to.
     * @param close whether the output stream shall be closed.
     */
    public void write(@Nonnull OutputStream outputStream, boolean close) throws IOException {
        toBlock().writeTo(outputStream, close);
    }
    
    /**
     * Reads a selfcontained block from the given input stream and optionally closes the input stream afterwards.
     * 
     * @param inputStream the input stream to read from.
     * @param close whether the input stream shall be closed.
     * 
     * @return the selfcontained block read from the input stream.
     * 
     * @ensure return.getType().equals(SELFCONTAINED) : "The returned block is selfcontained.";
     */
    private static @Nonnull Block read(@Nonnull InputStream inputStream, boolean close) throws InvalidEncodingException, IOException {
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
            
            return new Block(DEFAULT, bytes);
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
     * @require offset >= 0 && length >= 0 : "The offset and length is not negative.";
     * @require offset + length <= bytes.length : "The indicated section may not exceed the byte array.";
     */
    @SuppressWarnings("AssignmentToMethodParameter")
    private static void read(final @Nonnull InputStream inputStream, final @Nonnull byte[] bytes, int offset, int length) throws IOException {
        assert offset >= 0 && length >= 0 : "The offset and length is not negative.";
        assert offset + length <= bytes.length : "The indicated section may not exceed the byte array.";
        
        while (length > 0) {
            int read = inputStream.read(bytes, offset, length);
            if (read == -1) throw new UnexpectedEndOfFileException();
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
        if (value > (long) Integer.MAX_VALUE) throw new UnsupportedBlockLengthException();
        return (int) value;
    }
    
}
