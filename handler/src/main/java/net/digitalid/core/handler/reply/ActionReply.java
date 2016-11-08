package net.digitalid.core.handler.reply;

import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.generation.Provided;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.entity.annotations.OnClientRecipient;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.handler.Auditable;
import net.digitalid.core.handler.method.action.ExternalAction;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.restrictions.Restrictions;

/**
 * This class models a {@link Reply reply} to an {@link ExternalAction external action}.
 * Action replies are added to the {@link Audit audit} by the {@link Pusher pusher} on {@link Service services}.
 */
@Immutable
public abstract class ActionReply extends Reply<NonHostEntity> implements Auditable {
    
    /* -------------------------------------------------- Entity -------------------------------------------------- */
    
    @Pure
    @Override
    @Provided
    @Default("signature == null ? null : null /* Find a way to derive it from signature.getSubject(), probably make it injectable. */")
    public abstract @Nonnull NonHostEntity getEntity();
    
    // TODO: Use an @Derive on getProvidedSubject to derive the provided subject?
    
//    /**
//     * Creates an action reply that encodes the content of a packet.
//     * 
//     * @param account the account to which this action reply belongs.
//     */
//    protected ActionReply(@Nonnull Account account) {
//        super(account, account.getIdentity().getAddress());
//    }
    
    /* -------------------------------------------------- Execution -------------------------------------------------- */
    
    /**
     * Executes this action reply by the pusher.
     * 
     * @param action the external action that was sent.
     * 
     * @throws RequestException if the authorization is not sufficient.
     * 
     * @require hasSignature() : "This handler has a signature.";
     * @require action.getReplyClass().isInstance(this) : "This object is an instance of the action's reply class.";
     * @require getSubject().equals(action.getSubject()) : "The subjects of the reply and the action are the same.";
     * @require getEntityNotNull().equals(action.getEntityNotNull()) : "The entities of the reply and the action are the same.";
     * @require ((HostSignatureWrapper) getSignatureNotNull()).getSigner().equals(action.getRecipient()) : "The reply is signed by the action's recipient.";
     */
    @NonCommitting
    @PureWithSideEffects
    public abstract void executeByPusher(@Nonnull ExternalAction action) throws RequestException, SQLException;
    
    /**
     * Executes this action reply by the synchronizer.
     * 
     * @throws DatabaseException if this handler cannot be executed.
     */
    @NonCommitting
    @OnClientRecipient
    @PureWithSideEffects
    public abstract void executeBySynchronizer() throws DatabaseException;
    
    /* -------------------------------------------------- Audit Requirements -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToSeeAudit() {
        return ReadOnlyAgentPermissions.NONE;
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictionsToSeeAudit() {
        return Restrictions.MIN;
    }
    
    @Pure
    @Override
    public @Nullable Agent getRequiredAgentToSeeAudit() {
        return null;
    }
    
}
