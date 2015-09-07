package net.digitalid.core.module;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.agent.Agent;
import net.digitalid.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.core.agent.Restrictions;
import net.digitalid.core.annotations.Loaded;
import net.digitalid.core.annotations.Locked;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.OnMainThread;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.client.Client;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.host.Host;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.service.Service;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.ListWrapper;
import net.digitalid.core.wrappers.SelfcontainedWrapper;

/**
 * These modules are used on both {@link Host hosts} and {@link Client clients}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public class StateModule<T extends StateTable<T>> extends HostModule<T> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code table.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TABLE = SemanticType.map("table.state@core.digitalid.net").load(SelfcontainedWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType STATE = SemanticType.map("state@core.digitalid.net").load(ListWrapper.TYPE, TABLE);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– State Type –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the state type of the module.
     */
    private final @Nonnull @Loaded SemanticType stateType;
    
    /**
     * Returns the state type of this module.
     * 
     * @return the state type of this module.
     */
    @Pure
    public final @Nonnull @Loaded SemanticType getStateType() {
        return stateType;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new state module with the given service and identifier.
     * 
     * @param service the service to which the new module belongs.
     * @param identifier the common identifier of the new module.
     * 
     * @require InternalNonHostIdentifier.isValid(identifier) : "The string is a valid internal non-host identifier.";
     */
    @OnMainThread
    protected StateModule(@Nonnull Service service, @Nonnull String identifier) {
        super(service, identifier);
        
        this.stateType = SemanticType.map("state." + identifier).load(STATE);
    }
    
    /**
     * Returns a new state module with the given service and identifier.
     * 
     * @param service the service to which the new module belongs.
     * @param identifier the common identifier of the new module.
     * 
     * @return a new state module with the given service and identifier.
     * 
     * @require InternalNonHostIdentifier.isValid(identifier) : "The string is a valid internal non-host identifier.";
     */
    @Pure
    public static @Nonnull <T extends StateTable<T>> StateModule<T> get(@Nonnull Service service, @Nonnull String identifier) {
        return new StateModule<>(service, identifier);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Export and Import –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
     * @ensure return.getType().equals(getStateType()) : "The returned block has the indicated type.";
     */
    @Pure
    @Locked
    @NonCommitting
    public @Nonnull Block getState(@Nonnull NonHostEntity entity, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws SQLException {
        throw new UnsupportedOperationException("TODO");
    }
    
    /**
     * Adds the partial state in the given block to the given entity.
     * 
     * @param entity the entity to which the partial state is to be added.
     * @param block the block containing the partial state to be added.
     * 
     * @require block.getType().isBasedOn(getStateType()) : "The block is based on the indicated type.";
     */
    @Locked
    @NonCommitting
    public void addState(@Nonnull NonHostEntity entity, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        throw new UnsupportedOperationException("TODO");
    }
    
    /**
     * Removes all the entries of the given entity in this module.
     * 
     * @param entity the entity whose entries are to be removed.
     */
    @Locked
    @NonCommitting
    public void removeState(@Nonnull NonHostEntity entity) throws SQLException {
        throw new UnsupportedOperationException("TODO");
    }
    
}
