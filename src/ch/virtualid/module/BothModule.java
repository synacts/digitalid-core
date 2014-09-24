package ch.virtualid.module;

import ch.virtualid.agent.Agent;
import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.handler.InternalQuery;
import ch.virtualid.identity.SemanticType;
import ch.xdf.Block;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * These modules are used on both {@link Host hosts} and {@link Client clients}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class BothModule extends Module {
    
    /**
     * Returns the semantic type that corresponds to this module.
     * 
     * @return the semantic type that corresponds to this module.
     * 
     * @ensure return.isLoaded() : "The type declaration is loaded.";
     */
    @Pure
    protected abstract @Nonnull SemanticType getType();
    
    /**
     * Returns the partial state of the given entity restricted by the authorization of the given agent.
     * 
     * @param entity the entity whose partial state is to be returned.
     * @param agent the agent whose authorization restricts the returned state.
     * 
     * @return the partial state of the given entity restricted by the authorization of the given agent.
     * 
     * @ensure return.getType().equals(getType()) : "The returned block has the indicated type.";
     */
    @Pure
    protected abstract @Nonnull Block getAll(@Nonnull Entity entity, @Nonnull Agent agent) throws SQLException;
    
    /**
     * Adds the partial state in the given block to the given entity.
     * 
     * @param entity the entity to which the partial state is to be added.
     * @param block the block containing the partial state to be added.
     * 
     * @require block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
     */
    protected abstract void addAll(@Nonnull Entity entity, @Nonnull Block block) throws SQLException, InvalidEncodingException;
    
    /**
     * Removes all the entries of the given entity in this module.
     * 
     * @param entity the entity whose entries are to be removed.
     */
    protected abstract void removeAll(@Nonnull Entity entity) throws SQLException;
    
    
    /**
     * Returns 
     * 
     * @param role
     * 
     * @return 
     */
    @Pure
    public abstract @Nullable InternalQuery getInternalQuery(@Nonnull Role role);
    
}
