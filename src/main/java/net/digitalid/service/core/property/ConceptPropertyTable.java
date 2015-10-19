package net.digitalid.service.core.property;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.agent.Agent;
import net.digitalid.service.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.agent.Restrictions;
import net.digitalid.service.core.annotations.Loaded;
import net.digitalid.service.core.annotations.NonEncoding;
import net.digitalid.service.core.concept.Concept;
import net.digitalid.service.core.data.StateModule;
import net.digitalid.service.core.data.StateTable;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.storable.StorableFactory;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.column.Site;
import net.digitalid.utility.database.configuration.Database;

/**
 * This class models a database table that stores a {@link ReadOnlyProperty property} of a {@link Concept concept}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public abstract class ConceptPropertyTable<V, C extends Concept<C, E, ?>, E extends Entity> extends StateTable {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Entity Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factory to store and restore the entity.
     */
    private final @Nonnull StorableFactory<E, Site> entityFactory; // TODO: Use a more specific subtype as soon as the factories of entities are implemented.
    
    /**
     * Returns the factory to store and restore the entity.
     * 
     * @return the factory to store and restore the entity.
     */
    @Pure
    public final @Nonnull StorableFactory<E, Site> getEntityFactory() {
        return entityFactory;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Concept Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factory to store and restore the concept.
     */
    private final @Nonnull Concept.IndexBasedGlobalFactory<C, E, ?> conceptFactory;
    
    /**
     * Returns the factory to store and restore the concept.
     * 
     * @return the factory to store and restore the concept.
     */
    @Pure
    public final @Nonnull Concept.IndexBasedGlobalFactory<C, E, ?> getConceptFactory() {
        return conceptFactory;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Value Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factory to store and restore the value of the property.
     */
    private final @Nonnull StorableFactory<V, ? super E> valueFactory;
    
    /**
     * Returns the factory to store and restore the value of the property.
     * 
     * @return the factory to store and restore the value of the property.
     */
    @Pure
    public final @Nonnull StorableFactory<V, ? super E> getValueFactory() {
        return valueFactory;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– State Selector –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the state selector to restrict the returned state.
     */
    private final @Nonnull StateSelector stateSelector;
    
    /**
     * Returns the state selector to restrict the returned state.
     * 
     * @return the state selector to restrict the returned state.
     */
    @Pure
    public final @Nonnull StateSelector getStateSelector() {
        return stateSelector;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new concept property table with the given parameters.
     * 
     * @param module the module to which the new table belongs.
     * @param name the name of the new table.
     * @param dumpType the dump type of the new table.
     * @param stateType the state type of the new table.
     * @param entityFactory the factory to store and restore the entity.
     * @param conceptFactory the factory to store and restore the concept.
     * @param valueFactory the factory to store and restore the value of the property.
     * @param stateSelector the state selector to restrict the returned state.
     * 
     * @require !(module instanceof Service) : "The module is not a service.";
     */
    protected ConceptPropertyTable(@Nonnull StateModule module, @Nonnull @Validated String name, @Nonnull @Loaded SemanticType dumpType, @Nonnull @Loaded SemanticType stateType, @Nonnull StorableFactory<E, Site> entityFactory, @Nonnull Concept.IndexBasedGlobalFactory<C, E, ?> conceptFactory, @Nonnull StorableFactory<V, ? super E> valueFactory, @Nonnull StateSelector stateSelector) {
        super(module, name, dumpType, stateType);
        
        this.entityFactory = entityFactory;
        this.conceptFactory = conceptFactory;
        this.valueFactory = valueFactory;
        this.stateSelector = stateSelector;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– State –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    public abstract @Nonnull @NonEncoding Block getState(@Nonnull E entity, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws SQLException;
    
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
    public abstract void addState(@Nonnull E entity, @Nonnull @NonEncoding Block block) throws SQLException, IOException, PacketException, ExternalException;
    
    /**
     * Removes all the entries of the given entity in this data collection.
     * 
     * @param entity the entity whose entries are to be removed.
     */
    @Locked
    @NonCommitting
    public void removeState(@Nonnull E entity) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DELETE FROM " + entity.getSite() + getName() + " WHERE entity = " + entity);
        }
    }
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    @SuppressWarnings("unchecked")
    public final @Nonnull Block getState(@Nonnull NonHostEntity entity, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws SQLException {
        return getState((E) entity, permissions, restrictions, agent);
    }
    
    @Locked
    @Override
    @NonCommitting
    @SuppressWarnings("unchecked")
    public final void addState(@Nonnull NonHostEntity entity, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        addState((E) entity, block);
    }
    
    @Locked
    @Override
    @NonCommitting
    @SuppressWarnings("unchecked")
    public final void removeState(@Nonnull NonHostEntity entity) throws SQLException {
        removeState((E) entity);
    }
    
}
