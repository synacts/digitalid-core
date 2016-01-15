package net.digitalid.service.core.concept.property;

import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.exceptions.external.ExternalException;
import net.digitalid.utility.system.thread.annotations.MainThread;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;

import net.digitalid.database.core.Database;
import net.digitalid.database.core.annotations.Locked;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.annotations.NonEncoding;
import net.digitalid.service.core.block.wrappers.structure.ListWrapper;
import net.digitalid.service.core.block.wrappers.structure.TupleWrapper;
import net.digitalid.service.core.concept.Concept;
import net.digitalid.service.core.concepts.agent.Agent;
import net.digitalid.service.core.concepts.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.concepts.agent.Restrictions;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.identity.Identity;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.Loaded;
import net.digitalid.service.core.property.ReadOnlyProperty;
import net.digitalid.service.core.storage.SiteTable;

/**
 * This class models a database table that stores a {@link ReadOnlyProperty property} of a {@link Concept concept}.
 */
@Immutable
public abstract class ConceptPropertyTable<V, C extends Concept<C, E, ?>, E extends Entity> extends SiteTable {
    
    /* -------------------------------------------------- Type Mappings -------------------------------------------------- */
    
    /**
     * Maps the dump type of this table by deriving the identifier from the property name.
     * 
     * @param propertySetup the factory that contains the name and module of the property.
     * 
     * @return the dump type of this table by deriving the identifier from the property name.
     */
    @MainThread
    private static @Nonnull @Loaded SemanticType mapDumpType(@Nonnull ConceptPropertySetup<?, ?, ?> propertySetup) {
        final @Nonnull String identifier = propertySetup.getPropertyName() + propertySetup.getPropertyTable().getModule().getDumpType().getAddress().getStringWithDot();
        // TODO: should propertySetup.getValueConverters().getXDFConverter().getType() be replaced with propertySetup.getPropertyType()?
        final @Nonnull SemanticType entry = SemanticType.map("entry." + identifier).load(TupleWrapper.XDF_TYPE, Identity.IDENTIFIER, propertySetup.getConceptSetup().getXDFConverter().getType(), Time.TYPE, propertySetup.getPropertyType());
        return SemanticType.map(identifier).load(ListWrapper.XDF_TYPE, entry);
    }
    
    /**
     * Maps the state type of this table by deriving the identifier from the property name.
     * 
     * @param propertySetup the factory that contains the name and module of the property.
     * 
     * @return the state type of this table by deriving the identifier from the property name.
     */
    @MainThread
    private static @Nonnull @Loaded SemanticType mapStateType(@Nonnull ConceptPropertySetup<?, ?, ?> propertySetup) {
        final @Nonnull String identifier = propertySetup.getPropertyName() + propertySetup.getPropertyTable().getModule().getStateType().getAddress().getStringWithDot();
        // TODO: should propertySetup.getValueConverters().getXDFConverter().getType() be replaced with propertySetup.getPropertyType()?
        final @Nonnull SemanticType entry = SemanticType.map("entry." + identifier).load(TupleWrapper.XDF_TYPE, propertySetup.getConceptSetup().getXDFConverter().getType(), Time.TYPE, propertySetup.getPropertyType());
        return SemanticType.map(identifier).load(ListWrapper.XDF_TYPE, entry);
    }
    
    /* -------------------------------------------------- Property Factory -------------------------------------------------- */
    
    /**
     * Stores the property factory that contains the required information.
     */
    private final @Nonnull ConceptPropertySetup<V, C, E> propertySetup;
    
    /**
     * Returns the property factory that contains the required information.
     * 
     * @return the property factory that contains the required information.
     */
    @Pure
    // TODO: rename
    public final @Nonnull ConceptPropertySetup<V, C, E> getPropertyFactory() {
        return propertySetup;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new concept property table with the given property factory.
     * 
     * @param propertySetup the property factory that contains the required information.
     */
    protected ConceptPropertyTable(@Nonnull ConceptPropertySetup<V, C, E> propertySetup) {
        super(propertySetup.getPropertyTable().getModule(), propertySetup.getPropertyName(), mapDumpType(propertySetup), mapStateType(propertySetup));
        
        this.propertySetup = propertySetup;
    }
    
    /* -------------------------------------------------- Generic State -------------------------------------------------- */
    
    // Remark: The following methods are only necessary because Java does not allow lower bounds on type parameters.
    
    /**
     * Returns the state of the given entity in this data collection restricted by the given authorization.
     * 
     * @param entity the entity whose partial state is to be returned.
     * @param permissions the permissions that restrict the returned state.
     * @param restrictions the restrictions that restrict the returned state.
     * @param agent the agent whose authorization restricts the returned state.
     * 
     * @return the state of the given entity in this data collection restricted by the given authorization.
     * 
     * @ensure return.getType().equals(getStateType()) : "The returned block has the state type of this data collection.";
     */
    @Pure
    @Locked
    @NonCommitting
    public abstract @Nonnull @NonEncoding Block getState(@Nonnull E entity, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws DatabaseException;
    
    /**
     * Adds the state in the given block to the given entity in this data collection.
     * 
     * @param entity the entity to which the partial state is to be added.
     * @param block the block containing the partial state to be added.
     * 
     * @require block.getType().isBasedOn(getStateType()) : "The block is based on the state type of this data collection.";
     */
    @Locked
    @NonCommitting
    public abstract void addState(@Nonnull E entity, @Nonnull @NonEncoding Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException;
    
    /**
     * Removes all the entries of the given entity in this data collection.
     * 
     * @param entity the entity whose entries are to be removed.
     */
    @Locked
    @NonCommitting
    public void removeState(@Nonnull E entity) throws DatabaseException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DELETE FROM " + entity.getSite() + getName() + " WHERE entity = " + entity);
        } catch (SQLException exception) {
            throw DatabaseException.get(exception);
        }
    }
    
    /* -------------------------------------------------- Non-Generic State -------------------------------------------------- */
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    @SuppressWarnings("unchecked")
    public final @Nonnull Block getState(@Nonnull NonHostEntity entity, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws DatabaseException {
        return getState((E) entity, permissions, restrictions, agent);
    }
    
    @Locked
    @Override
    @NonCommitting
    @SuppressWarnings("unchecked")
    public final void addState(@Nonnull NonHostEntity entity, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        addState((E) entity, block);
    }
    
    @Locked
    @Override
    @NonCommitting
    @SuppressWarnings("unchecked")
    public final void removeState(@Nonnull NonHostEntity entity) throws DatabaseException {
        removeState((E) entity);
    }
    
}
