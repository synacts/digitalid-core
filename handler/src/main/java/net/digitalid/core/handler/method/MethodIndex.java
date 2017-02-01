package net.digitalid.core.handler.method;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.core.exceptions.request.RequestErrorCode;
import net.digitalid.core.exceptions.request.RequestExceptionBuilder;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.pack.Pack;
import net.digitalid.core.signature.Signature;

/**
 * All methods have to register themselves at this index.
 */
@Utility
public abstract class MethodIndex {
    
    /**
     * Maps method types to the converter that recovers the handler for that type.
     */
    private static final @Nonnull Map<@Nonnull SemanticType, @Nonnull Converter<? extends Method<?>, ? /* @Nonnull Signature<?> */>> converters = new ConcurrentHashMap<>();
    
    /**
     * Adds the given converter that recovers handlers for the given type.
     */
    @Impure
    @TODO(task = "Prevent that someone can overwrite an existing converter? (And the type could also be read from the given converter.)", date = "2016-11-07", author = Author.KASPAR_ETTER)
    public static void add(@Nonnull SemanticType type, @Nonnull Converter<? extends Method<?>, ? /* @Nonnull Signature<?> */> converter) {
        converters.put(type, converter);
    }
    
    @Pure
    @TODO(task = "Provide only the signature but with an appropriate generic type?", date = "2016-11-07", author = Author.KASPAR_ETTER)
    public static @Nonnull Method<?> get(@Nonnull Pack pack, @Nonnull Signature<?> signature) throws ExternalException {
        final @Nullable Converter<? extends Method<?>, ? /* @Nonnull Signature<?> */> converter = converters.get(pack.getType());
        if (converter == null) { throw RequestExceptionBuilder.withCode(RequestErrorCode.METHOD).withMessage(Strings.format("No method could be found for the type $.", pack.getType())).build(); }
        final @Nullable Method<?> method = pack.unpack(converter, null /* signature */);
        if (method == null) { throw RequestExceptionBuilder.withCode(RequestErrorCode.METHOD).withMessage(Strings.format("The method could not be recovered for the type $.", pack.getType())).build(); }
        return method;
    }
    
}
