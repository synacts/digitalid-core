package net.digitalid.core.asymmetrickey;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Functional;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.auxiliary.Time;

import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identity.HostIdentity;

/**
 * The private key retriever retrieves the private key of a host at a given time.
 */
@Stateless
@Functional
public interface PrivateKeyRetriever {
    
    /* -------------------------------------------------- Interface -------------------------------------------------- */
    
    /**
     * Returns the private key of the given host at the given time.
     */
    @Pure
    public @Nonnull PrivateKey getPrivateKey(@Nonnull HostIdentity host, @Nonnull Time time) throws RequestException;
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores the private key retriever, which has to be provided by the host package.
     */
    public static final @Nonnull Configuration<PrivateKeyRetriever> configuration = Configuration.withUnknownProvider();
    
    /* -------------------------------------------------- Static Access -------------------------------------------------- */
    
    /**
     * Retrieves the private key of the given host at the given time.
     */
    @Pure
    public static @Nonnull PrivateKey retrieve(@Nonnull HostIdentity host, @Nonnull Time time) throws RequestException {
        return configuration.get().getPrivateKey(host, time);
    }
    
    /**
     * Retrieves the private key of the given host at the given time.
     */
    @Pure
    @NonCommitting
    public static @Nonnull PrivateKey retrieve(@Nonnull HostIdentifier host, @Nonnull Time time) throws ExternalException {
        return retrieve(host.resolve(), time);
    }
    
}
