package ch.virtualid.module;

import ch.virtualid.agent.Agent;
import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Client;
import ch.virtualid.entity.NonHostEntity;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.server.Host;
import ch.xdf.Block;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

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
    public @Nonnull Block getState(@Nonnull NonHostEntity entity, @Nonnull Agent agent) throws SQLException;
    
    /**
     * Adds the partial state in the given block to the given entity.
     * 
     * @param entity the entity to which the partial state is to be added.
     * @param block the block containing the partial state to be added.
     * 
     * @require block.getType().isBasedOn(getStateFormat()) : "The block is based on the indicated type.";
     */
    public void addState(@Nonnull NonHostEntity entity, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException;
    
    /**
     * Removes all the entries of the given entity in this module.
     * 
     * @param entity the entity whose entries are to be removed.
     */
    public void removeState(@Nonnull NonHostEntity entity) throws SQLException;
    
}
