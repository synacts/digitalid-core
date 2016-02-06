package net.digitalid.core.state;

import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.state.Validated;

import net.digitalid.database.core.Database;
import net.digitalid.database.core.annotations.Locked;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.declaration.Declaration;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.conversion.Block;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.core.agent.Restrictions;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;

import net.digitalid.core.exceptions.NetworkException;
import net.digitalid.core.exceptions.RequestException;

import net.digitalid.core.identity.SemanticType;

import net.digitalid.core.identity.annotations.Loaded;

/**
 * This class implements a database table from which one can get, add and remove an {@link Entity entity's} state.
 * 
 * @see SiteTable
 */
@Immutable
abstract class SiteTableImplementation<M extends DelegatingSiteStorageImplementation> extends HostTableImplementation<M> implements SiteStorage {
    
    /* -------------------------------------------------- State Type -------------------------------------------------- */
    
    /**
     * Stores the state type of this table.
     */
    private final @Nonnull @Loaded SemanticType stateType;
    
    @Pure
    @Override
    public final @Nonnull @Loaded SemanticType getStateType() {
        return stateType;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new site table with the given parameters.
     * 
     * @param module the module to which the new table belongs.
     * @param name the name of the new table (unique within the module).
     * @param declaration the declaration of the new table.
     * @param dumpType the dump type of the new host table.
     * @param stateType the state type of the new site table.
     */
    protected SiteTableImplementation(@Nonnull M module, @Nonnull @Validated String name, @Nonnull Declaration declaration, @Nonnull @Loaded SemanticType dumpType, @Nonnull @Loaded SemanticType stateType) {
        super(module, name, declaration, dumpType);
        
        this.stateType = stateType;
        
        Storage.register(this);
    }
    
    /* -------------------------------------------------- State -------------------------------------------------- */
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public abstract @Nonnull Block getState(@Nonnull NonHostEntity entity, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws DatabaseException;
    
    @Locked
    @Override
    @NonCommitting
    public abstract void addState(@Nonnull NonHostEntity entity, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException;
    
    @Locked
    @Override
    @NonCommitting
    public void removeState(@Nonnull NonHostEntity entity) throws DatabaseException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DELETE FROM " + entity.getSite() + getName() + " WHERE entity = " + entity);
        } catch (@Nonnull SQLException exception) {
            throw DatabaseException.get(exception);
        }
    }
    
}
