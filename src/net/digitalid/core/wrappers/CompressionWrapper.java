package net.digitalid.core.wrappers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Encoding;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.errors.ShouldNeverHappenError;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.identity.SyntacticType;
import net.digitalid.core.io.Log;

/**
 * Wraps a block with the syntactic type {@code compression@core.digitalid.net} for encoding and decoding.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class CompressionWrapper extends Wrapper {
    
    /**
     * Stores the syntactic type {@code compression@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.map("compression@core.digitalid.net").load(1);
    
    /**
     * Indicates that no compression is used.
     */
    public static final byte NONE = 0;
    
    /**
     * Indicates that the ZLIB compression is used.
     */
    public static final byte ZLIB = 1;
    
    
    /**
     * Stores the element of this wrapper.
     * 
     * @invariant element == null || element.getType().isBasedOn(getType().getParameters().getNotNull(0)) : "The element is either null or based on the parameter of the block's type.";
     */
    private final @Nullable Block element;
    
    /**
     * Stores the algorithm of the compression.
     */
    private final byte algorithm;
    
    /**
     * Encodes the given element into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param element the element to encode into this new block.
     * @param algorithm indicates the algorithm of the compression.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(TYPE) : "The given type is based on the indicated syntactic type.";
     * @require element == null || element.getType().isBasedOn(type.getParameters().getNotNull(0)) : "The element is either null or based on the parameter of the given type.";
     * @require algorithm == NONE || algorithm == ZLIB : "The algorithm is either none or ZLIB.";
     */
    public CompressionWrapper(@Nonnull SemanticType type, @Nullable Block element, byte algorithm) {
        super(type);
        
        assert element == null || element.getType().isBasedOn(type.getParameters().getNonNullable(0)) : "The element is either null or based on the parameter of the given type.";
        assert algorithm == NONE || algorithm == ZLIB : "The algorithm is either none or ZLIB.";
        
        this.element = element;
        this.algorithm = algorithm;
    }
    
    /**
     * Encodes the given element into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param element the element to encode into this new block.
     * @param algorithm indicates the algorithm of the compression.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(TYPE) : "The given type is based on the indicated syntactic type.";
     * @require element == null || element.getType().isBasedOn(type.getParameters().getNotNull(0)) : "The element is either null or based on the parameter of the given type.";
     * @require algorithm == NONE || algorithm == ZLIB : "The algorithm is either none or ZLIB.";
     */
    public CompressionWrapper(@Nonnull SemanticType type, @Nullable Blockable element, byte algorithm) {
        this(type, Block.toBlock(element), algorithm);
    }
    
    /**
     * Wraps and decodes the given block.
     * 
     * @param block the block to be wrapped and decoded.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated syntactic type.";
     */
    public CompressionWrapper(@Nonnull Block block) throws InvalidEncodingException {
        super(block);
        
        this.algorithm = block.getByte(0);
        if (block.getLength() > 1) {
            final @Nonnull SemanticType parameter = block.getType().getParameters().getNonNullable(0);
            if (algorithm == NONE) {
                this.element = new Block(parameter, block, 1, block.getLength() - 1);
            } else if (algorithm == ZLIB) {
                try {
                    final @Nonnull Time start = new Time();
                    @Nonnull ByteArrayOutputStream uncompressed = new ByteArrayOutputStream(2 * block.getLength());
                    block.writeTo(1, new InflaterOutputStream(uncompressed), true);
                    this.element = new Block(parameter, uncompressed.toByteArray());
                    Log.verbose("Element with " + element.getLength() + " bytes uncompressed in " + start.ago().getValue() + " ms.");
                } catch (IOException exception) {
                    throw new InvalidEncodingException("The given block could not be decompressed.", exception);
                }
            } else {
                throw new InvalidEncodingException("The compression algorithm has to be either none or ZLIB.");
            }
        } else {
            this.element = null;
        }
    }
    
    
    /**
     * Returns the element of the wrapped block.
     * 
     * @return the element of the wrapped block.
     * 
     * @ensure element == null || element.getType().isBasedOn(getType().getParameters().getNotNull(0)) : "The element is either null or based on the parameter of the block's type.";
     */
    @Pure
    public @Nullable Block getElement() {
        return element;
    }
    
    /**
     * Returns the element of the wrapped block.
     * 
     * @return the element of the wrapped block.
     * 
     * @throws InvalidEncodingException if the element is null.
     * 
     * @ensure element.getType().isBasedOn(getType().getParameters().getNotNull(0)) : "The element is based on the parameter of the block's type.";
     */
    @Pure
    public @Nonnull Block getElementNotNull() throws InvalidEncodingException {
        if (element == null) throw new InvalidEncodingException("The compressed element is null.");
        return element;
    }
    
    /**
     * Returns whether the element is compressed.
     * 
     * @return whether the element is compressed.
     */
    @Pure
    public boolean isCompressed() {
        return algorithm != NONE;
    }
    
    
    @Pure
    @Override
    public @Nonnull SyntacticType getSyntacticType() {
        return TYPE;
    }
    
    /**
     * Stores the compression of the element.
     */
    private @Nullable ByteArrayOutputStream cache;
    
    /**
     * Returns the cached compression of the element.
     * 
     * @return the cached compression of the element.
     * 
     * @require element != null : "The element is not null.";
     * @require algorithm != NONE : "The element is compressed.";
     */
    @Pure
    private @Nonnull ByteArrayOutputStream getCache() {
        assert element != null : "The element is not null.";
        assert algorithm != NONE : "The element is compressed.";
        
        if (cache == null) {
            try {
                final @Nonnull Time start = new Time();
                cache = new ByteArrayOutputStream(element.getLength());
                element.writeTo(new DeflaterOutputStream(cache), true);
                Log.verbose("Element with " + element.getLength() + " bytes compressed in " + start.ago().getValue() + " ms.");
            } catch (@Nonnull IOException exception) {
                throw new ShouldNeverHappenError("The given element could not be compressed.", exception);
            }
        }
        return cache;
    }
    
    @Pure
    @Override
    protected int determineLength() {
        if (element == null) return 1;
        if (algorithm == NONE) return element.getLength() + 1;
        else return getCache().size() + 1;
    }
    
    @Pure
    @Override
    protected void encode(@Encoding @Nonnull Block block) {
        assert block.isEncoding() : "The given block is in the process of being encoded.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        
        block.setByte(0, algorithm);
        if (element != null) {
            if (algorithm == NONE) {
                element.writeTo(block, 1, block.getLength() - 1);
            } else {
                try {
                    getCache().writeTo(block.getOutputStream(1));
                } catch (@Nonnull IOException exception) {
                    throw new ShouldNeverHappenError("The compressed element could not be written.", exception);
                }
            }
        }
    }
    
}
