package ch.virtualid.module;

import ch.virtualid.agent.Agent;
import ch.virtualid.database.HostEntity;
import ch.xdf.Block;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * Database modules that represent a part of an entity's state have to extend this class.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public abstract class Module {
    
    /**
     * Returns the state of the given entity restricted by the authorization of the given agent.
     * 
     * @param connection an open connection to the database.
     * @param entity the entity whose state is to be returned.
     * @param agent the agent whose authorization restricts the returned state.
     * @return the state of the given entity restricted by the authorization of the given agent.
     */
    protected abstract @Nonnull Block getAll(@Nonnull Connection connection, @Nonnull Entity entity, @Nonnull Agent agent) throws SQLException;
    
    /**
     * Adds the state in the given block to the given entity.
     * 
     * @param connection an open connection to the database.
     * @param entity the entity to which the state is to be added.
     * @param block the block containing the state to be added.
     */
    protected abstract void addAll(@Nonnull Connection connection, @Nonnull Entity entity, @Nonnull Block block) throws SQLException, InvalidEncodingException;
    
    /**
     * Removes all the entries of the given entity in this module.
     * 
     * @param connection an open host connection to the database.
     * @param entity the entity whose entries are to be removed.
     */
    protected abstract void removeAll(@Nonnull HostEntity connection, @Nonnull Entity entity) throws SQLException;
    
}
