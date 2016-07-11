package net.digitalid.core.identification;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.cryptography.key.PublicKey;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identity.HostIdentity;

/**
 * The public key retriever retrieves the public key of a host at a given time.
 */
@Immutable
public interface PublicKeyRetriever {
    
    /* -------------------------------------------------- Interface -------------------------------------------------- */
    
    /**
     * Returns the public key of the given host at the given time.
     */
    @Pure
    @NonCommitting
    public @Nonnull PublicKey getPublicKey(@Nonnull HostIdentity host, @Nonnull Time time) throws ExternalException;
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores the public key retriever, which has to be provided by the cache package.
     */
    public static final @Nonnull Configuration<PublicKeyRetriever> configuration = Configuration.withUnknownProvider();
    
    /* -------------------------------------------------- Static Access -------------------------------------------------- */
    
    /**
     * Retrieves the public key of the given host at the given time.
     */
    @Pure
    @NonCommitting
    public static @Nonnull PublicKey retrieve(@Nonnull HostIdentity host, @Nonnull Time time) throws ExternalException {
        return configuration.get().getPublicKey(host, time);
    }
    
    /**
     * Retrieves the public key of the given host at the given time.
     */
    @Pure
    @NonCommitting
    public static @Nonnull PublicKey retrieve(@Nonnull HostIdentifier host, @Nonnull Time time) throws ExternalException {
        return retrieve(host.getIdentity(), time);
    }
    
}