package net.digitalid.core.identification;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.identification.identity.InternalIdentity;

/**
 * The public key retriever retrieves the public key of an internal identity at a given time.
 */
@Stateless
public interface PublicKeyRetriever {
    
    /* -------------------------------------------------- Interface -------------------------------------------------- */
    
    /**
     * Returns the public key of the given host at the given time.
     */
    @Pure
    @NonCommitting
    public @Nonnull PublicKey getPublicKey(@Nonnull InternalIdentity internalIdentity, @Nonnull Time time) throws ExternalException;
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores the public key retriever, which has to be provided by the cache package.
     */
    public static final @Nonnull Configuration<PublicKeyRetriever> configuration = Configuration.withUnknownProvider();
    
    /* -------------------------------------------------- Static Access -------------------------------------------------- */
    
    /**
     * Retrieves the public key of the given internal identity at the given time.
     */
    @Pure
    @NonCommitting
    public static @Nonnull PublicKey retrieve(@Nonnull InternalIdentity internalIdentity, @Nonnull Time time) throws ExternalException {
        return configuration.get().getPublicKey(internalIdentity, time);
    }
    
    /**
     * Retrieves the public key of the given internal identifier at the given time.
     */
    @Pure
    @NonCommitting
    public static @Nonnull PublicKey retrieve(@Nonnull InternalIdentifier internalIdentifier, @Nonnull Time time) throws ExternalException {
        return retrieve(internalIdentifier.resolve(), time);
    }
    
}
