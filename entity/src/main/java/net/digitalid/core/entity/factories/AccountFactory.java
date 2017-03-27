package net.digitalid.core.entity.factories;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.validation.annotations.type.Functional;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.identification.identity.InternalIdentity;
import net.digitalid.core.unit.CoreUnit;
import net.digitalid.core.unit.annotations.IsHost;
import net.digitalid.core.unit.annotations.OnHost;

/**
 * The account factory creates an account for a given internal identity.
 */
@Stateless
@Functional
public interface AccountFactory {
    
    /* -------------------------------------------------- Interface -------------------------------------------------- */
    
    /**
     * Returns an account on the given host for the given identity.
     */
    @Pure
    public @Nonnull @OnHost Entity<?> getAccount(@Nonnull @IsHost CoreUnit host, @Nonnull InternalIdentity identity);
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores the account factory, which has to be provided by the host package.
     */
    public static final @Nonnull Configuration<AccountFactory> configuration = Configuration.withUnknownProvider();
    
    /* -------------------------------------------------- Static Access -------------------------------------------------- */
    
    /**
     * Returns an account on the given host for the given identity.
     */
    @Pure
    public static @Nonnull @OnHost Entity<?> create(@Nonnull @IsHost CoreUnit host, @Nonnull InternalIdentity identity) {
        return configuration.get().getAccount(host, identity);
    }
    
}
