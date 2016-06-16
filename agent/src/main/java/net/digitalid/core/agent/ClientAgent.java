package net.digitalid.core.agent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.concurrent.ConcurrentHashMap;
import net.digitalid.utility.collections.concurrent.ConcurrentMap;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.core.Database;
import net.digitalid.database.core.annotations.Committing;
import net.digitalid.database.core.annotations.NonCommitting;

import net.digitalid.core.client.Commitment;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.identity.Identity;
import net.digitalid.core.synchronizer.Synchronizer;

import net.digitalid.service.core.annotations.OnlyForActions;
import net.digitalid.service.core.concept.Aspect;
import net.digitalid.service.core.concept.Instance;
import net.digitalid.service.core.concept.Observer;

/**
 * This class models a client agent that acts on behalf of an {@link Identity identity}.
 */
@Immutable
public final class ClientAgent extends Agent {
    
    
    /* -------------------------------------------------- Aspects -------------------------------------------------- */
    
    /**
     * Stores the aspect of the commitment being changed at the observed client agent.
     */
    public static final @Nonnull Aspect COMMITMENT = new Aspect(ClientAgent.class, "commitment changed");
    
    /**
     * Stores the aspect of the name being changed at the observed client agent.
     */
    public static final @Nonnull Aspect NAME = new Aspect(ClientAgent.class, "name changed");
    
    
    /* -------------------------------------------------- Commitment -------------------------------------------------- */
    
    /**
     * Stores the commitment of this client agent.
     */
    private @Nullable Commitment commitment;
    
    /**
     * Returns the commitment of this client agent.
     * 
     * @return the commitment of this client agent.
     */
    @NonCommitting
    public @Nonnull Commitment getCommitment() throws DatabaseException {
        if (commitment == null) { commitment = AgentModule.getCommitment(this); }
        return commitment;
    }
    
    /**
     * Sets the commitment of this client agent to the given commitment.
     * 
     * @param newCommitment the new commitment of this client agent.
     * 
     * @require isOnClient() : "This client agent is on a client.";
     */
    @Committing
    public void setCommitment(@Nonnull Commitment newCommitment) throws DatabaseException {
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
    @NonCommitting
    @OnlyForActions
    @SuppressWarnings("deprecation")
    public void replaceCommitment(@Nonnull Commitment oldCommitment, @Nonnull Commitment newCommitment) throws DatabaseException {
        AgentModule.replaceCommitment(this, oldCommitment, newCommitment);
        commitment = newCommitment;
        notify(COMMITMENT);
    }
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
//    private static final @Nonnull NonNullableReplaceableConceptPropertyTable<String> nameTable = NonNullableReplaceableConceptPropertyTable<String>.get();
    
    // TODO: Rename and remove the rest.
//    private @Nonnull NonNullableReplaceableConceptProperty<String, ClientAgent> nameProperty = NonNullableReplaceableConceptProperty<String, ClientAgent>.get(agentModule, this, "name", "replace.name.client.agent@core.digitalid.net", validator);
    
    /**
     * Stores the name of this client agent.
     * 
     * @invariant Client.isValid(name) : "The name is valid.";
     */
    private @Nullable String name;
    
    /**
     * Returns the name of this client agent.
     * 
     * @return the name of this client agent.
     * 
     * @ensure Client.isValid(return) : "The returned name is valid.";
     */
    @NonCommitting
    public @Nonnull String getName() throws DatabaseException {
        if (name == null) { name = AgentModule.getName(this); }
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
    @Committing
    public void setName(@Nonnull String newName) throws DatabaseException {
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
    @NonCommitting
    @OnlyForActions
    public void replaceName(@Nonnull String oldName, @Nonnull String newName) throws DatabaseException {
        AgentModule.replaceName(this, oldName, newName);
        name = newName;
        notify(NAME);
    }
    
    /* -------------------------------------------------- Agent -------------------------------------------------- */
    
    @Override
    public void reset() throws DatabaseException {
        this.commitment = null;
        this.name = null;
        super.reset();
    }
    
    @Pure
    @Override
    public boolean isClient() {
        return true;
    }
    
    /* -------------------------------------------------- Creation -------------------------------------------------- */
    
    /**
     * Creates this client agent in the database.
     * 
     * @param permissions the permissions of the client agent.
     * @param restrictions the restrictions of the client agent.
     * @param commitment the commitment of the client agent.
     * @param name the name of the given client agent.
     * 
     * @require permissions.isFrozen() : "The permissions are frozen.";
     * @require Client.isValid(name) : "The name is valid.";
     */
    @NonCommitting
    @OnlyForActions
    public void createForActions(@Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nonnull Commitment commitment, @Nonnull String name) throws DatabaseException {
        AgentModule.addClientAgent(this, permissions, restrictions, commitment, name);
        this.permissions = permissions.clone();
        this.restrictions = restrictions;
        this.commitment = commitment;
        this.name = name;
        notify(Agent.CREATED);
    }
    
    /* -------------------------------------------------- Indexing -------------------------------------------------- */
    
    /**
     * Caches client agents given their entity and number.
     */
    private static final @Nonnull ConcurrentMap<NonHostEntity, ConcurrentMap<Long, ClientAgent>> index = new ConcurrentHashMap<>();
    
    static {
        if (Database.isSingleAccess()) {
            Instance.observeAspects(new Observer() {
                @Override public void notify(@Nonnull Aspect aspect, @Nonnull Instance instance) { index.remove(instance); }
            }, Entity.DELETED);
        }
    }
    
    /**
     * Resets the client agents of the given entity after having reloaded the agents module.
     * 
     * @param entity the entity whose client agents are to be reset.
     */
    public static void reset(@Nonnull NonHostEntity entity) throws DatabaseException {
        if (Database.isSingleAccess()) {
            final @Nullable ConcurrentMap<Long, ClientAgent> map = index.get(entity);
            if (map != null) {
                for (final @Nonnull ClientAgent clientAgent : map.values()) { clientAgent.reset(); }
            }
        }
    }
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    /**
     * Creates a new client agent with the given entity and number.
     * 
     * @param entity the entity to which this client agent belongs.
     * @param number the number that references this client agent.
     * @param removed whether this client agent has been removed.
     */
    ClientAgent(@Nonnull NonHostEntity entity, long number, boolean removed) {
        super(entity, number, removed);
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
            if (map == null) { map = index.putIfAbsentElseReturnPresent(entity, new ConcurrentHashMap<Long, ClientAgent>()); }
            @Nullable ClientAgent clientAgent = map.get(number);
            if (clientAgent == null) { clientAgent = map.putIfAbsentElseReturnPresent(number, new ClientAgent(entity, number, removed)); }
            return clientAgent;
        } else {
            return new ClientAgent(entity, number, removed);
        }
    }
    
    /* -------------------------------------------------- SQLizable -------------------------------------------------- */
    
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
    @NonCommitting
    public static @Nonnull ClientAgent get(@Nonnull NonHostEntity entity, @NonCapturable @Nonnull SelectionResult result, boolean removed) throws DatabaseException {
        return get(entity, resultSet.getLong(columnIndex), removed);
    }
    
}