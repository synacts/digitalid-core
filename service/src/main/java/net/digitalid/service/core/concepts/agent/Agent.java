package net.digitalid.service.core.concepts.agent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.database.core.annotations.Committing;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.database.core.table.Site;
import net.digitalid.service.core.action.synchronizer.Synchronizer;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.structure.TupleWrapper;
import net.digitalid.service.core.block.wrappers.value.BooleanWrapper;
import net.digitalid.service.core.block.wrappers.value.integer.Integer64Wrapper;
import net.digitalid.service.core.concept.Concept;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.exceptions.request.RequestErrorCode;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.identity.Identity;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.site.annotations.Clients;
import net.digitalid.utility.annotations.reference.Capturable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.freezable.Frozen;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;

/**
 * This class models an agent that acts on behalf of an {@link Identity identity}.
 * 
 * @see ClientAgent
 * @see OutgoingRole
 * 
 * @see AgentModule
 */
@Immutable
public abstract class Agent extends Concept<Agent, NonHostEntity, Long> {
    
    /* -------------------------------------------------- Aspects -------------------------------------------------- */
    
    /**
     * Stores the aspect of the observed agent being created in the database.
     * This aspect is also used to notify that an agent gets unremoved again.
     */
    public static final @Nonnull Aspect CREATED = new Aspect(Agent.class, "created");
    
    /**
     * Stores the aspect of the observed agent being deleted from the database.
     * Instead of truly deleting the agent, it is just marked as being removed.
     */
    public static final @Nonnull Aspect DELETED = new Aspect(Agent.class, "deleted");
    
    /**
     * Stores the aspect of the agent being reset after having reloaded the agents module.
     */
    public static final @Nonnull Aspect RESET = new Aspect(Agent.class, "agent reset");
    
    /**
     * Stores the aspect of the permissions being changed at the observed agent.
     */
    public static final @Nonnull Aspect PERMISSIONS = new Aspect(Agent.class, "permissions changed");
    
    /**
     * Stores the aspect of the restrictions being changed at the observed agent.
     */
    public static final @Nonnull Aspect RESTRICTIONS = new Aspect(Agent.class, "restrictions changed");
    
    
    /* -------------------------------------------------- Types -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code number.agent@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType NUMBER = SemanticType.map("number.agent@core.digitalid.net").load(Integer64Wrapper.XDF_TYPE);
    
    /**
     * Stores the semantic type {@code client.agent@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType CLIENT = SemanticType.map("client.agent@core.digitalid.net").load(BooleanWrapper.XDF_TYPE);
    
    /**
     * Stores the semantic type {@code removed.agent@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType REMOVED = SemanticType.map("removed.agent@core.digitalid.net").load(BooleanWrapper.XDF_TYPE);
    
    /**
     * Stores the semantic type {@code agent@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("agent@core.digitalid.net").load(TupleWrapper.XDF_TYPE, NUMBER, CLIENT, REMOVED);
    
    
    /* -------------------------------------------------- Number -------------------------------------------------- */
    
    /**
     * Stores the number that references this agent in the database.
     */
    private final long number;
    
    /**
     * Returns the number that references this agent.
     * 
     * @return the number that references this agent.
     */
    @Pure
    public final long getNumber() {
        return number;
    }
    
    /* -------------------------------------------------- Removed -------------------------------------------------- */
    
    /**
     * Stores whether this agent has been removed.
     */
    private boolean removed;
    
    /**
     * Returns whether this agent is removed.
     * <p>
     * <em>Important:</em> If the agent comes from a block, this information is not to be trusted!
     * 
     * @return whether this agent is removed.
     */
    @Pure
    public final boolean isRemoved() {
        return removed;
    }
    
    /**
     * Checks that this agent is not removed and throws a {@link RequestException} otherwise.
     */
    @Pure
    public final void checkNotRemoved() throws RequestException {
        if (isRemoved()) { throw RequestException.get(RequestErrorCode.AUTHORIZATION, "The agent has been removed."); }
    }
    
    /**
     * Removes this agent from the database by marking it as being removed.
     * 
     * @require !isRemoved() : "This agent is not removed.";
     */
    @Committing
    @Clients
    public final void remove() throws DatabaseException {
        assert !isRemoved() : "This agent is not removed.";
        
        Synchronizer.execute(new AgentRemove(this));
    }
    
    /**
     * Removes this agent from the database by marking it as being removed.
     */
    @NonCommitting
    @OnlyForActions
    final void removeForActions() throws DatabaseException {
        AgentModule.removeAgent(this);
        if (isOnHost() && this instanceof OutgoingRole) { ((OutgoingRole) this).revoke(); }
        removed = true;
        notify(DELETED);
    }
    
    /**
     * Unremoves this agent from the database by marking it as no longer being removed.
     * 
     * @require isOnClient() : "This agent is on a client.";
     * @require isRemoved() : "This agent is removed.";
     */
    @Committing
    public final void unremove() throws DatabaseException {
        assert isRemoved() : "This agent is removed.";
        
        Synchronizer.execute(new AgentUnremove(this));
    }
    
    /**
     * Unremoves this agent from the database by marking it as no longer being removed.
     */
    @NonCommitting
    @OnlyForActions
    final void unremoveForActions() throws DatabaseException {
        AgentModule.unremoveAgent(this);
        if (isOnHost() && this instanceof OutgoingRole) { ((OutgoingRole) this).issue(); }
        removed = false;
        notify(CREATED);
    }
    
    /* -------------------------------------------------- Permissions -------------------------------------------------- */
    
    /**
     * Stores the permissions of this agent or null if not yet loaded.
     */
    protected @Nullable @NonFrozen FreezableAgentPermissions permissions;
    
    /**
     * Returns the permissions of this agent.
     * 
     * @return the permissions of this agent.
     */
    @Pure
    @NonCommitting
    public final @Nonnull @NonFrozen ReadOnlyAgentPermissions getPermissions() throws DatabaseException {
        if (permissions == null) { permissions = AgentModule.getPermissions(this); }
        return permissions;
    }
    
    /**
     * Adds the given permissions to this agent.
     * <p>
     * <em>Important:</em> The additional permissions should not cover any existing permissions. If they do,
     * make sure to {@link #removePermissions(net.digitalid.service.core.agent.ReadonlyAgentPermissions) remove} them first.
     * 
     * @param permissions the permissions to be added to this agent.
     */
    @Committing
    @Clients
    public final void addPermissions(@Nonnull @Frozen ReadOnlyAgentPermissions permissions) throws DatabaseException {
        if (!permissions.isEmpty()) { Synchronizer.execute(new AgentPermissionsAdd(this, permissions)); }
    }
    
    /**
     * Adds the given permissions to this agent.
     * 
     * @param newPermissions the permissions to be added to this agent.
     */
    @NonCommitting
    @OnlyForActions
    final void addPermissionsForActions(@Nonnull @Frozen ReadOnlyAgentPermissions newPermissions) throws DatabaseException {
        AgentModule.addPermissions(this, newPermissions);
        if (permissions != null) { permissions.putAll(newPermissions); }
        notify(PERMISSIONS);
    }
    
    /**
     * Removes the given permissions from this agent.
     * 
     * @param permissions the permissions to be removed from this agent.
     */
    @Committing
    @Clients
    public final void removePermissions(@Nonnull @Frozen ReadOnlyAgentPermissions permissions) throws DatabaseException {
        if (!permissions.isEmpty()) { Synchronizer.execute(new AgentPermissionsRemove(this, permissions)); }
    }
    
    /**
     * Removes the given permissions from this agent.
     * 
     * @param oldPermissions the permissions to be removed from this agent.
     */
    @NonCommitting
    @OnlyForActions
    final void removePermissionsForActions(@Nonnull @Frozen ReadOnlyAgentPermissions oldPermissions) throws DatabaseException {
        AgentModule.removePermissions(this, oldPermissions);
        if (permissions != null) { permissions.removeAll(oldPermissions); }
        notify(PERMISSIONS);
    }
    
    /* -------------------------------------------------- Restrictions -------------------------------------------------- */
    
    /**
     * Stores the restrictions of this agent or null if not yet loaded.
     * 
     * @invariant restrictions == null || restrictions.match(this) : "The restrictions are null or match this agent.";
     */
    protected @Nullable Restrictions restrictions;
    
    /**
     * Returns the restrictions of this agent.
     * 
     * @return the restrictions of this agent.
     * 
     * @ensure return.match(this) : "The restrictions match this agent.";
     */
    @Pure
    @NonCommitting
    public final @Nonnull Restrictions getRestrictions() throws DatabaseException {
        if (restrictions == null) { restrictions = AgentModule.getRestrictions(this); }
        return restrictions;
    }
    
    /**
     * Sets the restrictions of this agent.
     * 
     * @param newRestrictions the new restrictions of this agent.
     * 
     * @require newRestrictions.match(this) : "The new restrictions match this agent.";
     */
    @Committing
    @Clients
    public final void setRestrictions(@Nonnull Restrictions newRestrictions) throws DatabaseException {
        final @Nonnull Restrictions oldRestrictions = getRestrictions();
        if (!newRestrictions.equals(oldRestrictions)) {
            Synchronizer.execute(new AgentRestrictionsReplace(this, oldRestrictions, newRestrictions));
        }
    }
    
    /**
     * Replaces the restrictions of this agent.
     * 
     * @param oldRestrictions the old restrictions of this agent.
     * @param newRestrictions the new restrictions of this agent.
     * 
     * @require oldRestrictions.match(this) : "The old restrictions match this agent.";
     * @require newRestrictions.match(this) : "The new restrictions match this agent.";
     */
    @NonCommitting
    @OnlyForActions
    final void replaceRestrictions(@Nonnull Restrictions oldRestrictions, @Nonnull Restrictions newRestrictions) throws DatabaseException {
        AgentModule.replaceRestrictions(this, oldRestrictions, newRestrictions);
        restrictions = newRestrictions;
        notify(RESTRICTIONS);
    }
    
    /* -------------------------------------------------- Abstract -------------------------------------------------- */
    
    /**
     * Resets this agent.
     */
    public void reset() throws DatabaseException {
        this.removed = AgentModule.isRemoved(this);
        this.permissions = null;
        this.restrictions = null;
        notify(RESET);
    }
    
    /**
     * Returns whether this agent is a client.
     * 
     * @return whether this agent is a client.
     */
    @Pure
    public abstract boolean isClient();
    
    /* -------------------------------------------------- Covering -------------------------------------------------- */
    
    /**
     * Returns the agents that are weaker than this agent.
     * 
     * @return the agents that are weaker than this agent.
     * 
     * @ensure return.!isFrozen() : "The list is not frozen.";
     */
    @Pure
    @NonCommitting
    public final @Capturable @Nonnull FreezableList<Agent> getWeakerAgents() throws DatabaseException {
        return AgentModule.getWeakerAgents(this);
    }
    
    /**
     * Returns the weaker agent with the given agent number.
     * 
     * @param agentNumber the number of the returned agent.
     * 
     * @return the weaker agent with the given agent number.
     * 
     * @throws DatabaseException if no such weaker agent is found.
     */
    @Pure
    @NonCommitting
    public final @Nonnull Agent getWeakerAgent(long agentNumber) throws DatabaseException {
        return AgentModule.getWeakerAgent(this, agentNumber);
    }
    
    /**
     * Returns whether this agent covers the given agent.
     * 
     * @param agent the agent that needs to be covered.
     * 
     * @return whether this agent covers the given agent.
     * 
     * @require getEntity().equals(agent.getEntity()) : "The given agent belongs to the same entity.";
     */
    @Pure
    @NonCommitting
    public final boolean covers(@Nonnull Agent agent) throws DatabaseException {
        return !isRemoved() && AgentModule.isStronger(this, agent);
    }
    
    /**
     * Checks whether this agent covers the given agent and throws a {@link RequestException} if not.
     * 
     * @param agent the agent that needs to be covered.
     */
    @Pure
    @NonCommitting
    public final void checkCovers(@Nonnull Agent agent) throws RequestException, SQLException {
        if (!covers(agent)) { throw RequestException.get(RequestErrorCode.AUTHORIZATION, "This agent does not cover the other agent."); }
    }
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    /**
     * Creates a new agent with the given entity and number.
     * 
     * @param entity the entity to which this agent belongs.
     * @param number the number that references this agent.
     * @param removed whether this agent has been removed.
     */
    Agent(@Nonnull NonHostEntity entity, long number, boolean removed) {
        super(entity);
        
        this.number = number;
        this.removed = removed;
    }
    
    /**
     * Returns the agent with the given number at the given entity.
     * 
     * @param entity the entity to which the agent belongs.
     * @param number the number that denotes the new agent.
     * @param client whether the agent is a client agent.
     * @param removed whether the agent has been removed.
     */
    @Pure
    public static @Nonnull Agent get(@Nonnull NonHostEntity entity, long number, boolean client, boolean removed) {
        return client ? ClientAgent.get(entity, number, removed) : OutgoingRole.get(entity, number, removed, false);
    }
    
    /* -------------------------------------------------- Blockable -------------------------------------------------- */
    
    @Pure
    @Override
    public final @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public final @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = FreezableArray.get(3);
        elements.set(0, Integer64Wrapper.encode(NUMBER, getNumber()));
        elements.set(1, BooleanWrapper.encode(CLIENT, isClient()));
        elements.set(2, BooleanWrapper.encode(REMOVED, isRemoved()));
        return TupleWrapper.encode(TYPE, elements.freeze());
    }
    
    /**
     * Returns the agent with the number given by the block.
     * 
     * @param entity the entity to which the agent belongs.
     * @param block a block containing the number of the agent.
     * 
     * @return the agent with the number given by the block.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    @Pure
    public static @Nonnull Agent get(@Nonnull NonHostEntity entity, @Nonnull Block block) throws InvalidEncodingException, InternalException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(block).getNonNullableElements(3);
        final long number = Integer64Wrapper.decode(elements.getNonNullable(0));
        final boolean client = BooleanWrapper.decode(elements.getNonNullable(1));
        final boolean removed = BooleanWrapper.decode(elements.getNonNullable(2));
        return get(entity, number, client, removed);
    }
    
    /* -------------------------------------------------- SQLizable -------------------------------------------------- */
    
    /**
     * Stores the data type used to store instances of this class in the database.
     */
    public static final @Nonnull String FORMAT = "BIGINT";
    
    /**
     * Returns the foreign key constraint used to reference instances of this class.
     * 
     * @param site the site at which the foreign key constraint is declared.
     * 
     * @return the foreign key constraint used to reference instances of this class.
     */
    @NonCommitting
    public static @Nonnull String getReference(@Nonnull Site site) throws DatabaseException {
        AgentModule.createReferenceTable(site);
        return "REFERENCES " + site + "agent (entity, agent) ON DELETE CASCADE";
    }
    
    /**
     * Returns the given columns of the result set as an instance of this class.
     * 
     * @param entity the entity to which the agent belongs.
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndexes the indexes of the columns containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     * 
     * @require columnIndexes.length == 3 : "The number of given indexes is 3.";
     */
    @Pure
    @NonCommitting
    public static @Nullable Agent get(@Nonnull NonHostEntity entity, @Nonnull ResultSet resultSet, int... columnIndexes) throws DatabaseException {
        assert columnIndexes.length == 3 : "The number of given indexes is 3.";
        
        final long number = resultSet.getLong(columnIndexes[0]);
        if (resultSet.wasNull()) { return null; }
        return get(entity, number, resultSet.getBoolean(columnIndexes[1]), resultSet.getBoolean(columnIndexes[2]));
    }
    
    /**
     * Returns the given columns of the result set as an instance of this class.
     * 
     * @param entity the entity to which the agent belongs.
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndexes the indexes of the columns containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     * 
     * @require columnIndexes.length == 3 : "The number of given indexes is 3.";
     */
    @Pure
    @NonCommitting
    public static @Nonnull Agent getNotNull(@Nonnull NonHostEntity entity, @Nonnull ResultSet resultSet, int... columnIndexes) throws DatabaseException {
        assert columnIndexes.length == 3 : "The number of given indexes is 3.";
        
        return get(entity, resultSet.getLong(columnIndexes[0]), resultSet.getBoolean(columnIndexes[1]), resultSet.getBoolean(columnIndexes[2]));
    }
    
    @Override
    @NonCommitting
    public final void set(@NonCapturable @Nonnull ValueCollector collector) throws DatabaseException {
        preparedStatement.setLong(parameterIndex, getNumber());
    }
    
    /**
     * Sets the parameter at the given index of the prepared statement to the given agent.
     * 
     * @param agent the agent to which the parameter at the given index is to be set.
     * @param preparedStatement the prepared statement whose parameter is to be set.
     * @param parameterIndex the index of the parameter to set.
     */
    @NonCommitting
    public static void set(@Nullable Agent agent, @NonCapturable @Nonnull ValueCollector collector) throws DatabaseException {
        if (agent == null) { preparedStatement.setNull(parameterIndex, Types.BIGINT); }
        else { agent.set(preparedStatement, parameterIndex); }
    }
    
}
