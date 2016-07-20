package net.digitalid.core.client;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.math.Exponent;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.string.DomainName;
import net.digitalid.utility.validation.annotations.type.Mutable;

/**
 * The client secret loader loads and stores the secret of a client.
 */
@Mutable
public interface ClientSecretLoader {
    
    /* -------------------------------------------------- Interface -------------------------------------------------- */
    
    /**
     * Returns the secret of the client with the given identifier.
     */
    @Pure
    public @Nonnull Exponent getClientSecret(@Nonnull @DomainName @MaxSize(63) String identifier);
    
    /**
     * Sets the secret of the client with the given identifier.
     */
    @Impure
    public void setClientSecret(@Nonnull @DomainName @MaxSize(63) String identifier, @Nonnull Exponent secret);
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores the client secret loader, which has to be provided by another package.
     */
    public static final @Nonnull Configuration<ClientSecretLoader> configuration = Configuration.withUnknownProvider();
    
    /* -------------------------------------------------- Static Access -------------------------------------------------- */
    
    /**
     * Loads the secret of the client with the given identifier.
     */
    @Pure
    public static @Nonnull Exponent load(@Nonnull @DomainName @MaxSize(63) String identifier) {
        return configuration.get().getClientSecret(identifier);
    }
    
    /**
     * Stores the secret of the client with the given identifier.
     */
    @Impure
    public static void store(@Nonnull @DomainName @MaxSize(63) String identifier, @Nonnull Exponent secret) {
        configuration.get().setClientSecret(identifier, secret);
    }
    
}
