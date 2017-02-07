package net.digitalid.core.handler;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Functional;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.annotations.OnHost;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.InternalIdentifier;

/**
 * The account factory creates an account with a given internal identifier.
 */
@Stateless
@Functional
public interface AccountFactory {
    
    /* -------------------------------------------------- Interface -------------------------------------------------- */
    
    /**
     * Returns an account on the given recipient for the given subject.
     */
    @Pure
    @NonCommitting
    public @Nonnull @OnHost Entity<?> getAccount(@Nonnull HostIdentifier recipient, @Nonnull InternalIdentifier subject) throws ExternalException;
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores the account factory, which has to be provided by the host package.
     */
    public static final @Nonnull Configuration<AccountFactory> configuration = Configuration.withUnknownProvider();
    
    /* -------------------------------------------------- Static Access -------------------------------------------------- */
    
    /**
     * Returns an account on the given recipient for the given subject.
     */
    @Pure
    @NonCommitting
    public static @Nonnull @OnHost Entity<?> create(@Nonnull HostIdentifier recipient, @Nonnull InternalIdentifier subject) throws ExternalException {
        return configuration.get().getAccount(recipient, subject);
    }
    
}
