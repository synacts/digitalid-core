package net.digitalid.core.handler.method;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.logging.Log;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.threading.Threading;
import net.digitalid.utility.threading.annotations.MainThread;
import net.digitalid.utility.tuples.Pair;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.core.compression.Compression;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.exceptions.request.RequestErrorCode;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.exceptions.request.RequestExceptionBuilder;
import net.digitalid.core.identification.identity.IdentifierResolver;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.pack.Pack;
import net.digitalid.core.signature.Signature;

/**
 * All methods have to register themselves at this index.
 */
@Utility
public abstract class MethodIndex {
    
    /**
     * Stores a dummy configuration in order to have an initialization target.
     */
    public static final @Nonnull Configuration<Boolean> configuration = Configuration.with(Boolean.TRUE).addDependency(IdentifierResolver.configuration);
    
    /**
     * Maps method types to the converter that recovers the handler for that type.
     */
    private static final @Nonnull Map<@Nonnull SemanticType, @Nonnull Converter<? extends Method<?>, @Nonnull Pair<@Nullable Signature<Compression<Pack>>, @Nonnull Entity>>> converters = new ConcurrentHashMap<>();
    
    /**
     * Adds the given converter to recover the methods of the given type.
     */
    @Impure
    @MainThread
    public static void add(@Nonnull Converter<? extends Method<?>, @Nonnull Pair<@Nullable Signature<Compression<Pack>>, @Nonnull Entity>> converter, @Nonnull SemanticType type) {
        Require.that(Threading.isMainThread()).orThrow("The method 'add' may only be called on the main thread.");
        
        Log.debugging("Registered a converter for the type $.", type);
        converters.put(type, converter);
    }
    
    /**
     * Adds the given converter to recover the methods of its type.
     */
    @Impure
    @MainThread
    public static void add(@Nonnull Converter<? extends Method<?>, @Nonnull Pair<@Nullable Signature<Compression<Pack>>, @Nonnull Entity>> converter) {
        add(converter, SemanticType.map(converter));
    }
    
    /**
     * Returns the method that handles the pack of the given signature for the given entity.
     */
    @Pure
    public static @Nonnull Method<?> get(@Nonnull Signature<Compression<Pack>> signature, @Nonnull Entity entity) throws RequestException, RecoveryException {
        final @Nonnull Pack pack = signature.getObject().getObject();
        final @Nullable Converter<? extends Method<?>, @Nonnull Pair<@Nullable Signature<Compression<Pack>>, @Nonnull Entity>> converter = converters.get(pack.getType());
        if (converter == null) { throw RequestExceptionBuilder.withCode(RequestErrorCode.METHOD).withMessage(Strings.format("No method could be found for the type $.", pack.getType())).build(); }
        return pack.unpack(converter, Pair.of(signature, entity));
    }
    
}
