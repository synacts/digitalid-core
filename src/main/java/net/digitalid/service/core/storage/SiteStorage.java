package net.digitalid.service.core.storage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.annotations.NonEncoding;
import net.digitalid.service.core.concepts.agent.Agent;
import net.digitalid.service.core.concepts.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.concepts.agent.Restrictions;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.utility.system.exceptions.InternalException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.Loaded;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.exceptions.DatabaseException;

/**
 * This interface models a storage that contains part of an {@link Entity entity's} state.
 * 
 * @see SiteTableImplementation
 * @see DelegatingSiteStorageImplementation
 */
public interface SiteStorage extends HostStorage {
    
    /**
     * Returns the state type of this storage.
     * 
     * @return the state type of this storage.
     */
    @Pure
    public @Nonnull @Loaded SemanticType getStateType();
    
    /**
     * Returns the state of the given entity in this storage restricted by the given authorization.
     * 
     * @param entity the entity whose partial state is to be returned.
     * @param permissions the permissions that restrict the returned state.
     * @param restrictions the restrictions that restrict the returned state.
     * @param agent the agent whose authorization restricts the returned state.
     * 
     * @return the state of the given entity in this storage restricted by the given authorization.
     * 
     * @ensure return.getType().equals(getStateType()) : "The returned block has the state type of this storage.";
     */
    @Pure
    @Locked
    @NonCommitting
    public @Nonnull @NonEncoding Block getState(@Nonnull NonHostEntity entity, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws DatabaseException;
    
    /**
     * Adds the state in the given block to the given entity in this storage.
     * 
     * @param entity the entity to which the partial state is to be added.
     * @param block the block containing the partial state to be added.
     * 
     * @require block.getType().isBasedOn(getStateType()) : "The block is based on the state type of this storage.";
     */
    @Locked
    @NonCommitting
    public void addState(@Nonnull NonHostEntity entity, @Nonnull @NonEncoding Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException;
    
    /**
     * Removes all the entries of the given entity in this storage.
     * 
     * @param entity the entity whose entries are to be removed.
     */
    @Locked
    @NonCommitting
    public void removeState(@Nonnull NonHostEntity entity) throws DatabaseException;
    
}
