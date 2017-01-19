package net.digitalid.core.handler.reply;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.signature.host.HostSignature;

/**
 * Description.
 */
@Utility
@TODO(task = "Implement this class similarly to MethodIndex.", date = "2016-11-08", author = Author.KASPAR_ETTER)
public abstract class ReplyIndex {
    
    /**
     * Each reply needs to {@link #add(net.digitalid.core.identification.identity.SemanticType, net.digitalid.core.handler.reply.ReplyIndex.Factory) register} a factory that inherits from this class.
     */
    protected static abstract class Factory {
        
        /**
         * Creates a reply that handles contents of the indicated type.
         * 
         * @param entity the entity to which the returned reply belongs
         * @param signature the signature of the returned reply.
         * @param number the number that references the reply.
         * 
         * @return a new reply that decodes the given block.
         * 
         * @require block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
         * 
         * @ensure return.hasSignature() : "The returned reply has a signature.";
         */
        @Pure
        @NonCommitting
        protected abstract Reply create(@Nullable NonHostEntity entity, @Nonnull HostSignature<?> signature, long number) throws ExternalException;
        
    }
    
    
    /**
     * Maps reply types to the factory that creates handlers for that type.
     */
    private static final @Nonnull Map<SemanticType, Factory> converters = new ConcurrentHashMap<>();
    
    /**
     * Adds the given factory that creates handlers for the given type.
     * 
     * @param type the type to handle.
     * @param factory the factory to add.
     */
    @Impure
    protected static void add(@Nonnull SemanticType type, @Nonnull Factory factory) {
        converters.put(type, factory);
    }
    
    /**
     * Returns a reply that handles the given block.
     * 
     * @param entity the entity to which the returned reply belongs.
     * @param signature the signature of the returned reply.
     * @param number the number that references the reply.
     * 
     * @return a reply that decodes the given block.
     * 
     * @throws RequestException if no handler is found for the given content type.
     * 
     * @ensure return.hasSignature() : "The returned reply has a signature.";
     */
    @Pure
    @NonCommitting
    private static @Nonnull Reply get(@Nullable NonHostEntity entity, @Nonnull HostSignature<?> signature, long number) throws ExternalException {
        final @Nullable ReplyIndex.Factory factory = converters.get(null);
        if (factory == null) { throw new RuntimeException(); /* TODO: InvalidReplyTypeException.get(block.getType()); */ }
        else { return factory.create(entity, signature, number); }
    }
    
    @Pure
    @TODO(task = "Implement this method (here or somewhere/somehow else.", date = "2016-11-09", author = Author.KASPAR_ETTER)
    private static long store(@Nonnull HostSignature<?> signature) {
        return 0;
    }
    
    /**
     * Returns a reply that handles the given block.
     * 
     * @param entity the entity to which the returned reply belongs.
     * @param signature the signature of the returned reply.
     * 
     * @return a reply that decodes the given block.
     * 
     * @throws RequestException if no handler is found for the given content type.
     * 
     * @ensure return.hasSignature() : "The returned reply has a signature.";
     */
    @Pure
    @NonCommitting
    public static @Nonnull Reply get(@Nullable NonHostEntity entity, @Nonnull HostSignature<?> signature) throws ExternalException {
        return get(entity, signature, store(signature));
    }
    
}
