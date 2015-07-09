package net.digitalid.core.agent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Committing;
import net.digitalid.core.annotations.Frozen;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.OnlyForActions;
import net.digitalid.core.annotations.OnlyForClients;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.FreezableArray;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.ReadOnlyArray;
import net.digitalid.core.concept.Aspect;
import net.digitalid.core.concept.NonHostConcept;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.entity.Site;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.exceptions.packet.PacketError;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identity.Identity;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.interfaces.Blockable;
import net.digitalid.core.interfaces.SQLizable;
import net.digitalid.core.synchronizer.Synchronizer;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.BooleanWrapper;
import net.digitalid.core.wrappers.Int64Wrapper;
import net.digitalid.core.wrappers.TupleWrapper;

/**
 * This class models an agent that acts on behalf of an {@link Identity identity}.
 * 
 * @see ClientAgent
 * @see OutgoingRole
 * 
 * @see AgentModule
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public abstract class Agent extends NonHostConcept implements Blockable, SQLizable {
    
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Aspects –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code number.agent@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType NUMBER = SemanticType.create("number.agent@core.digitalid.net").load(Int64Wrapper.TYPE);
    
    /**
     * Stores the semantic type {@code client.agent@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType CLIENT = SemanticType.create("client.agent@core.digitalid.net").load(BooleanWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code removed.agent@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType REMOVED = SemanticType.create("removed.agent@core.digitalid.net").load(BooleanWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code agent@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("agent@core.digitalid.net").load(TupleWrapper.TYPE, NUMBER, CLIENT, REMOVED);
    
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Number –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Removed –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
     * Returns whether this agent is not removed.
     * <p>
     * <em>Important:</em> If the agent comes from a block, this information is not to be trusted!
     * 
     * @return whether this agent is not removed.
     */
    @Pure
    public final boolean isNotRemoved() {
        return !removed;
    }
    
    /**
     * Checks that this agent is not removed and throws a {@link PacketException} otherwise.
     */
    @Pure
    public final void checkNotRemoved() throws PacketException {
        if (isRemoved()) throw new PacketException(PacketError.AUTHORIZATION, "The agent has been removed.");
    }
    
    /**
     * Removes this agent from the database by marking it as being removed.
     * 
     * @require isOnClient() : "This agent is on a client.";
     * @require isNotRemoved() : "This agent is not removed.";
     */
    @Committing
    public final void remove() throws SQLException {
        assert isNotRemoved() : "This agent is not removed.";
        
        Synchronizer.execute(new AgentRemove(this));
    }
    
    /**
     * Removes this agent from the database by marking it as being removed.
     */
    @NonCommitting
    @OnlyForActions
    final void removeForActions() throws SQLException {
        AgentModule.removeAgent(this);
        if (isOnHost() && this instanceof OutgoingRole) ((OutgoingRole) this).revoke();
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
    public final void unremove() throws SQLException {
        assert isRemoved() : "This agent is removed.";
        
        Synchronizer.execute(new AgentUnremove(this));
    }
    
    /**
     * Unremoves this agent from the database by marking it as no longer being removed.
     */
    @NonCommitting
    @OnlyForActions
    final void unremoveForActions() throws SQLException {
        AgentModule.unremoveAgent(this);
        if (isOnHost() && this instanceof OutgoingRole) ((OutgoingRole) this).issue();
        removed = false;
        notify(CREATED);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Permissions –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    public final @Nonnull @NonFrozen ReadOnlyAgentPermissions getPermissions() throws SQLException {
        if (permissions == null) permissions = AgentModule.getPermissions(this);
        return permissions;
    }
    
    /**
     * Adds the given permissions to this agent.
     * <p>
     * <em>Important:</em> The additional permissions should not cover any existing permissions. If they do,
     * make sure to {@link #removePermissions(net.digitalid.core.agent.ReadonlyAgentPermissions) remove} them first.
     * 
     * @param permissions the permissions to be added to this agent.
     */
    @Committing
    @OnlyForClients
    public final void addPermissions(@Nonnull @Frozen ReadOnlyAgentPermissions permissions) throws SQLException {
        if (!permissions.isEmpty()) Synchronizer.execute(new AgentPermissionsAdd(this, permissions));
    }
    
    /**
     * Adds the given permissions to this agent.
     * 
     * @param newPermissions the permissions to be added to this agent.
     */
    @NonCommitting
    @OnlyForActions
    final void addPermissionsForActions(@Nonnull @Frozen ReadOnlyAgentPermissions newPermissions) throws SQLException {
        AgentModule.addPermissions(this, newPermissions);
        if (permissions != null) permissions.putAll(newPermissions);
        notify(PERMISSIONS);
    }
    
    /**
     * Removes the given permissions from this agent.
     * 
     * @param permissions the permissions to be removed from this agent.
     */
    @Committing
    @OnlyForClients
    public final void removePermissions(@Nonnull @Frozen ReadOnlyAgentPermissions permissions) throws SQLException {
        if (!permissions.isEmpty()) Synchronizer.execute(new AgentPermissionsRemove(this, permissions));
    }
    
    /**
     * Removes the given permissions from this agent.
     * 
     * @param oldPermissions the permissions to be removed from this agent.
     */
    @NonCommitting
    @OnlyForActions
    final void removePermissionsForActions(@Nonnull @Frozen ReadOnlyAgentPermissions oldPermissions) throws SQLException {
        AgentModule.removePermissions(this, oldPermissions);
        if (permissions != null) permissions.removeAll(oldPermissions);
        notify(PERMISSIONS);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Restrictions –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    public final @Nonnull Restrictions getRestrictions() throws SQLException {
        if (restrictions == null) restrictions = AgentModule.getRestrictions(this);
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
    @OnlyForClients
    public final void setRestrictions(@Nonnull Restrictions newRestrictions) throws SQLException {
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
    final void replaceRestrictions(@Nonnull Restrictions oldRestrictions, @Nonnull Restrictions newRestrictions) throws SQLException {
        AgentModule.replaceRestrictions(this, oldRestrictions, newRestrictions);
        restrictions = newRestrictions;
        notify(RESTRICTIONS);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Abstract –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Resets this agent.
     */
    public void reset() throws SQLException {
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Covering –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the agents that are weaker than this agent.
     * 
     * @return the agents that are weaker than this agent.
     * 
     * @ensure return.isNotFrozen() : "The list is not frozen.";
     */
    @Pure
    @NonCommitting
    public final @Capturable @Nonnull FreezableList<Agent> getWeakerAgents() throws SQLException {
        return AgentModule.getWeakerAgents(this);
    }
    
    /**
     * Returns the weaker agent with the given agent number.
     * 
     * @param agentNumber the number of the returned agent.
     * 
     * @return the weaker agent with the given agent number.
     * 
     * @throws SQLException if no such weaker agent is found.
     */
    @Pure
    @NonCommitting
    public final @Nonnull Agent getWeakerAgent(long agentNumber) throws SQLException {
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
    public final boolean covers(@Nonnull Agent agent) throws SQLException {
        return !isRemoved() && AgentModule.isStronger(this, agent);
    }
    
    /**
     * Checks whether this agent covers the given agent and throws a {@link PacketException} if not.
     * 
     * @param agent the agent that needs to be covered.
     */
    @Pure
    @NonCommitting
    public final void checkCovers(@Nonnull Agent agent) throws PacketException, SQLException {
        if (!covers(agent)) throw new PacketException(PacketError.AUTHORIZATION, "This agent does not cover the other agent.");
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructors –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Blockable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public final @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public final @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<>(3);
        elements.set(0, new Int64Wrapper(NUMBER, getNumber()).toBlock());
        elements.set(1, new BooleanWrapper(CLIENT, isClient()).toBlock());
        elements.set(2, new BooleanWrapper(REMOVED, isRemoved()).toBlock());
        return new TupleWrapper(TYPE, elements.freeze()).toBlock();
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
    public static @Nonnull Agent get(@Nonnull NonHostEntity entity, @Nonnull Block block) throws InvalidEncodingException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull ReadOnlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(3);
        final long number = new Int64Wrapper(elements.getNotNull(0)).getValue();
        final boolean client = new BooleanWrapper(elements.getNotNull(1)).getValue();
        final boolean removed = new BooleanWrapper(elements.getNotNull(2)).getValue();
        return get(entity, number, client, removed);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– SQLizable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    public static @Nonnull String getReference(@Nonnull Site site) throws SQLException {
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
    public static @Nullable Agent get(@Nonnull NonHostEntity entity, @Nonnull ResultSet resultSet, int... columnIndexes) throws SQLException {
        assert columnIndexes.length == 3 : "The number of given indexes is 3.";
        
        final long number = resultSet.getLong(columnIndexes[0]);
        if (resultSet.wasNull()) return null;
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
    public static @Nonnull Agent getNotNull(@Nonnull NonHostEntity entity, @Nonnull ResultSet resultSet, int... columnIndexes) throws SQLException {
        assert columnIndexes.length == 3 : "The number of given indexes is 3.";
        
        return get(entity, resultSet.getLong(columnIndexes[0]), resultSet.getBoolean(columnIndexes[1]), resultSet.getBoolean(columnIndexes[2]));
    }
    
    @Override
    @NonCommitting
    public final void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
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
    public static void set(@Nullable Agent agent, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        if (agent == null) preparedStatement.setNull(parameterIndex, Types.BIGINT);
        else agent.set(preparedStatement, parameterIndex);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Casting –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns this agent as a {@link ClientAgent}.
     * 
     * @return this agent as a {@link ClientAgent}.
     * 
     * @throws InvalidEncodingException if this agent is not an instance of {@link ClientAgent}.
     */
    @Pure
    public final @Nonnull ClientAgent toClientAgent() throws InvalidEncodingException {
        if (this instanceof ClientAgent) return (ClientAgent) this;
        throw new InvalidEncodingException("This agent cannot be cast to ClientAgent.");
    }
    
    /**
     * Returns this agent as an {@link OutgoingRole}.
     * 
     * @return this agent as an {@link OutgoingRole}.
     * 
     * @throws InvalidEncodingException if this agent is not an instance of {@link OutgoingRole}.
     */
    @Pure
    public final @Nonnull OutgoingRole toOutgoingRole() throws InvalidEncodingException {
        if (this instanceof OutgoingRole) return (OutgoingRole) this;
        throw new InvalidEncodingException("This agent cannot be cast to OutgoingRole.");
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Object –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public final boolean equals(Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof Agent)) return false;
        @Nonnull Agent other = (Agent) object;
        return this.number == other.number;
    }
    
    @Pure
    @Override
    public final int hashCode() {
        return (int) (number ^ (number >>> 32));
    }
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        return String.valueOf(number);
    }
    
}
