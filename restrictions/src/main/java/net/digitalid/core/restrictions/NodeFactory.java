package net.digitalid.core.restrictions;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.validation.annotations.type.Functional;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.entity.NonHostEntity;

/**
 * The node factory returns the node for the given entity with the given key.
 */
@Stateless
@Functional
public interface NodeFactory {
    
    /* -------------------------------------------------- Interface -------------------------------------------------- */
    
    /**
     * Returns the agent for the given entity with the given key.
     */
    @Pure
    @NonCommitting
    public @Nonnull Node getAgent(@Nonnull NonHostEntity entity, long key) throws DatabaseException;
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores the node factory, which has to be provided by the node package.
     */
    public static final @Nonnull Configuration<NodeFactory> configuration = Configuration.withUnknownProvider();
    
}
