package net.digitalid.core.module;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.agent.Agent;
import net.digitalid.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.core.agent.Restrictions;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.client.Client;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.host.Host;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.wrappers.Block;

/**
 * These modules are used on both {@link Host hosts} and {@link Client clients}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
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
     * Returns the partial state of the given entity restricted by the given authorization.
     * 
     * @param entity the entity whose partial state is to be returned.
     * @param permissions the permissions that restrict the returned state.
     * @param restrictions the restrictions that restrict the returned state.
     * @param agent the agent whose authorization restricts the returned state.
     * 
     * @return the partial state of the given entity restricted by the given authorization.
     * 
     * @ensure return.getType().equals(getStateFormat()) : "The returned block has the indicated type.";
     */
    @Pure
    @NonCommitting
    public @Nonnull Block getState(@Nonnull NonHostEntity entity, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws SQLException;
    
    /**
     * Adds the partial state in the given block to the given entity.
     * 
     * @param entity the entity to which the partial state is to be added.
     * @param block the block containing the partial state to be added.
     * 
     * @require block.getType().isBasedOn(getStateFormat()) : "The block is based on the indicated type.";
     */
    @NonCommitting
    public void addState(@Nonnull NonHostEntity entity, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException;
    
    /**
     * Removes all the entries of the given entity in this module.
     * 
     * @param entity the entity whose entries are to be removed.
     */
    @NonCommitting
    public void removeState(@Nonnull NonHostEntity entity) throws SQLException;
    
}
