package net.digitalid.core.exceptions.request;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Captured;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.elements.NullableElements;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.generation.Normalize;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This exception indicates an error in the encoding or content of a request.
 * 
 * @see RequestErrorCode
 */
@Immutable
@GenerateSubclass
@GenerateConverter
public abstract class RequestException extends ExternalException {
    
    /* -------------------------------------------------- Code -------------------------------------------------- */
    
    /**
     * Returns the error code of this request exception.
     */
    @Pure
    public abstract @Nonnull RequestErrorCode getCode();
    
    /* -------------------------------------------------- Decoded -------------------------------------------------- */
    
    /**
     * Returns whether this exception was decoded from a block.
     */
    @Pure
    public abstract @Default("true") boolean isDecoded();
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    // TODO: Make sure that the message is properly normalized.
    protected RequestException(@Nonnull @Normalize("\"(\" + code + \") \" + message") String message, @Nullable Exception cause, @Captured @Nonnull @NullableElements Object... arguments) {
        super(message, cause, arguments);
    }
    
    /**
     * Returns a new request exception with the given code, message and cause.
     */
    @Pure
    public static @Nonnull RequestException with(@Nonnull RequestErrorCode code, @Nonnull String message, @Nullable Exception cause, @Captured @Nonnull @NullableElements Object... arguments) {
        return new RequestExceptionSubclass(message, cause, arguments, code, false);
    }
    
    /**
     * Returns a new request exception with the given code and message.
     */
    @Pure
    public static @Nonnull RequestException with(@Nonnull RequestErrorCode code, @Nonnull String message, @Captured @Nonnull @NullableElements Object... arguments) {
        return with(code, message, null, arguments);
    }
    
    @Pure
    @Recover
    @TODO(task = "Think about how to best convert and recover request exceptions.", date = "2016-10-30", author = Author.KASPAR_ETTER)
    static @Nonnull RequestException with(@Nonnull RequestErrorCode code) {
        return new RequestExceptionSubclass("", null, new Object[0], code, true);
    }
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    // TODO: Remove the following code once the behavior is correctly generated.
    
//    /**
//     * Stores the semantic type {@code message.error.request@core.digitalid.net}.
//     */
//    private static final @Nonnull SemanticType MESSAGE = SemanticType.map("message.error.request@core.digitalid.net").load(StringWrapper.XDF_TYPE);
//    
//    /**
//     * Stores the semantic type {@code error.request@core.digitalid.net}.
//     */
//    public static final @Nonnull SemanticType TYPE = SemanticType.map("error.request@core.digitalid.net").load(TupleWrapper.XDF_TYPE, RequestErrorCode.TYPE, MESSAGE);
//    
//    /**
//     * The XDF converter for this class.
//     */
//    @Immutable
//    public static final class XDFConverter extends NonRequestingXDFConverter<RequestException, Object> {
//        
//        /**
//         * Creates a new XDF converter.
//         */
//        protected XDFConverter() {
//            super(TYPE);
//        }
//        
//        @Pure
//        @Override
//        public final @Nonnull Block encodeNonNullable(@Nonnull RequestException exception) {
//            final @Nonnull FreezableArray<Block> elements = FreezableArray.get(2);
//            elements.set(0, RequestErrorCode.XDF_CONVERTER.encodeNonNullable(exception.code));
//            elements.set(1, StringWrapper.encodeNonNullable(MESSAGE, exception.getMessage()));
//            return TupleWrapper.encode(TYPE, elements.freeze());
//        }
//        
//        @Pure
//        @Override
//        public final @Nonnull RequestException decodeNonNullable(@Nonnull Object none, @Nonnull Block block) throws InvalidEncodingException, InternalException {
//            Require.that(block.getType().isBasedOn(getType())).orThrow("The block is based on the type of this converter.");
//            
//            final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(block).getNonNullableElements(2);
//            final @Nonnull RequestErrorCode code = RequestErrorCode.XDF_CONVERTER.decodeNonNullable(none, elements.getNonNullable(0));
//            final @Nonnull String message = StringWrapper.decodeNonNullable(elements.getNonNullable(1));
//            return new RequestException(code, "A host responded with a request error. [" + message + "]", null, true);
//        }
//        
//    }
    
}
