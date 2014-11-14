package ch.virtualid.agent;

import ch.virtualid.annotations.OnlyForActions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.concept.Aspect;
import ch.virtualid.concept.Concept;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Site;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.virtualid.module.both.Agents;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.ReadonlyArray;
import ch.xdf.Block;
import ch.xdf.BooleanWrapper;
import ch.xdf.Int64Wrapper;
import ch.xdf.TupleWrapper;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models an agent that acts on behalf of an {@link Identity identity}.
 * 
 * @see ClientAgent
 * @see OutgoingRole
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.8
 */
public abstract class Agent extends Concept implements Immutable, Blockable, SQLizable {
    
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
     * Stores the aspect of the permissions being changed at the observed agent.
     */
    public static final @Nonnull Aspect PERMISSIONS = new Aspect(Agent.class, "permissions changed");
    
    /**
     * Stores the aspect of the restrictions being changed at the observed agent.
     */
    public static final @Nonnull Aspect RESTRICTIONS = new Aspect(Agent.class, "restrictions changed");
    
    
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
    public static @Nonnull String getReference(@Nonnull Site site) throws SQLException {
        Agents.createReferenceTable(site);
        return "REFERENCES " + site + "agent (entity, agent) ON DELETE CASCADE";
    }
    
    
    /**
     * Stores the semantic type {@code number.agent@virtualid.ch}.
     */
    public static final @Nonnull SemanticType NUMBER = SemanticType.create("number.agent@virtualid.ch").load(Int64Wrapper.TYPE);
    
    /**
     * Stores the semantic type {@code client.agent@virtualid.ch}.
     */
    public static final @Nonnull SemanticType CLIENT = SemanticType.create("client.agent@virtualid.ch").load(BooleanWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code removed.agent@virtualid.ch}.
     */
    public static final @Nonnull SemanticType REMOVED = SemanticType.create("removed.agent@virtualid.ch").load(BooleanWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code agent@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("agent@virtualid.ch").load(TupleWrapper.TYPE, NUMBER, CLIENT, REMOVED);
    
    
    /**
     * Stores the number that references this agent in the database.
     */
    private final long number;
    
    /**
     * Stores whether this agent has been removed.
     */
    private boolean removed;
    
    /**
     * Stores the permissions of this agent or null if not yet loaded.
     */
    protected @Nullable AgentPermissions permissions;
    
    /**
     * Stores the restrictions of this agent or null if not yet loaded.
     */
    protected @Nullable Restrictions restrictions;
    
    
    /**
     * Creates a new agent with the given entity and number.
     * 
     * @param entity the entity to which this agent belongs.
     * @param number the number that references this agent.
     * @param removed whether this agent has been removed.
     */
    protected Agent(@Nonnull Entity entity, long number, boolean removed) {
        super(entity);
        
        this.number = number;
        this.removed = removed;
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
     * Returns whether this agent has been removed.
     * <p>
     * <em>Important:</em> If the agent comes from a block, this information is not to be trusted!
     * 
     * @return whether this agent has been removed.
     */
    @Pure
    public final boolean isRemoved() {
        return removed;
    }
    
    /**
     * Checks that this agent is not removed and throws a {@link PacketException} otherwise.
     */
    @Pure
    public void checkNotRemoved() throws PacketException {
        if (isRemoved()) throw new PacketException(PacketError.AUTHORIZATION, "The agent has been removed.");
    }
    
    /**
     * Removes this agent from the database by marking it as being removed.
     */
    public final void remove() throws SQLException {
//        Synchronizer.execute(new AgentRemove(this));
    }
    
    /**
     * Removes this agent from the database by marking it as being removed.
     */
    @OnlyForActions
    public final void removeForActions() throws SQLException {
//        Agents.removeAgent(this);
        removed = true;
        notify(DELETED);
    }
    
    /**
     * Unremoves this agent from the database by marking it as no longer being removed.
     */
    public final void unremove() throws SQLException {
//        Synchronizer.execute(new AgentUnremove(this));
    }
    
    /**
     * Unremoves this agent from the database by marking it as no longer being removed.
     */
    @OnlyForActions
    public final void unremoveForActions() throws SQLException {
//        Agents.unremoveAgent(this);
        removed = false;
        notify(CREATED);
    }
    
    
    /**
     * Returns the permissions of this agent.
     * 
     * @return the permissions of this agent.
     */
    @Pure
    public final @Nonnull ReadonlyAgentPermissions getPermissions() throws SQLException {
        if (permissions == null) {
            throw new SQLException();
//            permissions = Agents.getPermissions(this);
        }
        return permissions;
    }
    
    /**
     * Adds the given permissions to this agent.
     * 
     * @param permissions the permissions to be add.
     */
    public final void addPermissions(@Nonnull ReadonlyAgentPermissions permissions) throws SQLException {
        if (!permissions.isEmpty()) {
//            Synchronizer.execute(new AgentPermissionsAdd(this, permissions));
        }
    }
    
    /**
     * Adds the given permissions to this agent.
     * 
     * @param newPermissions the permissions to be added to this agent.
     * 
     * @require newPermissions.isNotEmpty() : "The new permissions are not empty.";
     */
    @OnlyForActions
    public final void addPermissionsForActions(@Nonnull ReadonlyAgentPermissions newPermissions) throws SQLException {
        assert newPermissions.isNotEmpty() : "The new permissions are not empty.";
        
//        Agents.addPermissions(this, newPermissions);
//        if (permissions != null) permissions.addAll(newPermissions);
        notify(PERMISSIONS);
    }
    
    /**
     * Removes the given permissions from this agent.
     * 
     * @param permissions the permissions to be removed from this agent.
     */
    public final void removePermissions(@Nonnull ReadonlyAgentPermissions permissions) throws SQLException {
        if (!permissions.isEmpty()) {
//            Synchronizer.execute(new AgentPermissionsRemove(this, permissions));
        }
    }
    
    /**
     * Removes the given permissions from this agent.
     * 
     * @param oldPermissions the permissions to be removed from this agent.
     * 
     * @require oldPermissions.isNotEmpty() : "The old permissions are not empty.";
     */
    @OnlyForActions
    public final void removePermissionsForActions(@Nonnull ReadonlyAgentPermissions oldPermissions) throws SQLException {
        assert oldPermissions.isNotEmpty() : "The old permissions are not empty.";
        
//        Agents.removePermissions(this, oldPermissions);
        if (permissions != null) permissions.removeAll(permissions);
        notify(PERMISSIONS);
    }
    
    
    /**
     * Returns the restrictions of this agent.
     * 
     * @return the restrictions of this agent.
     */
    @Pure
    public final @Nonnull Restrictions getRestrictions() throws SQLException {
        if (restrictions == null) {
            throw new SQLException();
//            restrictions = Agents.getRestrictions(this);
        }
        return restrictions;
    }
    
    /**
     * Sets the restrictions of this agent.
     * 
     * @param newRestrictions the new restrictions of this agent.
     */
    public final void setRestrictions(@Nonnull Restrictions newRestrictions) throws SQLException {
        final @Nullable Restrictions oldRestrictions = getRestrictions();
        if (!newRestrictions.equals(oldRestrictions)) {
//            Synchronizer.execute(new AgentRestrictionsReplace(this, oldRestrictions, newRestrictions));
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
//        Agents.replaceRestrictions(this, oldRestrictions, newRestrictions);
        restrictions = newRestrictions;
        notify(RESTRICTIONS);
    }
    
    
    /**
     * Returns whether this agent covers the given agent.
     * 
     * @param agent the agent that needs to be covered.
     * 
     * @return whether this agent covers the given agent.
     */
    @Pure
    public boolean covers(@Nonnull Agent agent) throws SQLException {
        return !isRemoved() && getPermissions().cover(agent.getPermissions()) && getRestrictions().cover(agent.getRestrictions());
    }
    
    /**
     * Checks whether this agent covers the given agent and throws a {@link PacketException} if not.
     * 
     * @param agent the agent that needs to be covered.
     */
    @Pure
    public void checkCovers(@Nonnull Agent agent) throws PacketException, SQLException {
        if (!covers(agent)) throw new PacketException(PacketError.AUTHORIZATION, "This agent does not cover the other agent.");
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
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<Block>(3);
        elements.set(0, new Int64Wrapper(NUMBER, getNumber()).toBlock());
        elements.set(1, new BooleanWrapper(CLIENT, isClient()).toBlock());
        elements.set(2, new BooleanWrapper(REMOVED, isRemoved()).toBlock());
        return new TupleWrapper(TYPE, elements.freeze()).toBlock();
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
    public static @Nonnull Agent get(@Nonnull Entity entity, long number, boolean client, boolean removed) {
        return client ? ClientAgent.get(entity, number, removed) : OutgoingRole.get(entity, number, removed, false);
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
    public static @Nonnull Agent get(@Nonnull Entity entity, @Nonnull Block block) throws InvalidEncodingException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(3);
        final long number = new Int64Wrapper(elements.getNotNull(0)).getValue();
        final boolean client = new BooleanWrapper(elements.getNotNull(1)).getValue();
        final boolean removed = new BooleanWrapper(elements.getNotNull(2)).getValue();
        return get(entity, number, client, removed);
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
