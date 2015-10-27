package net.digitalid.service.core.concept.property;

import net.digitalid.service.core.block.annotations.NonEncoding;

import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.ListWrapper;
import net.digitalid.service.core.block.wrappers.TupleWrapper;
import net.digitalid.service.core.identity.annotations.Loaded;
import net.digitalid.service.core.dataservice.StateTable;
import net.digitalid.service.core.concepts.agent.Agent;
import net.digitalid.service.core.concepts.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.concepts.agent.Restrictions;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.concept.Concept;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identity.Identity;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.annotations.OnMainThread;
import net.digitalid.utility.database.configuration.Database;

/**
 * This class models a database table that stores a {@link ReadOnlyProperty property} of a {@link Concept concept}.
 */
@Immutable
public abstract class ConceptPropertyTable<V, C extends Concept<C, E, ?>, E extends Entity<E>> extends StateTable {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Type Mappings –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Maps the dump type of this table by deriving the identifier from the property name.
     * 
     * @param propertyFactory the factory that contains the name and module of the property.
     * 
     * @return the dump type of this table by deriving the identifier from the property name.
     */
    @OnMainThread
    private static @Nonnull @Loaded SemanticType mapDumpType(@Nonnull ConceptPropertyFactory<?, ?, ?> propertyFactory) {
        final @Nonnull String identifier = propertyFactory.getPropertyName() + propertyFactory.getStateModule().getDumpType().getAddress().getStringWithDot();
        final @Nonnull SemanticType entry = SemanticType.map("entry." + identifier).load(TupleWrapper.TYPE, Identity.IDENTIFIER, propertyFactory.getConceptFactories().getEncodingFactory().getType(), Time.TYPE, propertyFactory.getValueFactories().getEncodingFactory().getType());
        return SemanticType.map(identifier).load(ListWrapper.TYPE, entry);
    }
    
    /**
     * Maps the state type of this table by deriving the identifier from the property name.
     * 
     * @param propertyFactory the factory that contains the name and module of the property.
     * 
     * @return the state type of this table by deriving the identifier from the property name.
     */
    @OnMainThread
    private static @Nonnull @Loaded SemanticType mapStateType(@Nonnull ConceptPropertyFactory<?, ?, ?> propertyFactory) {
        final @Nonnull String identifier = propertyFactory.getPropertyName() + propertyFactory.getStateModule().getStateType().getAddress().getStringWithDot();
        final @Nonnull SemanticType entry = SemanticType.map("entry." + identifier).load(TupleWrapper.TYPE, propertyFactory.getConceptFactories().getEncodingFactory().getType(), Time.TYPE, propertyFactory.getValueFactories().getEncodingFactory().getType());
        return SemanticType.map(identifier).load(ListWrapper.TYPE, entry);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Property Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the property factory that contains the required information.
     */
    private final @Nonnull ConceptPropertyFactory<V, C, E> propertyFactory;
    
    /**
     * Returns the property factory that contains the required information.
     * 
     * @return the property factory that contains the required information.
     */
    @Pure
    public final @Nonnull ConceptPropertyFactory<V, C, E> getPropertyFactory() {
        return propertyFactory;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new concept property table with the given property factory.
     * 
     * @param propertyFactory the property factory that contains the required information.
     */
    protected ConceptPropertyTable(@Nonnull ConceptPropertyFactory<V, C, E> propertyFactory) {
        super(propertyFactory.getStateModule(), propertyFactory.getPropertyName(), mapDumpType(propertyFactory), mapStateType(propertyFactory));
        
        this.propertyFactory = propertyFactory;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Generic State –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    public abstract @Nonnull @NonEncoding Block getState(@Nonnull E entity, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws AbortException;
    
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
    public abstract void addState(@Nonnull E entity, @Nonnull @NonEncoding Block block) throws AbortException, PacketException, ExternalException, NetworkException;
    
    /**
     * Removes all the entries of the given entity in this data collection.
     * 
     * @param entity the entity whose entries are to be removed.
     */
    @Locked
    @NonCommitting
    public void removeState(@Nonnull E entity) throws AbortException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DELETE FROM " + entity.getSite() + getName() + " WHERE entity = " + entity);
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Non-Generic State –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    @SuppressWarnings("unchecked")
    public final @Nonnull Block getState(@Nonnull NonHostEntity entity, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws AbortException {
        return getState((E) entity, permissions, restrictions, agent);
    }
    
    @Locked
    @Override
    @NonCommitting
    @SuppressWarnings("unchecked")
    public final void addState(@Nonnull NonHostEntity entity, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException {
        addState((E) entity, block);
    }
    
    @Locked
    @Override
    @NonCommitting
    @SuppressWarnings("unchecked")
    public final void removeState(@Nonnull NonHostEntity entity) throws AbortException {
        removeState((E) entity);
    }
    
}
