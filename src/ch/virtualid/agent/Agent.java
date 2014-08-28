package ch.virtualid.agent;

import ch.virtualid.annotations.OnlyForActions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Synchronizer;
import ch.virtualid.concept.Aspect;
import ch.virtualid.concept.Concept;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Entity;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import static ch.virtualid.io.Level.ERROR;
import ch.virtualid.module.both.Agents;
import ch.virtualid.packet.PacketError;
import ch.virtualid.packet.PacketException;
import ch.virtualid.server.Host;
import ch.xdf.Block;
import ch.xdf.BooleanWrapper;
import ch.xdf.TupleWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models an agent that acts on behalf of a virtual identity.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.9
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
        
        Host.setRestrictions(this, newRestrictions);
        this.restrictions = newRestrictions;
        this.restrictionsLoaded = true;
        
        // TODO: notify(null);
    }
    
    
    /**
     * Returns whether this authorization covers the given authorization.
     * 
     * @param authorization the authorization that needs to be covered.
     * @return whether this authorization covers the given authorization.
     */
    public boolean covers(@Nonnull Agent authorization) throws SQLException {
        @Nullable Restrictions thisRestrictions = getRestrictions();
        @Nullable Restrictions otherRestrictions = authorization.getRestrictions();
        return (otherRestrictions == null || thisRestrictions != null && thisRestrictions.cover(otherRestrictions)) && getPermissions().cover(authorization.getPermissions());
    }
    
    /**
     * Checks whether this authorization covers the given authorization and throws a {@link PacketException} if not.
     * 
     * @param authorization the authorization that needs to be covered.
     */
    public void checkCoverage(@Nonnull Agent authorization) throws PacketException, SQLException {
        if (!covers(authorization)) throw new PacketException(PacketError.AUTHORIZATION);
    }
    
    
    /**
     * Returns whether this authorization has been restricted.
     * 
     * @return whether this authorization has been restricted.
     */
    public final boolean isRestricted() {
        return restricted;
    }
    
    /**
     * Sets this authorization to have been restricted.
     */
    protected final void setRestricted() {
        restricted = true;
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
     * @require !isRestricted() : "This agent may not have been restricted.";
     */
    public final void remove() throws SQLException {
        // TODO
    }
    
    /**
     * Returns whether this authorization belongs to a client.
     * 
     * @return whether this authorization belongs to a client.
     */
    @Pure
    public abstract boolean isClient();
    
    
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
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        try {
            // TODO: Only return the authorization number? -> Together with the actual class!
            // -> Only among agents? Save incoming roles as (issuer, relation)?
            @Nonnull Block[] tuple = new Block[2];
            tuple[0] = Block.toBlock(getRestrictions());
            tuple[1] = getPermissions().toBlock();
            return new TupleWrapper(tuple).toBlock();
        } catch (@Nonnull SQLException exception) {
            Database.logger.log(ERROR, "Could not load the restrictions or permissions of an authorization.", exception);
            return Block.EMPTY;
        }
    }
    
    
    /**
     * Creates a new agent with the given connection and identity from the given block.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity at which this agent is authorized.
     * @param block the block containing the agent.
     * @return the new agent with the given connection and identity from the given block.
     */
    public final @Nonnull Agent create(@Nonnull Connection connection, @Nonnull NonHostIdentity identity, @Nonnull Block block) throws InvalidEncodingException, FailedIdentityException {
        @Nonnull Block[] tuple = new TupleWrapper(block).getElementsNotNull(2);
        if (new BooleanWrapper(tuple[1]).getValue()) return new ClientAgent(connection, identity, block);
        else return new OutgoingRole(connection, identity, block);
    }
    
    
    /**
     * Redetermines which agents are stronger and weaker than this agent.
     */
    public final void redetermineAgents() throws SQLException {
        Host.redetermineAgents(this);
    }
    
    /**
     * Removes this agent from the database.
     * Make sure to call {@link #redetermineAgents()} afterwards.
     */
    @Override
    public final void remove() throws SQLException {
        Host.removeAgent(this);
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        @Nonnull Block[] tuple = new Block[2];
        tuple[0] = super.toBlock();
        tuple[1] = new BooleanWrapper(this instanceof ClientAgent).toBlock();
        return new TupleWrapper(tuple).toBlock();
    }
    
}
