package net.digitalid.core.agent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.collections.list.FreezableList;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.generator.annotations.generators.GenerateTableConverter;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.generation.NonRepresentative;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.property.map.WritablePersistentMapProperty;
import net.digitalid.database.property.value.WritablePersistentValueProperty;

import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.exceptions.request.RequestErrorCode;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.exceptions.request.RequestExceptionBuilder;
import net.digitalid.core.identification.identity.Identity;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.permissions.FreezableAgentPermissions;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.restrictions.Node;
import net.digitalid.core.restrictions.Restrictions;
import net.digitalid.core.subject.CoreServiceCoreSubject;
import net.digitalid.core.subject.annotations.GenerateSynchronizedProperty;

/**
 * This class models an agent that acts on behalf of an {@link Identity identity}.
 */
@Immutable
@GenerateTableConverter(table = "unit_core_Agent_Agent") // TODO: How can we get the table name without adding the generated attribute table converter to the attribute core subject module?
@TODO(task = "How can we observe when a new agent is added? Do we need an extensible property besides the index?", date = "2017-08-17", author = Author.KASPAR_ETTER)
public abstract class Agent extends CoreServiceCoreSubject<NonHostEntity, Long> {
    
    /* -------------------------------------------------- Removed -------------------------------------------------- */
    
    /**
     * Returns whether this agent is removed.
     */
    @Pure
    @Default("true")
    @GenerateSynchronizedProperty
    public abstract @Nonnull WritablePersistentValueProperty<Agent, @Nonnull Boolean> removed();
    
    /**
     * Checks that this agent is not removed and throws a {@link RequestException} otherwise.
     */
    @Pure
    public void checkNotRemoved() throws RequestException, DatabaseException, RecoveryException {
        if (removed().get()) { throw RequestExceptionBuilder.withCode(RequestErrorCode.AUTHORIZATION).withMessage("The agent has been removed.").build(); }
    }
    
    /* -------------------------------------------------- Permissions -------------------------------------------------- */
    
    /**
     * Returns the permissions of this agent.
     * <p>
     * <em>Important:</em> The additional permissions should not cover any existing permissions. If they do,
     * make sure to {@link WritablePersistentMapProperty#remove(java.lang.Object) remove} them first.
     */
    @Pure
    @GenerateSynchronizedProperty
    @Default("FreezableAgentPermissions.withNoPermissions()")
    public abstract @Nonnull WritablePersistentMapProperty<Agent, SemanticType, Boolean, ReadOnlyAgentPermissions, FreezableAgentPermissions> permissions();
    
    /* -------------------------------------------------- Subtypes -------------------------------------------------- */
    
    /**
     * Returns whether this agent is a client agent.
     */
    @Pure
    @NonRepresentative
    public abstract boolean isClientAgent();
    
    /**
     * Returns whether this agent is an outgoing role.
     */
    @Pure
    public final boolean isOutgoingRole() {
        return !isClientAgent();
    }
    
    /* -------------------------------------------------- Matching -------------------------------------------------- */
    
    /**
     * Returns whether this agent matches the given restrictions.
     */
    @Pure
    public boolean matches(@Nonnull Restrictions restrictions) {
        final @Nullable Node node = restrictions.getNode();
        return isClientAgent() == restrictions.isOnlyForClients() && (node == null || getEntity().equals(node.getEntity()));
    }
    
    /* -------------------------------------------------- Restrictions -------------------------------------------------- */
    
    /**
     * Returns the restrictions of this agent.
     */
    @Pure
    @GenerateSynchronizedProperty
    @Default("net.digitalid.core.restrictions.Restrictions.MIN")
    @TODO(task = "Check that the entity of the restrictions match the entity of this subject by providing a value validator.", date = "2017-08-18", author = Author.KASPAR_ETTER)
    public abstract @Nonnull WritablePersistentValueProperty<Agent, @Nonnull Restrictions> restrictions();
    
    /* -------------------------------------------------- Weaker Agents -------------------------------------------------- */
    
    /**
     * Returns the agents that are weaker than this agent.
     */
    @Pure
    @NonCommitting
    public abstract @Capturable @Nonnull @NonFrozen FreezableList<@Nonnull Agent> getWeakerAgents() throws DatabaseException;
    
    /**
     * Returns the weaker agent with the given agent number.
     * 
     * @throws DatabaseException if no such weaker agent is found.
     */
    @Pure
    @NonCommitting
    public abstract @Nonnull Agent getWeakerAgent(long agentNumber) throws DatabaseException;
    
    /* -------------------------------------------------- Coverage -------------------------------------------------- */
    
    /**
     * Returns whether this agent covers the given agent.
     * 
     * @require getEntity().equals(agent.getEntity()) : "The given agent belongs to the same entity.";
     */
    @Pure
    @NonCommitting
    public abstract boolean covers(@Nonnull /* @Matching */ Agent agent) throws DatabaseException, RecoveryException; /* {
        return !removed().get() && AgentModule.isStronger(this, agent);
    } */
    
    /**
     * Checks that this agent covers the given agent and throws a {@link RequestException} otherwise.
     */
    @Pure
    @NonCommitting
    public void checkCovers(@Nonnull Agent agent) throws RequestException, DatabaseException, RecoveryException {
        if (!covers(agent)) { throw RequestExceptionBuilder.withCode(RequestErrorCode.AUTHORIZATION).withMessage(Strings.format("The agent $ does not cover the agent $.", this, agent)).build(); }
    }
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    /**
     * Returns the agent with the given key at the given entity.
     */
    @Pure
    @Recover
    @NonCommitting
    public static @Nonnull Agent of(@Nonnull NonHostEntity entity, long key) throws DatabaseException {
        return AgentFactory.configuration.get().getAgent(entity, key);
    }
    
}
