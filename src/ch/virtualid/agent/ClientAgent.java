package ch.virtualid.agent;

import ch.virtualid.annotations.OnlyForActions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Image;
import ch.virtualid.client.Commitment;
import ch.virtualid.client.Synchronizer;
import ch.virtualid.concept.Aspect;
import ch.virtualid.concept.Instance;
import ch.virtualid.concept.Observer;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.NonHostEntity;
import ch.virtualid.handler.action.internal.ClientAgentCommitmentReplace;
import ch.virtualid.handler.action.internal.ClientAgentIconReplace;
import ch.virtualid.handler.action.internal.ClientAgentNameReplace;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.virtualid.module.both.Agents;
import ch.virtualid.util.ConcurrentHashMap;
import ch.virtualid.util.ConcurrentMap;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models a client agent that acts on behalf of an {@link Identity identity}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class ClientAgent extends Agent implements Immutable, Blockable, SQLizable {
    
    /**
     * Stores the aspect of the commitment being changed at the observed client agent.
     */
    public static final @Nonnull Aspect COMMITMENT = new Aspect(ClientAgent.class, "commitment changed");
    
    /**
     * Stores the aspect of the name being changed at the observed client agent.
     */
    public static final @Nonnull Aspect NAME = new Aspect(ClientAgent.class, "name changed");
    
    /**
     * Stores the aspect of the icon being changed at the observed client agent.
     */
    public static final @Nonnull Aspect ICON = new Aspect(ClientAgent.class, "icon changed");
    
    
    /**
     * Stores the commitment of this client agent.
     */
    private @Nullable Commitment commitment;
    
    /**
     * Stores the name of this client agent.
     * 
     * @invariant Client.isValid(name) : "The name is valid.";
     */
    private @Nullable String name;
    
    /**
     * Stores the icon of this client agent.
     * 
     * @invariant Client.isValid(icon) : "The icon is valid.";
     */
    private @Nullable Image icon;
    
    /**
     * Creates a new client agent with the given entity and number.
     * 
     * @param entity the entity to which this client agent belongs.
     * @param number the number that references this client agent.
     * @param removed whether this client agent has been removed.
     */
    private ClientAgent(@Nonnull NonHostEntity entity, long number, boolean removed) {
        super(entity, number, removed);
    }
    
    
    /**
     * Returns the commitment of this client agent.
     * 
     * @return the commitment of this client agent.
     */
    public @Nonnull Commitment getCommitment() throws SQLException {
        if (commitment == null) commitment = Agents.getCommitment(this);
        return commitment;
    }
    
    /**
     * Sets the commitment of this client agent to the given commitment.
     * 
     * @param newCommitment the new commitment of this client agent.
     * 
     * @require isOnClient() : "This client agent is on a client.";
     */
    public void setCommitment(@Nonnull Commitment newCommitment) throws SQLException {
        final @Nonnull Commitment oldCommitment = getCommitment();
        if (!newCommitment.equals(oldCommitment)) {
            Synchronizer.execute(new ClientAgentCommitmentReplace(this, oldCommitment, newCommitment));
        }
    }
    
    /**
     * Replaces the commitment of this client agent.
     * 
     * @param oldCommitment the old commitment of this client agent.
     * @param newCommitment the new commitment of this client agent.
     */
    @OnlyForActions
    public void replaceCommitment(@Nonnull Commitment oldCommitment, @Nonnull Commitment newCommitment) throws SQLException {
        Agents.replaceCommitment(this, oldCommitment, newCommitment);
        commitment = newCommitment;
        notify(COMMITMENT);
    }
    
    
    /**
     * Returns the name of this client agent.
     * 
     * @return the name of this client agent.
     * 
     * @ensure Client.isValid(return) : "The returned name is valid.";
     */
    public @Nonnull String getName() throws SQLException {
        if (name == null) name = Agents.getName(this);
        return name;
    }
    
    /**
     * Sets the name of this client agent.
     * 
     * @param newName the new name of this client agent.
     * 
     * @require isOnClient() : "This client agent is on a client.";
     * @require Client.isValid(newName) : "The new name is valid.";
     */
    public void setName(@Nonnull String newName) throws SQLException {
        final @Nonnull String oldName = getName();
        if (!newName.equals(oldName)) {
            Synchronizer.execute(new ClientAgentNameReplace(this, oldName, newName));
        }
    }
    
    /**
     * Replaces the name of this client agent.
     * 
     * @param oldName the old name of this client agent.
     * @param newName the new name of this client agent.
     * 
     * @require Client.isValid(oldName) : "The old name is valid.";
     * @require Client.isValid(newName) : "The new name is valid.";
     */
    @OnlyForActions
    public void replaceName(@Nonnull String oldName, @Nonnull String newName) throws SQLException {
        Agents.replaceName(this, oldName, newName);
        name = newName;
        notify(NAME);
    }
    
    
    /**
     * Returns the icon of this client agent.
     * 
     * @return the icon of this client agent.
     * 
     * @ensure Client.isValid(return) : "The returned icon is valid.";
     */
    public @Nonnull Image getIcon() throws SQLException {
        if (icon == null) icon = Agents.getIcon(this);
        return icon;
    }
    
    /**
     * Sets the icon of this client agent.
     * 
     * @param newIcon the new icon of this client agent.
     * 
     * @require isOnClient() : "This client agent is on a client.";
     * @require Client.isValid(newIcon) : "The new icon is valid.";
     */
    public void setIcon(@Nonnull Image newIcon) throws SQLException {
        final @Nonnull Image oldIcon = getIcon();
        if (!newIcon.equals(oldIcon)) {
            Synchronizer.execute(new ClientAgentIconReplace(this, oldIcon, newIcon));
        }
    }
    
    /**
     * Replaces the icon of this client agent.
     * 
     * @param oldIcon the old icon of this client agent.
     * @param newIcon the new icon of this client agent.
     * 
     * @require Client.isValid(oldIcon) : "The old icon is valid.";
     * @require Client.isValid(newIcon) : "The new icon is valid.";
     */
    @OnlyForActions
    public void replaceIcon(@Nonnull Image oldIcon, @Nonnull Image newIcon) throws SQLException {
        Agents.replaceIcon(this, oldIcon, newIcon);
        icon = newIcon;
        notify(ICON);
    }
    
    
    @Override
    public void reset() {
        this.commitment = null;
        this.name = null;
        this.icon = null;
        super.reset();
    }
    
    
    @Pure
    @Override
    public boolean isClient() {
        return true;
    }
    
    
    /**
     * Creates this client agent in the database.
     * 
     * @param permissions the permissions of the client agent.
     * @param restrictions the restrictions of the client agent.
     * @param commitment the commitment of the client agent.
     * @param name the name of the given client agent.
     * @param icon the icon of the given client agent.
     * 
     * @require permissions.isFrozen() : "The permissions are frozen.";
     * @require Client.isValid(name) : "The name is valid.";
     * @require Client.isValid(icon) : "The icon is valid.";
     */
    @OnlyForActions
    public void createForActions(@Nonnull ReadonlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nonnull Commitment commitment, @Nonnull String name, @Nonnull Image icon) throws SQLException {
        Agents.addClientAgent(this, permissions, restrictions, commitment, name, icon);
        this.permissions = permissions.clone();
        this.restrictions = restrictions;
        this.commitment = commitment;
        this.name = name;
        this.icon = icon;
        notify(Agent.CREATED);
    }
    
    
    /**
     * Caches client agents given their entity and number.
     */
    private static final @Nonnull ConcurrentMap<NonHostEntity, ConcurrentMap<Long, ClientAgent>> index = new ConcurrentHashMap<NonHostEntity, ConcurrentMap<Long, ClientAgent>>();
    
    static {
        if (Database.isSingleAccess()) {
            Instance.observeAspects(new Observer() {
                @Override public void notify(@Nonnull Aspect aspect, @Nonnull Instance instance) { index.remove(instance); }
            }, Entity.DELETED);
        }
    }
    
    /**
     * Returns a (locally cached) client agent that might not (yet) exist in the database.
     * 
     * @param entity the entity to which the client agent belongs.
     * @param number the number that denotes the client agent.
     * @param removed whether the client agent has been removed.
     * 
     * @return a new or existing client agent with the given entity and number.
     */
    @Pure
    public static @Nonnull ClientAgent get(@Nonnull NonHostEntity entity, long number, boolean removed) {
        if (Database.isSingleAccess()) {
            @Nullable ConcurrentMap<Long, ClientAgent> map = index.get(entity);
            if (map == null) map = index.putIfAbsentElseReturnPresent(entity, new ConcurrentHashMap<Long, ClientAgent>());
            @Nullable ClientAgent clientAgent = map.get(number);
            if (clientAgent == null) clientAgent = map.putIfAbsentElseReturnPresent(number, new ClientAgent(entity, number, removed));
            return clientAgent;
        } else {
            return new ClientAgent(entity, number, removed);
        }
    }
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param entity the entity to which the client agent belongs.
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * @param removed whether the client agent has been removed.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    public static @Nonnull ClientAgent get(@Nonnull NonHostEntity entity, @Nonnull ResultSet resultSet, int columnIndex, boolean removed) throws SQLException {
        return get(entity, resultSet.getLong(columnIndex), removed);
    }
    
    /**
     * Resets the client agents of the given entity after having reloaded the agents module.
     * 
     * @param entity the entity whose client agents are to be reset.
     */
    public static void reset(@Nonnull NonHostEntity entity) {
        if (Database.isSingleAccess()) {
            final @Nullable ConcurrentMap<Long, ClientAgent> map = index.get(entity);
            if (map != null) for (final @Nonnull ClientAgent clientAgent : map.values()) clientAgent.reset();
        }
    }
    
}
