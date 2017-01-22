package net.digitalid.core.clientagent;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.property.value.WritablePersistentValueProperty;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.commitment.Commitment;
import net.digitalid.core.subject.annotations.GenerateSynchronizedProperty;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.identification.identity.Identity;
import net.digitalid.core.property.value.ValuePropertyRequiredAuthorization;
import net.digitalid.core.property.value.ValuePropertyRequiredAuthorizationBuilder;

/**
 * This class models a client agent that acts on behalf of an {@link Identity identity}.
 */
@Immutable
// TODO: @GenerateSubclass
@GenerateConverter
public abstract class ClientAgent extends Agent {
    
    /* -------------------------------------------------- Commitment -------------------------------------------------- */
    
    /**
     * Stores the required authorization to change the commitment.
     */
    static final @Nonnull ValuePropertyRequiredAuthorization<NonHostEntity<?>, Long, ClientAgent, Commitment> COMMITMENT = ValuePropertyRequiredAuthorizationBuilder.<NonHostEntity<?>, Long, ClientAgent, Commitment>withRequiredAgentToExecuteMethod((concept, value) -> concept).withRequiredAgentToSeeMethod((concept, value) -> concept).build();
    
    /**
     * Returns the commitment of this client agent.
     */
    @Pure
    @GenerateSynchronizedProperty
    public abstract @Nonnull WritablePersistentValueProperty<ClientAgent, @Nonnull Commitment> commitment();
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    /**
     * Stores the required authorization to change the name.
     */
    static final @Nonnull ValuePropertyRequiredAuthorization<NonHostEntity<?>, Long, ClientAgent, String> NAME_AUTHORIZATION = ValuePropertyRequiredAuthorizationBuilder.<NonHostEntity<?>, Long, ClientAgent, String>withRequiredAgentToExecuteMethod((concept, value) -> concept).withRequiredAgentToSeeMethod((concept, value) -> concept).build();
    
    /**
     * Returns the name of this client agent.
     */
    @Pure
    @GenerateSynchronizedProperty
    public abstract @Nonnull WritablePersistentValueProperty<ClientAgent, @Nonnull @MaxSize(50) String> name();
    
    /* -------------------------------------------------- Creation -------------------------------------------------- */
    
    // TODO: Think about how to create concepts (and synchronize this across sites).
    
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
//    @NonCommitting
//    @OnlyForActions
//    public void createForActions(@Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nonnull Commitment commitment, @Nonnull String name) throws DatabaseException {
//        AgentModule.addClientAgent(this, permissions, restrictions, commitment, name);
//        this.permissions = permissions.clone();
//        this.restrictions = restrictions;
//        this.commitment = commitment;
//        this.name = name;
//        notify(Agent.CREATED);
//    }
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    /**
     * Returns the potentially cached client agent of the given entity that might not yet exist in the database.
     */
    @Pure
    @Recover
    public static @Nonnull ClientAgent of(@Nonnull NonHostEntity<?> entity, long key) {
        return null;
//        return ClientAgentSubclass.MODULE.getConceptIndex().get(entity, key); // TODO
    }
    
}
