package ch.virtualid.module;

import ch.virtualid.agent.Agent;
import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.handler.InternalQuery;
import ch.virtualid.handler.QueryReply;
import ch.virtualid.identity.SemanticType;
import ch.xdf.Block;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * These modules are used on both {@link Host hosts} and {@link Client clients}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public interface BothModule extends HostModule, ClientModule {
    
    /**
     * Returns the format of an entity's partial state in this module.
     * 
     * @return the format of an entity's partial state in this module.
     * 
     * @ensure return.isLoaded() : "The type declaration is loaded.";
     */
    @Pure
    public @Nonnull SemanticType getStateFormat();
    
    /**
     * Returns the partial state of the given entity restricted by the authorization of the given agent.
     * 
     * @param entity the entity whose partial state is to be returned.
     * @param agent the agent whose authorization restricts the returned state.
     * 
     * @return the partial state of the given entity restricted by the authorization of the given agent.
     * 
     * @ensure return.getType().equals(getStateFormat()) : "The returned block has the indicated type.";
     */
    @Pure
    public @Nonnull Block getState(@Nonnull Entity entity, @Nonnull Agent agent) throws SQLException;
    
    /**
     * Adds the partial state in the given block to the given entity.
     * 
     * @param entity the entity to which the partial state is to be added.
     * @param block the block containing the partial state to be added.
     * 
     * @require block.getType().isBasedOn(getStateFormat()) : "The block is based on the indicated type.";
     */
    public void addState(@Nonnull Entity entity, @Nonnull Block block) throws SQLException, InvalidEncodingException;
    
    /**
     * Removes all the entries of the given entity in this module.
     * 
     * @param entity the entity whose entries are to be removed.
     */
    public void removeState(@Nonnull Entity entity) throws SQLException;
    
    /**
     * Returns an internal query for reloading the data of this module.
     * <p>
     * <em>Important:</em> The block of the corresponding {@link QueryReply}
     * has to be based on the semantic type of {@link #getStateFormat()} in order to
     * pass it to {@link #addState(ch.virtualid.entity.Entity, ch.xdf.Block)}.
     * 
     * @param role the role whose data has to be reloaded.
     * 
     * @return an internal query for reloading the data of this module.
     */
    @Pure
    public @Nullable InternalQuery getInternalQuery(@Nonnull Role role);
    
}
