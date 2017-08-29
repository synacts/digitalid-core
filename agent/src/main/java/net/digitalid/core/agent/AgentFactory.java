package net.digitalid.core.agent;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Functional;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.commitment.Commitment;
import net.digitalid.core.entity.NonHostEntity;

/**
 * The agent factory returns the agent for the given entity with the given key.
 */
@Stateless
//@Functional
public interface AgentFactory {
    
    /* -------------------------------------------------- Interface -------------------------------------------------- */
    
    /**
     * Returns the agent for the given entity with the given key.
     */
    @Pure
    @NonCommitting
    public @Nonnull Agent getAgent(@Nonnull NonHostEntity entity, long key) throws DatabaseException;
    
    /**
     * Returns the agent for the given entity with the given key.
     */
    @Pure
    public @Nonnull Agent getAgent(@Nonnull NonHostEntity entity, @Nonnull Commitment commitment) throws DatabaseException;
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores the agent factory, which has to be provided by the client agent package.
     */
    public static final @Nonnull Configuration<AgentFactory> configuration = Configuration.withUnknownProvider();
    
    /* -------------------------------------------------- Static Access -------------------------------------------------- */
    
    /**
     * Retrieves the public key of the given host at the given time.
     */
    @Pure
    @NonCommitting
    public static @Nonnull Agent retrieve(@Nonnull NonHostEntity entity, @Nonnull Commitment commitment) throws DatabaseException {
        return configuration.get().getAgent(entity, commitment);
    }
    
}
