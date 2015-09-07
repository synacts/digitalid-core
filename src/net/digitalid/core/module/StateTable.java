package net.digitalid.core.module;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.agent.Agent;
import net.digitalid.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.core.agent.Restrictions;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Loaded;
import net.digitalid.core.annotations.Locked;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.Validated;
import net.digitalid.core.database.Database;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.wrappers.Block;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public abstract class StateTable<T extends StateTable<T>> extends HostTable<T> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Table Type –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the state type of this table.
     */
    private final @Nonnull @Loaded SemanticType stateType;
    
    /**
     * Returns the state type of this table.
     * 
     * @return the state type of this table.
     */
    @Pure
    public final @Nonnull @Loaded SemanticType getStateType() {
        return stateType;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new host table with the given parameters.
     * 
     * @param module the module to which the new table belongs.
     * @param name the name of the new table.
     * @param tableType the type of the new table.
     * @param stateType the state type of the new table.
     */
    protected StateTable(@Nonnull Module<T> module, @Nonnull @Validated String name, @Nonnull @Loaded SemanticType tableType, @Nonnull @Loaded SemanticType stateType) {
        super(module, name, tableType);
        
        this.stateType = stateType;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– State –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    public abstract @Nonnull Block getState(@Nonnull NonHostEntity entity, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws SQLException;
    
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
    public abstract void addState(@Nonnull NonHostEntity entity, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException;
    
    /**
     * Removes all the entries of the given entity in this table.
     * 
     * @param entity the entity whose entries are to be removed.
     */
    @Locked
    @NonCommitting
    public void removeState(@Nonnull NonHostEntity entity) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DELETE FROM " + entity.getSite() + getName() + " WHERE entity = " + entity);
        }
    }
    
}
