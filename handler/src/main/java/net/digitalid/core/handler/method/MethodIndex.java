package net.digitalid.core.handler.method;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.logging.Log;
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
     * Adds the given converter to recover the methods of its type.
     */
    @Impure
    public static void add(@Nonnull Converter<? extends Method<?>, Signature<Compression<Pack>>> converter) {
        final @Nonnull SemanticType type = SemanticType.map(converter);
        converters.put(type, converter);
        Log.debugging("Registered a converter for the type $.", type);
    }
    
    @Pure
    public static @Nonnull Method<?> get(@Nonnull Signature<Compression<Pack>> signature) throws RequestException, RecoveryException {
        final @Nonnull Pack pack = signature.getObject().getObject();
        final @Nullable Converter<? extends Method<?>, Signature<Compression<Pack>>> converter = converters.get(pack.getType());
        if (converter == null) { throw RequestExceptionBuilder.withCode(RequestErrorCode.METHOD).withMessage(Strings.format("No method could be found for the type $.", pack.getType())).build(); }
        return pack.unpack(converter, signature);
    }
    
}
