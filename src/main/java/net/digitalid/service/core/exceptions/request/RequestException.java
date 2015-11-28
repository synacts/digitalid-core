package net.digitalid.service.core.exceptions.request;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.StringWrapper;
import net.digitalid.service.core.block.wrappers.TupleWrapper;
import net.digitalid.service.core.converter.xdf.AbstractNonRequestingXDFConverter;
import net.digitalid.service.core.converter.xdf.XDF;
import net.digitalid.service.core.exceptions.external.encoding.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.system.exceptions.InternalException;
import net.digitalid.utility.system.logger.Log;

/**
 * This exception indicates an error in the encoding or content of a request.
 * 
 * @see RequestErrorCode
 */
@Immutable
public final class RequestException extends Exception implements XDF<RequestException, Object> {
    
    /* -------------------------------------------------- Code -------------------------------------------------- */
    
    /**
     * Stores the error code of this request exception.
     */
    private final @Nonnull RequestErrorCode code;
    
    /**
     * Returns the error code of this request exception.
     * 
     * @return the error code of this request exception.
     */
    @Pure
    public final @Nonnull RequestErrorCode getCode() {
        return code;
    }
    
    /* -------------------------------------------------- Decoded -------------------------------------------------- */
    
    /**
     * Stores whether this exception was decoded from a block.
     */
    private final boolean decoded;
    
    /**
     * Returns whether this exception was decoded from a block.
     * 
     * @return whether this exception was decoded from a block.
     */
    @Pure
    public final boolean isDecoded() {
        return decoded;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new request exception with the given parameters.
     * 
     * @param code the error code of the request exception.
     * @param message a string explaining the request exception.
     * @param cause the cause of the request exception, if available.
     * @param decoded whether the exception was decoded from a block.
     */
    protected RequestException(@Nonnull RequestErrorCode code, @Nonnull String message, @Nullable Exception cause, boolean decoded) {
        super("(" + code.getName() + ") " + message, cause);
        
        this.code = code;
        this.decoded = decoded;
        
        if (decoded) { Log.warning("A request exception was decoded.", this); }
        else { Log.warning("A request exception occurred.", this); }
    }
    
    /**
     * Returns a new request exception with the given code, message and cause.
     * 
     * @param code the error code of the request exception.
     * @param message a string explaining the request exception.
     * @param cause the cause of the request exception, if available.
     * 
     * @return a new request exception with the given code, message and cause.
     */
    @Pure
    public static @Nonnull RequestException get(@Nonnull RequestErrorCode error, @Nonnull String message, @Nullable Exception cause) {
        return new RequestException(error, message, cause, false);
    }
    
    /**
     * Returns a new request exception with the given code and message.
     * 
     * @param code the error code of the request exception.
     * @param message a string explaining the request exception.
     * 
     * @return a new request exception with the given code and message.
     */
    @Pure
    public static @Nonnull RequestException get(@Nonnull RequestErrorCode error, @Nonnull String message) {
        return get(error, message, null);
    }
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code message.error.request@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MESSAGE = SemanticType.map("message.error.request@core.digitalid.net").load(StringWrapper.XDF_TYPE);
    
    /**
     * Stores the semantic type {@code error.request@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("error.request@core.digitalid.net").load(TupleWrapper.XDF_TYPE, RequestErrorCode.TYPE, MESSAGE);
    
    /**
     * The XDF converter for this class.
     */
    @Immutable
    public static final class XDFConverter extends AbstractNonRequestingXDFConverter<RequestException, Object> {
        
        /**
         * Creates a new XDF converter.
         */
        protected XDFConverter() {
            super(TYPE);
        }
        
        @Pure
        @Override
        public final @Nonnull Block encodeNonNullable(@Nonnull RequestException exception) {
            final @Nonnull FreezableArray<Block> elements = FreezableArray.get(2);
            elements.set(0, RequestErrorCode.XDF_CONVERTER.encodeNonNullable(exception.code));
            elements.set(1, StringWrapper.encodeNonNullable(MESSAGE, exception.getMessage()));
            return TupleWrapper.encode(TYPE, elements.freeze());
        }
        
        @Pure
        @Override
        public final @Nonnull RequestException decodeNonNullable(@Nonnull Object none, @Nonnull Block block) throws InvalidEncodingException, InternalException {
            assert block.getType().isBasedOn(getType()) : "The block is based on the type of this converter.";
            
            final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(block).getNonNullableElements(2);
            final @Nonnull RequestErrorCode code = RequestErrorCode.XDF_CONVERTER.decodeNonNullable(none, elements.getNonNullable(0));
            final @Nonnull String message = StringWrapper.decodeNonNullable(elements.getNonNullable(1));
            return new RequestException(code, "A host responded with a request error. [" + message + "]", null, true);
        }
        
    }
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull XDFConverter XDF_CONVERTER = new XDFConverter();
    
    @Pure
    @Override
    public final @Nonnull XDFConverter getXDFConverter() {
        return XDF_CONVERTER;
    }
    
}
