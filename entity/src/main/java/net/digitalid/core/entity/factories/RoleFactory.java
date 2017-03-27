package net.digitalid.core.entity.factories;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.validation.annotations.type.Functional;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.unit.CoreUnit;
import net.digitalid.core.unit.annotations.IsClient;
import net.digitalid.core.unit.annotations.OnClient;

/**
 * The role factory returns the role with the given key.
 */
@Stateless
@Functional
public interface RoleFactory {
    
    /* -------------------------------------------------- Interface -------------------------------------------------- */
    
    /**
     * Returns the role on the given client with the given key.
     */
    @Pure
    @NonCommitting
    public @Nonnull @OnClient Entity<?> getRole(@Nonnull @IsClient CoreUnit client, long key) throws DatabaseException, RecoveryException;
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores the role factory, which has to be provided by the client package.
     */
    public static final @Nonnull Configuration<RoleFactory> configuration = Configuration.withUnknownProvider();
    
    /* -------------------------------------------------- Static Access -------------------------------------------------- */
    
    /**
     * Returns the role on the given client with the given key.
     */
    @Pure
    @NonCommitting
    public static @Nonnull @OnClient Entity<?> create(@Nonnull @IsClient CoreUnit client, long key) throws DatabaseException, RecoveryException {
        return configuration.get().getRole(client, key);
    }
    
}
