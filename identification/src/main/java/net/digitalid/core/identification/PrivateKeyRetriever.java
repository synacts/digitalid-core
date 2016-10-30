package net.digitalid.core.identification;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.asymmetrickey.PrivateKey;

/**
 *
 */
@Stateless
public interface PrivateKeyRetriever {
    
    /* -------------------------------------------------- Interface -------------------------------------------------- */
    
    /**
     * Returns the private key of the given host at the given time.
     */
    @Pure
    @NonCommitting
    public @Nonnull PrivateKey getPrivateKey(@Nonnull Time time) throws ExternalException;
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores the private key retriever, which has to be provided by the cache package.
     */
    public static final @Nonnull Configuration<PrivateKeyRetriever> configuration = Configuration.withUnknownProvider();
    
    /* -------------------------------------------------- Static Access -------------------------------------------------- */
    
    /**
     * Retrieves the private key of the given host at the given time.
     */
    @Pure
    @NonCommitting
    public static @Nonnull PrivateKey retrieve(@Nonnull Time time) throws ExternalException {
        return configuration.get().getPrivateKey(time);
    }
    
}
