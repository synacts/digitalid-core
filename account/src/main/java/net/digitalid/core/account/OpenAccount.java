package net.digitalid.core.account;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.contracts.exceptions.PreconditionExceptionBuilder;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.storage.Storage;
import net.digitalid.utility.validation.annotations.equality.Unequal;
import net.digitalid.utility.validation.annotations.generation.NonRepresentative;
import net.digitalid.utility.validation.annotations.generation.Provided;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.string.CodeIdentifier;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Invariant;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.annotations.type.Loaded;
import net.digitalid.core.client.Client;
import net.digitalid.core.client.role.NativeRole;
import net.digitalid.core.client.role.Role;
import net.digitalid.core.client.role.RoleArguments;
import net.digitalid.core.client.role.RoleArgumentsBuilder;
import net.digitalid.core.client.role.RoleModule;
import net.digitalid.core.clientagent.ClientAgent;
import net.digitalid.core.commitment.Commitment;
import net.digitalid.core.compression.Compression;
import net.digitalid.core.compression.CompressionConverterBuilder;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.entity.factories.AccountFactory;
import net.digitalid.core.entity.factories.HostFactory;
import net.digitalid.core.exceptions.request.RequestErrorCode;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.exceptions.request.RequestExceptionBuilder;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.handler.annotations.Matching;
import net.digitalid.core.handler.annotations.MethodHasBeenReceived;
import net.digitalid.core.handler.method.CoreMethod;
import net.digitalid.core.handler.method.Method;
import net.digitalid.core.handler.method.action.Action;
import net.digitalid.core.handler.method.action.InternalAction;
import net.digitalid.core.handler.reply.ActionReply;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;
import net.digitalid.core.identification.identity.Category;
import net.digitalid.core.identification.identity.IdentifierResolver;
import net.digitalid.core.identification.identity.InternalNonHostIdentity;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.node.context.Context;
import net.digitalid.core.pack.Pack;
import net.digitalid.core.pack.PackConverter;
import net.digitalid.core.permissions.FreezableAgentPermissions;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.property.map.WritableSynchronizedMapProperty;
import net.digitalid.core.property.value.WritableSynchronizedValueProperty;
import net.digitalid.core.restrictions.Restrictions;
import net.digitalid.core.restrictions.RestrictionsBuilder;
import net.digitalid.core.service.CoreService;
import net.digitalid.core.signature.Signature;
import net.digitalid.core.signature.client.ClientSignature;
import net.digitalid.core.signature.client.ClientSignatureCreator;
import net.digitalid.core.unit.annotations.OnClientRecipient;
import net.digitalid.core.unit.annotations.OnHost;
import net.digitalid.core.unit.annotations.OnHostRecipient;

/**
 * Opens a new account with the given category and client.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class OpenAccount extends InternalAction implements CoreMethod<NonHostEntity> {
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    /**
     * Stores the semantic type of this action.
     */
    public static final @Nonnull @Loaded SemanticType TYPE = SemanticType.map(OpenAccountConverter.INSTANCE);
    
    /* -------------------------------------------------- Entity -------------------------------------------------- */
    
    @Pure
    @Override
    @Provided
    public abstract @Nullable Entity getProvidedEntity();
    
    /**
     * Returns null, which is a violation of the postcondition of {@link Method#getEntity()}.
     */
    @Pure
    @Override
    public @Nullable NonHostEntity getEntity() {
        return null;
    }
    
    /**
     * Returns true whenever this method has been received in order to fulfill the invariant of {@link Method}.
     */
    @Pure
    @Override
    public boolean isOnHost() {
        return hasBeenReceived();
    }
    
    /* -------------------------------------------------- Subject -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nullable InternalIdentifier getProvidedSubject() {
        return null;
    }
    
    @Pure
    @Override
    public abstract @Nonnull InternalIdentifier getSubject();
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    /**
     * Returns the category of the new account.
     */
    @Pure
    public abstract @Nonnull @Invariant(condition = "#.isInternalNonHostIdentity()", message = "The category does not denote an internal non-host identity.") Category getCategory();
    
    /**
     * Returns the key of the client agent.
     */
    @Pure
    public abstract long getClientAgentKey();
    
    /**
     * Returns the name of the client agent.
     */
    @Pure
    public abstract @Nonnull @CodeIdentifier @MaxSize(63) @Unequal("general") String getName();
    
    /* -------------------------------------------------- Provided -------------------------------------------------- */
    
    /**
     * Returns the commitment that was provided with the builder.
     */
    @Pure
    @NonRepresentative
    public abstract @Nullable Commitment getProvidedCommitment();
    
    /**
     * Returns the commitment of the client agent.
     */
    @Pure
    @TODO(author = Author.STEPHANIE_STROKA, task = "Use secret commitment instead of adding the secret to the commitment again in the getSignature() method", date = "2017-08-14")
    public @Nonnull Commitment getCommitment() {
        final @Nullable Signature<?> signature = getSignature();
        if (signature instanceof ClientSignature) {
            return ((ClientSignature<?>) signature).getCommitment();
        } else {
            final @Nullable Commitment commitment = getProvidedCommitment();
            if (commitment == null) { throw PreconditionExceptionBuilder.withMessage("The commitment has to be provided if this action has not been received.").build(); }
            return commitment;
        }
    }
    
    /**
     * Returns the secret of the client agent on the client or null on the host.
     */
    @Pure
    @NonRepresentative
    public abstract @Nullable Exponent getSecret();
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Returns a new action to open an account for the given 
     */
    @Pure
    @NonCommitting
    public static @Nonnull NativeRole of(@Nonnull Category category, @Nonnull InternalNonHostIdentifier subject, @Nonnull Client client) throws ExternalException {
        final @Nonnull OpenAccount openAccount = OpenAccountBuilder.withSubject(subject).withCategory(category).withClientAgentKey(0).withName(client.getName()).withProvidedCommitment(client.getCommitment(subject)).withSecret(client.secret.get()).build();
        openAccount.send();

        final @Nonnull InternalNonHostIdentity identity = subject.resolve();
        // The following tests whether the client can be assigned a native role.
        final @Nonnull RoleArguments arguments = RoleArgumentsBuilder.withClient(client).withIssuer(identity).withAgentKey(0).build();
        final @Nonnull Role role = RoleModule.map(arguments);
        openAccount.initialize(role);
        
        return NativeRole.with(client, role.getKey()); // The role has to be reloaded because the client agent did not exist before the initialization.
    }
    
    /* -------------------------------------------------- Execution -------------------------------------------------- */
    
    @Override
    @NonCommitting
    @PureWithSideEffects
    protected void executeOnBoth() throws DatabaseException, RecoveryException {
        throw PreconditionExceptionBuilder.withMessage("The action to open an account should never be executed on a client.").build();
    }
    
    /**
     * Creates the root context and the client agent for the given entity.
     */
    @NonCommitting
    @PureWithSideEffects
    @SuppressWarnings("unchecked")
    public void initialize(@Nonnull NonHostEntity entity) throws DatabaseException, RecoveryException {
        final @Nonnull Context context = Context.of(entity);
        ((WritableSynchronizedValueProperty<NonHostEntity, Long, Context, String>) context.name()).setWithoutSynchronization("Root Context");
        
        final @Nonnull ClientAgent clientAgent = ClientAgent.of(entity, getClientAgentKey());
        final @Nonnull Restrictions restrictions = RestrictionsBuilder.withOnlyForClients(true).withAssumeRoles(true).withWriteToNode(true).withNode(context).build();
        ((WritableSynchronizedMapProperty<NonHostEntity, Long, Agent, SemanticType, Boolean, ReadOnlyAgentPermissions, FreezableAgentPermissions>) clientAgent.permissions()).addWithoutSynchronization(FreezableAgentPermissions.GENERAL, Boolean.TRUE);
        ((WritableSynchronizedValueProperty<NonHostEntity, Long, Agent, Restrictions>) clientAgent.restrictions()).setWithoutSynchronization(restrictions);
        ((WritableSynchronizedValueProperty<NonHostEntity, Long, ClientAgent, Commitment>) clientAgent.commitment()).setWithoutSynchronization(getCommitment());
        ((WritableSynchronizedValueProperty<NonHostEntity, Long, ClientAgent, String>) clientAgent.name()).setWithoutSynchronization(getName());
    }
    
    @Override
    @NonCommitting
    @OnHostRecipient
    @PureWithSideEffects
    @MethodHasBeenReceived
    public @Nullable @Matching ActionReply executeOnHost() throws RequestException, DatabaseException, RecoveryException {
        if (IdentifierResolver.configuration.get().load(getSubject()) != null) { throw RequestExceptionBuilder.withCode(RequestErrorCode.IDENTITY).withMessage("An account with the identifier " + getSubject() + " already exists.").build(); }
        
        // TODO: Include the restriction mechanisms like the tokens.
        
        final @Nonnull InternalNonHostIdentity identity = (InternalNonHostIdentity) IdentifierResolver.configuration.get().map(getCategory(), getSubject());
        final @Nonnull @OnHost Entity entity = AccountFactory.create(HostFactory.create(getSubject().getHostIdentifier()), identity);
        initialize((NonHostEntity) entity);
        return null;
    }
    
    /* -------------------------------------------------- Similarity -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean isSimilarTo(@Nonnull Method<?> other) {
        return false;
    }
    
    /* -------------------------------------------------- Sending -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Signature<Compression<Pack>> getSignature(@Nonnull Compression<Pack> compression) throws ExternalException {
        return ClientSignatureCreator.sign(compression, CompressionConverterBuilder.withObjectConverter(PackConverter.INSTANCE).build()).to(getSubject()).with(getCommitment().addSecret(getSecret()));
    }
    
    /* -------------------------------------------------- Auditable -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToSeeMethod() {
        return ReadOnlyAgentPermissions.GENERAL_WRITE;
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictionsToSeeMethod() {
        return Restrictions.MAX;
    }
    
    /* -------------------------------------------------- Storage -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Storage getStorage() {
        return CoreService.INSTANCE.getModule();
    }
    
    /* -------------------------------------------------- Reversion -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean interferesWith(@Nonnull Action action) {
        return false;
    }
    
    @Pure
    @Override
    @OnClientRecipient
    public @Nullable InternalAction getReverse() {
        return null;
    }
    
}
