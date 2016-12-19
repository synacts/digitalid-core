package net.digitalid.core.handler.method;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.conversion.converter.Converter;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.core.exceptions.request.RequestErrorCode;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.selfcontained.Selfcontained;
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
    public static @Nonnull Method<?> get(@Nonnull Selfcontained selfcontained, @Nonnull Signature<?> signature) throws ExternalException {
        final @Nullable Converter<? extends Method<?>, ? /* @Nonnull Signature<?> */> converter = converters.get(selfcontained.getType());
        if (converter == null) { throw RequestException.with(RequestErrorCode.METHOD, "No method could be found for the type $.", selfcontained.getType()); }
        final @Nullable Method<?> method = selfcontained.recover(converter, null /* signature */);
        if (method == null) { throw RequestException.with(RequestErrorCode.METHOD, "The method could not be recovered for the type $.", selfcontained.getType()); }
        return method;
    }
    
}
