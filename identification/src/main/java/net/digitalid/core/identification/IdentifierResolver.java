package net.digitalid.core.identification;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.identification.identifier.Identifier;
import net.digitalid.core.identification.identity.Identity;

/**
 * The identifier resolver resolves an identifier into the corresponding identity.
 */
@Mutable
public interface IdentifierResolver {
    
    /* -------------------------------------------------- Interface -------------------------------------------------- */
    
    /**
     * Resolves the given identifier into an identity.
     */
    @Pure
    @NonCommitting
    public abstract @Nonnull Identity resolve(@Nonnull Identifier identifier) throws ExternalException;
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores the identifier resolver, which has to be provided by another package.
     */
    public static final @Nonnull Configuration<IdentifierResolver> configuration = Configuration.withUnknownProvider();
    
}
