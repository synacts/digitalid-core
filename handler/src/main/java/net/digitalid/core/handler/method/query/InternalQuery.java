package net.digitalid.core.handler.method.query;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.exceptions.request.RequestErrorCode;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.exceptions.request.RequestExceptionBuilder;
import net.digitalid.core.handler.annotations.Matching;
import net.digitalid.core.handler.annotations.MethodHasBeenReceived;
import net.digitalid.core.handler.method.InternalMethod;
import net.digitalid.core.handler.method.Method;
import net.digitalid.core.handler.reply.QueryReply;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.restrictions.Restrictions;
import net.digitalid.core.service.CoreService;
import net.digitalid.core.signature.Signature;
import net.digitalid.core.signature.credentials.CredentialsSignature;
import net.digitalid.core.unit.annotations.OnHostRecipient;

/**
 * Internal queries can only be sent by clients and are always signed identity-based.
 */
@Immutable
public abstract class InternalQuery extends Query<NonHostEntity<?>> implements InternalMethod {
    
    /* -------------------------------------------------- Similarity -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean isSimilarTo(@Nonnull Method<?> other) {
        return super.isSimilarTo(other) && other instanceof InternalQuery;
    }
    
    /* -------------------------------------------------- Execution -------------------------------------------------- */
    
    /**
     * Executes this internal query on the host.
     * 
     * @return the reply to this internal query.
     */
    @NonCommitting
    @OnHostRecipient
    @PureWithSideEffects
    @MethodHasBeenReceived
    @TODO(task = "Also pass the present agent as an argument so that it does not have to be recreated again?", date = "2016-11-09", author = Author.KASPAR_ETTER)
    protected abstract @Nonnull @Matching QueryReply<NonHostEntity<?>> execute() throws DatabaseException;
    
    @Override
    @NonCommitting
    @OnHostRecipient
    @PureWithSideEffects
    @MethodHasBeenReceived
    public @Nonnull @Matching QueryReply<NonHostEntity<?>> executeOnHost() throws RequestException, DatabaseException, RecoveryException {
        Require.that(hasBeenReceived()).orThrow("This internal query can only be executed if it has been received.");
        
        final @Nullable Signature<?> signature = getSignature();
        
        final @Nonnull ReadOnlyAgentPermissions presentPermissions;
        final @Nonnull Restrictions presentRestrictions;
        
        if (getService() == CoreService.INSTANCE) {
            final @Nonnull Agent presentAgent = null; // TODO: = signature.getAgentCheckedAndRestricted(getEntity());
            presentPermissions = presentAgent.permissions().get();
            presentRestrictions = presentAgent.restrictions().get();
        } else {
            if (!(signature instanceof CredentialsSignature<?>)) { throw RequestExceptionBuilder.withCode(RequestErrorCode.SIGNATURE).withMessage("Internal queries of a non-core service have to be signed with credentials.").build(); }
            final @Nonnull CredentialsSignature<?> credentialsSignature = (CredentialsSignature<?>) signature;
            presentPermissions = null; // TODO: Get the permissions from the credentials signature.
            presentRestrictions = null; // TODO: Get the restrictions from the credentials signature.
        }
        
        final @Nonnull ReadOnlyAgentPermissions requiredPermissions = getRequiredPermissionsToExecuteMethod();
        if (!requiredPermissions.equals(ReadOnlyAgentPermissions.NONE)) { presentPermissions.checkCover(requiredPermissions); }
        
        final @Nonnull Restrictions requiredRestrictions = getRequiredRestrictionsToExecuteMethod();
        if (!requiredRestrictions.equals(Restrictions.MIN)) { presentRestrictions.checkCover(requiredRestrictions); }
        
        return execute();
    }
    
}
