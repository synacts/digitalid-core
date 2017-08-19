package net.digitalid.core.clientagent;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.collaboration.enumerations.Priority;
import net.digitalid.utility.collections.list.FreezableLinkedList;
import net.digitalid.utility.collections.list.FreezableList;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.generator.annotations.generators.GenerateTableConverter;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.logging.Log;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.math.modulo.Even;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.conversion.SQL;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.interfaces.Database;
import net.digitalid.database.property.value.WritablePersistentValueProperty;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.agent.AgentFactory;
import net.digitalid.core.commitment.Commitment;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.identification.identity.Identity;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.outgoingrole.OutgoingRole;
import net.digitalid.core.permissions.FreezableAgentPermissions;
import net.digitalid.core.property.RequiredAuthorization;
import net.digitalid.core.property.RequiredAuthorizationBuilder;
import net.digitalid.core.restrictions.Restrictions;
import net.digitalid.core.subject.annotations.GenerateSynchronizedProperty;

/**
 * This class models a client agent that acts on behalf of an {@link Identity identity}.
 */
@Immutable
@GenerateSubclass
@GenerateTableConverter(table = "unit_core_ClientAgent_ClientAgent") // TODO: How can we get the table name without adding the generated attribute table converter to the attribute core subject module?
public abstract class ClientAgent extends Agent {
    
    /* -------------------------------------------------- Key -------------------------------------------------- */
    
    @Pure
    @Override
    public abstract @Nonnull @Even Long getKey();
    
    /* -------------------------------------------------- Removed -------------------------------------------------- */
    
    /**
     * Stores the required authorization to change whether an agent is removed.
     */
    static final @Nonnull RequiredAuthorization<NonHostEntity, Long, Agent, @Nonnull Boolean> REMOVED = RequiredAuthorizationBuilder.<NonHostEntity, Long, Agent, Boolean>withRequiredAgentToExecuteMethod((agent, removed) -> agent).withRequiredAgentToSeeMethod((agent, removed) -> agent).build();
    
    /* -------------------------------------------------- Permissions -------------------------------------------------- */
    
    /**
     * Stores the required authorization to change the permissions of an agent.
     */
    static final @Nonnull RequiredAuthorization<NonHostEntity, Long, Agent, @Nonnull SemanticType> PERMISSIONS = RequiredAuthorizationBuilder.<NonHostEntity, Long, Agent, SemanticType>withRequiredPermissionsToExecuteMethod((agent, permissionKey) -> FreezableAgentPermissions.withPermission(permissionKey, Boolean.TRUE /* TODO: permissionValue */).freeze()).withRequiredAgentToExecuteMethod((agent, permissionKey) -> agent).withRequiredPermissionsToSeeMethod((agent, permissionKey) -> FreezableAgentPermissions.withPermission(permissionKey, Boolean.TRUE /* TODO: permissionValue */).freeze()).withRequiredAgentToSeeMethod((agent, permissionKey) -> agent).build();
    
    /* -------------------------------------------------- Restrictions -------------------------------------------------- */
    
    /**
     * Stores the required authorization to change the restrictions of an agent.
     */
    static final @Nonnull RequiredAuthorization<NonHostEntity, Long, Agent, @Nonnull Restrictions> RESTRICTIONS = RequiredAuthorizationBuilder.<NonHostEntity, Long, Agent, Restrictions>withRequiredRestrictionsToExecuteMethod((agent, restrictions) -> restrictions).withRequiredAgentToExecuteMethod((agent, restrictions) -> agent).withRequiredRestrictionsToSeeMethod((agent, restrictions) -> restrictions).withRequiredAgentToSeeMethod((agent, restrictions) -> agent).build();
    
    /* -------------------------------------------------- Commitment -------------------------------------------------- */
    
    /**
     * Stores the required authorization to change the commitment.
     */
    static final @Nonnull RequiredAuthorization<NonHostEntity, Long, ClientAgent, @Nonnull Commitment> COMMITMENT = RequiredAuthorizationBuilder.<NonHostEntity, Long, ClientAgent, Commitment>withRequiredAgentToExecuteMethod((agent, commitment) -> agent).withRequiredAgentToSeeMethod((agent, commitment) -> agent).build();
    
    /**
     * Returns the commitment of this client agent.
     */
    @Pure
    @GenerateSynchronizedProperty
    @TODO(task = "Make sure that the commitment can also be replaced by the affected client agent if the host rotates its keys.", date = "2017-08-18", author = Author.KASPAR_ETTER, priority = Priority.LOW)
    public abstract @Nonnull WritablePersistentValueProperty<ClientAgent, @Nonnull Commitment> commitment();
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    /**
     * Stores the required authorization to change the name.
     */
    static final @Nonnull RequiredAuthorization<NonHostEntity, Long, ClientAgent, @Nonnull String> NAME = RequiredAuthorizationBuilder.<NonHostEntity, Long, ClientAgent, String>withRequiredAgentToExecuteMethod((agent, name) -> agent).withRequiredAgentToSeeMethod((agent, name) -> agent).build();
    
    /**
     * Returns the name of this client agent.
     */
    @Pure
    @Default("\"Client Name\"")
    @GenerateSynchronizedProperty
    @TODO(task = "Make sure that the validity is checked.", date = "2017-08-18", author = Author.KASPAR_ETTER)
    public abstract @Nonnull WritablePersistentValueProperty<ClientAgent, @Nonnull @MaxSize(50) String> name();
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    /**
     * Returns the potentially cached client agent of the given entity that might not yet exist in the database.
     */
    @Pure
    @Recover
    @TODO(task = "The entry in the agent table should rather be created in the core subejct index.", date = "2017-08-19", author = Author.KASPAR_ETTER)
    public static @Nonnull ClientAgent of(@Nonnull NonHostEntity entity, @Even long key) throws DatabaseException {
        Log.information("ClientAgent with key $", key);
        final @Nonnull ClientAgent clientAgent = ClientAgentSubclass.MODULE.getSubjectIndex().get(entity, key);
        SQL.insertOrReplace(ClientAgentSubclass.SUPER_MODULE.getSubjectTable(), clientAgent, clientAgent.getUnit());
        Database.instance.get().commit(); // TODO: Remove again!
        return clientAgent;
    }
    
    /* -------------------------------------------------- Injection -------------------------------------------------- */
    
    /**
     * Initializes the agent factory.
     */
    @PureWithSideEffects
    @Initialize(target = AgentFactory.class)
    public static void initializeAgentFactory() {
        AgentFactory.configuration.set((entity, key) -> key % 2 == 0 ? ClientAgent.of(entity, key) : OutgoingRole.of(entity, key));
    }
    
    /* -------------------------------------------------- Weaker Agents -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    @TODO(task = "Implement the agent hierarchy.", date = "2017-08-18", author = Author.KASPAR_ETTER)
    public @Capturable @Nonnull @NonFrozen FreezableList<@Nonnull Agent> getWeakerAgents() throws DatabaseException {
        return FreezableLinkedList.withNoElements();
    }
    
    @Pure
    @Override
    @NonCommitting
    @TODO(task = "Implement the agent hierarchy.", date = "2017-08-18", author = Author.KASPAR_ETTER)
    public @Nonnull Agent getWeakerAgent(long agentNumber) throws DatabaseException {
        return this;
    }
    
    /* -------------------------------------------------- Coverage -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    @TODO(task = "Check that the given agent belongs to the same entity.", date = "2017-08-18", author = Author.KASPAR_ETTER)
    public boolean covers(@Nonnull /* @Matching */ Agent agent) throws DatabaseException, RecoveryException {
        return !removed().get();// TODO: && AgentModule.isStronger(this, agent);
    }
    
    /* -------------------------------------------------- Subtypes -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean isClientAgent() {
        return true;
    }
    
}
