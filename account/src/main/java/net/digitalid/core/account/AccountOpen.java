package net.digitalid.core.account;

import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.contracts.exceptions.PreconditionExceptionBuilder;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.storage.Storage;
import net.digitalid.utility.validation.annotations.equality.Unequal;
import net.digitalid.utility.validation.annotations.generation.NonRepresentative;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.string.CodeIdentifier;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Invariant;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.client.Client;
import net.digitalid.core.commitment.Commitment;
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
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.restrictions.Restrictions;
import net.digitalid.core.service.CoreService;
import net.digitalid.core.signature.Signature;
import net.digitalid.core.signature.client.ClientSignature;
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
public abstract class AccountOpen extends InternalAction implements CoreMethod<NonHostEntity> {
    
    /* -------------------------------------------------- Entity -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nullable NonHostEntity getProvidedEntity() {
        return null;
    }
    
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
    public static @Nonnull AccountOpen with(@Nonnull Category category, @Nonnull InternalNonHostIdentifier subject, @Nonnull Client client) throws ExternalException {
        return AccountOpenBuilder.withSubject(subject).withCategory(category).withClientAgentKey(ThreadLocalRandom.current().nextLong()).withName(client.getName()).withProvidedCommitment(client.getCommitment(subject)).withSecret(client.secret.get()).build();
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
    public void initialize(@Nonnull NonHostEntity entity) throws DatabaseException {
//        final @Nonnull Context context = Context.of(entity);
//        context.createForActions(); // TODO: This should probably become part of the Context.of(entity) method.
//        context.name().set("Root Context"); // TODO: The name should be stored in the database without synchronization.
        
//        final @Nonnull ClientAgent clientAgent = ClientAgent.of(entity, getClientAgentKey());
//        final @Nonnull Restrictions restrictions = RestrictionsBuilder.withOnlyForClients(true).withAssumeRoles(true).withWriteToNode(true).withNode(context).build();
//        clientAgent.createForActions(FreezableAgentPermissions.GENERAL_WRITE, restrictions, getCommitment(), getName()); // TODO: Do this differently.
    }
    
    @Override
    @NonCommitting
    @OnHostRecipient
    @PureWithSideEffects
    @MethodHasBeenReceived
    public @Nullable @Matching ActionReply executeOnHost() throws RequestException, DatabaseException, RecoveryException {
        if (IdentifierResolver.configuration.get().load(getSubject()) != null) { throw RequestExceptionBuilder.withCode(RequestErrorCode.IDENTITY).withMessage("An account with the identifier " + getSubject() + " already exists.").build(); }
        
        // TODO: Include the resctriction mechanisms like the tokens.
        
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
    
    // TODO: Use the commitment and the secret to send this action with a client signature.
    
//    @Override
//    @NonCommitting
//    public @Nonnull Response send() throws ExternalException {
//        final @Nullable Exponent secret = getSecret();
//        if (secret == null) { throw PreconditionExceptionBuilder.withMessage("The secret may not be null for sending.").build(); }
//        return new ClientRequest(getSubject(), getCommitment().addSecret(secret)).send();
//    }
    
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
        return CoreService.MODULE;
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
