package net.digitalid.core.handler.method;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.core.compression.Compression;
import net.digitalid.core.exceptions.request.RequestErrorCode;
import net.digitalid.core.exceptions.request.RequestException;
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
    private static final @Nonnull Map<@Nonnull SemanticType, @Nonnull Converter<? extends Method<?>, Signature<Compression<Pack>>>> converters = new ConcurrentHashMap<>();
    
    /**
     * Adds the given converter that recovers handlers for the given type.
     */
    @Impure
    @TODO(task = "Prevent that someone can overwrite an existing converter? (And the type could also be read from the given converter.)", date = "2016-11-07", author = Author.KASPAR_ETTER)
    public static void add(@Nonnull SemanticType type, @Nonnull Converter<? extends Method<?>, Signature<Compression<Pack>>> converter) {
        converters.put(type, converter);
    }
    
    @Pure
    public static @Nonnull Method<?> get(@Nonnull Signature<Compression<Pack>> signature) throws RequestException, RecoveryException {
        final @Nonnull Pack pack = signature.getObject().getObject();
        final @Nullable Converter<? extends Method<?>, Signature<Compression<Pack>>> converter = converters.get(pack.getType());
        if (converter == null) { throw RequestExceptionBuilder.withCode(RequestErrorCode.METHOD).withMessage(Strings.format("No method could be found for the type $.", pack.getType())).build(); }
        return pack.unpack(converter, signature);
    }
    
}
