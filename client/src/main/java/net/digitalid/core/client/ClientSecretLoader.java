package net.digitalid.core.client;

import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.file.Files;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.string.DomainName;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.core.group.Exponent;
import net.digitalid.core.group.ExponentBuilder;
import net.digitalid.core.group.ExponentConverter;
import net.digitalid.core.parameters.Parameters;
import net.digitalid.core.selfcontained.Selfcontained;

/**
 * The client secret loader loads and stores the secret of a client.
 */
@Mutable
public class ClientSecretLoader {
    
    /* -------------------------------------------------- Interface -------------------------------------------------- */
    
    /**
     * Returns the secret of the client with the given identifier.
     */
    @Pure
    @TODO(task = "Throw a net.digitalid.core.conversion.FileException instead.", date = "2016-12-08", author = Author.KASPAR_ETTER)
    public @Nonnull Exponent getClientSecret(@Nonnull @DomainName @MaxSize(63) String identifier) throws ExternalException {
        final @Nonnull File file = Files.relativeToConfigurationDirectory(identifier + ".client.xdf");
        if (file.exists()) {
            // TODO: Check the type of the loaded selfcontained?
            return Selfcontained.loadFrom(file).recover(ExponentConverter.INSTANCE, null);
        } else {
            final @Nonnull Exponent secret = ExponentBuilder.withValue(new BigInteger(Parameters.HASH.get(), new SecureRandom())).build();
            setClientSecret(identifier, secret);
            return secret;
        }
    }
    
    /**
     * Sets the secret of the client with the given identifier.
     */
    @Impure
    @TODO(task = "Throw a net.digitalid.core.conversion.FileException instead.", date = "2016-12-08", author = Author.KASPAR_ETTER)
    public void setClientSecret(@Nonnull @DomainName @MaxSize(63) String identifier, @Nonnull Exponent secret) throws ExternalException {
        final @Nonnull File file = Files.relativeToConfigurationDirectory(identifier + ".client.xdf");
        Selfcontained.convert(secret, ExponentConverter.INSTANCE).storeTo(file);
    }
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores the configured client secret loader.
     */
    public static final @Nonnull Configuration<ClientSecretLoader> configuration = Configuration.with(new ClientSecretLoader());
    
    /* -------------------------------------------------- Static Access -------------------------------------------------- */
    
    /**
     * Loads the secret of the client with the given identifier.
     */
    @Pure
    public static @Nonnull Exponent load(@Nonnull @DomainName @MaxSize(63) String identifier) throws ExternalException {
        return configuration.get().getClientSecret(identifier);
    }
    
    /**
     * Stores the secret of the client with the given identifier.
     */
    @Impure
    public static void store(@Nonnull @DomainName @MaxSize(63) String identifier, @Nonnull Exponent secret) throws ExternalException {
        configuration.get().setClientSecret(identifier, secret);
    }
    
}
