package ch.virtualid.agent;

import ch.virtualid.annotations.OnlyForActions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.concept.Aspect;
import ch.virtualid.concept.Concept;
import ch.virtualid.entity.Entity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.virtualid.module.both.Agents;
import ch.virtualid.packet.PacketError;
import ch.virtualid.packet.PacketException;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.ReadonlyArray;
import ch.xdf.Block;
import ch.xdf.BooleanWrapper;
import ch.xdf.Int64Wrapper;
import ch.xdf.TupleWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models an agent that acts on behalf of a virtual identity.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.6
 */
public abstract class Agent extends Concept implements Immutable, Blockable, SQLizable {
    
    /**
     * Stores the aspect of the permissions being changed at the observed agent.
     */
    public static final @Nonnull Aspect PERMISSIONS = new Aspect(Agent.class, "permissions changed");
    
    /**
     * Stores the aspect of the restrictions being changed at the observed agent.
     */
    public static final @Nonnull Aspect RESTRICTIONS = new Aspect(Agent.class, "authentications changed");
    
    /**
     * Stores the aspect of the authorization being restricted at the observed agent.
     */
    public static final @Nonnull Aspect RESTRICTED = new Aspect(Agent.class, "authorization restricted");
    
    /**
     * Stores the aspect of the observed agent being created in the database.
     */
    public static final @Nonnull Aspect CREATED = new Aspect(Agent.class, "created");
    
    /**
     * Stores the aspect of the observed agent being deleted from the database.
     */
    public static final @Nonnull Aspect DELETED = new Aspect(Agent.class, "deleted");
    
    
    /**
     * Stores the data type used to store instances of this class in the database.
     */
    public static final @Nonnull String FORMAT = "BIGINT";
    
    /**
     * Stores the foreign key constraint used to reference instances of this class.
     */
    public static final @Nonnull String REFERENCE = "REFERENCES agent (entity, agent) ON DELETE CASCADE ON UPDATE CASCADE";
    
    
    /**
     * Stores the semantic type {@code client.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType CLIENT = SemanticType.create("client.agent@virtualid.ch").load(BooleanWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code number.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType NUMBER = SemanticType.create("number.agent@virtualid.ch").load(Int64Wrapper.TYPE);
    
    /**
     * Stores the semantic type {@code agent@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("agent@virtualid.ch").load(TupleWrapper.TYPE, CLIENT, NUMBER);
    
    
    /**
     * Stores the number that references this agent in the database.
     */
    private final long number;
    
    /**
     * Stores the permissions of this agent or null if not yet loaded.
     */
    private @Nullable AgentPermissions permissions;
    
    /**
     * Stores whether the restrictions have been loaded from the database.
     */
    private boolean restrictionsLoaded = false;
    
    /**
     * Stores the restrictions of this agent or null if not yet loaded or set.
     */
    private @Nullable Restrictions restrictions;
    
    /**
     * Stores whether this agent has been restricted and can thus no longer be altered.
     */
    private boolean restricted = false;
    
    /**
     * Stores whether the removed status has been loaded from the database.
     */
    private boolean removedLoaded = false;
    
    /**
     * Stores whether this agent has been removed from the database.
     */
    private boolean removed = false;
    
    
    /**
     * Creates a new agent with the given entity and number.
     * 
     * @param entity the entity to which this agent belongs.
     * @param number the number that references this agent.
     */
    protected Agent(@Nonnull Entity entity, long number) {
        super(entity);
        
        this.number = number;
    }
    
    
    /**
     * Returns the number that references this agent.
     * 
     * @return the number that references this agent.
     */
    @Pure
    public final long getNumber() {
        return number;
    }
    
    
    /**
     * Returns the permissions of this agent.
     * 
     * @return the permissions of this agent.
     */
    @Pure
    public final @Nonnull ReadonlyAgentPermissions getPermissions() throws SQLException {
        if (permissions == null) {
            permissions = Agents.getPermissions(this);
        }
        return permissions;
    }
    
    /**
     * Adds the given permissions to this agent.
     * 
     * @param permissions the permissions to be add.
     * 
     * @require !isRestricted() : "The authorization of this agent may not have been restricted.";
     */
    public final void addPermissions(@Nonnull ReadonlyAgentPermissions permissions) throws SQLException {
        assert !isRestricted() : "The authorization of this agent may not have been restricted.";
        
        if (!permissions.isEmpty()) {
            Synchronizer.execute(new AgentPermissionsAdd(this, permissions));
        }
    }
    
    
    /**
     * Adds the given permissions to this agent.
     * 
     * @param newPermissions the permissions to be added to this agent.
     * 
     * @require !newPermissions.isEmpty() : "The new permissions are not empty.";
     */
    @OnlyForActions
    public final void addPermissionsForActions(@Nonnull ReadonlyAgentPermissions newPermissions) throws SQLException {
        assert !newPermissions.isEmpty() : "The new permissions are not empty.";
        
        Agents.addPermissions(this, newPermissions);
        if (permissions != null) permissions.addAll(newPermissions);
        notify(PERMISSIONS);
    }
    
    /**
     * Removes the given permissions from this agent.
     * 
     * @param permissions the permissions to be removed from this agent.
     * 
     * @require !isRestricted() : "The authorization of this agent may not have been restricted.";
     */
    public final void removePermissions(@Nonnull ReadonlyAgentPermissions permissions) throws SQLException {
        assert !isRestricted() : "The authorization of this agent may not have been restricted.";
        
        if (!permissions.isEmpty()) {
            Synchronizer.execute(new AgentPermissionsRemove(this, permissions));
        }
    }
    
    /**
     * Removes the given permissions from this agent.
     * 
     * @param oldPermissions the permissions to be removed from this agent.
     * 
     * @require !oldPermissions.isEmpty() : "The old permissions are not empty.";
     */
    @OnlyForActions
    public final void removePermissionsForActions(@Nonnull ReadonlyAgentPermissions oldPermissions) throws SQLException {
        assert !oldPermissions.isEmpty() : "The old permissions are not empty.";
        
        Agents.removePermissions(this, oldPermissions);
        if (permissions != null) permissions.removeAll(permissions);
        notify(PERMISSIONS);
    }
    
    
    /**
     * Returns the restrictions of this agent or null if not yet set.
     * 
     * @return the restrictions of this agent or null if not yet set.
     */
    @Pure
    public final @Nullable Restrictions getRestrictions() throws SQLException {
        if (!restrictionsLoaded) {
            restrictions = Agents.getRestrictions(this);
            restrictionsLoaded = true;
        }
        return restrictions;
    }
    
    /**
     * Sets the restrictions of this agent.
     * 
     * @param newRestrictions the new restrictions of this agent.
     * 
     * @require !isRestricted() : "The authorization of this agent may not have been restricted.";
     */
    public final void setRestrictions(@Nonnull Restrictions newRestrictions) throws SQLException {
        assert !isRestricted() : "The authorization of this agent may not have been restricted.";
        
        final @Nullable Restrictions oldRestrictions = getRestrictions();
        if (!newRestrictions.equals(oldRestrictions)) {
            Synchronizer.execute(new AgentRestrictionsReplace(this, oldRestrictions, newRestrictions));
        }
    }
    
    /**
     * Replaces the restrictions of this agent.
     * 
     * @param oldRestrictions the old restrictions of this agent.
     * @param newRestrictions the new restrictions of this agent.
     */
    @OnlyForActions
    public void replaceRestrictions(@Nullable Restrictions oldRestrictions, @Nonnull Restrictions newRestrictions) throws SQLException {
        Agents.replaceRestrictions(this, oldRestrictions, newRestrictions);
        restrictions = newRestrictions;
        restrictionsLoaded = true;
        notify(RESTRICTIONS);
    }
    
    
    /**
     * Returns whether this agent covers the given agent.
     * 
     * @param agent the agent that needs to be covered.
     * @return whether this agent covers the given agent.
     */
    public boolean covers(@Nonnull Agent agent) throws SQLException {
        final @Nullable Restrictions thisRestrictions = getRestrictions();
        final @Nullable Restrictions otherRestrictions = agent.getRestrictions();
        return (otherRestrictions == null || thisRestrictions != null && thisRestrictions.cover(otherRestrictions)) && getPermissions().cover(agent.getPermissions());
    }
    
    /**
     * Checks whether this agent covers the given agent and throws a {@link PacketException} if not.
     * 
     * @param agent the agent that needs to be covered.
     */
    public void checkCoverage(@Nonnull Agent agent) throws PacketException, SQLException {
        if (!covers(agent)) throw new PacketException(PacketError.AUTHORIZATION);
    }
    
    
    /**
     * Returns whether this agent has been removed from the database.
     * 
     * @return whether this agent has been removed from the database.
     */
    public final boolean isRemoved() throws SQLException {
        if (!removedLoaded) {
            removed = Agents.isRemoved(this);
            removedLoaded = true;
        }
        return removed;
    }
    
    /**
     * Removes this agent from the database.
     * 
     * @require !isRestricted() : "The authorization of this agent may not have been restricted.";
     */
    public final void remove() throws SQLException {
        assert !isRestricted() : "The authorization of this agent may not have been restricted.";
        
        Synchronizer.execute(new AgentRemove(this));
    }
    
    /**
     * Removes this agent from the database.
     */
    @OnlyForActions
    public final void removeForActions() throws SQLException {
        Agents.removeAgent(this);
        removed = true;
        removedLoaded = true;
        notify(DELETED);
    }
    
    
    /**
     * Returns whether this agent has been restricted.
     * 
     * @return whether this agent has been restricted.
     */
    @Pure
    public final boolean isRestricted() {
        return restricted;
    }
    
    /**
     * Sets this agent to have been restricted.
     */
    protected final void setRestricted() {
        restricted = true;
    }
    
    
    /**
     * Returns whether this agent is a client.
     * 
     * @return whether this agent is a client.
     */
    @Pure
    public abstract boolean isClient();
    
    
    @Pure
    @Override
    public final @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public final @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<Block>(2);
        elements.set(0, new BooleanWrapper(CLIENT, isClient()).toBlock());
        elements.set(1, new Int64Wrapper(NUMBER, getNumber()).toBlock());
        return new TupleWrapper(getType(), elements.freeze()).toBlock();
    }
    
    /**
     * Returns the agent with the number given by the block.
     * 
     * @param entity the entity to which the agent belongs.
     * @param block a block containing the number of the agent.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    @Pure
    public static @Nonnull Agent get(@Nonnull Entity entity, @Nonnull Block block) throws InvalidEncodingException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(2);
        final boolean client = new BooleanWrapper(elements.getNotNull(0)).getValue();
        final long number = new Int64Wrapper(elements.getNotNull(1)).getValue();
        return client ? ClientAgent.get(entity, number) : OutgoingRole.get(entity, number);
    }
    
    @Override
    public final void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        preparedStatement.setLong(parameterIndex, getNumber());
    }
    
    
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
        return (int) (this.number ^ (this.number >>> 32));
    }
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        return String.valueOf(number);
    }
    
}
