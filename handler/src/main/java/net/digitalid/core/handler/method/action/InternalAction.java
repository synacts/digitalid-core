package net.digitalid.core.handler.method.action;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.entity.annotations.OnClientRecipient;
import net.digitalid.core.entity.annotations.OnHostRecipient;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.handler.method.InternalMethod;
import net.digitalid.core.handler.method.Method;
import net.digitalid.core.handler.reply.ActionReply;
import net.digitalid.core.handler.reply.Reply;

/**
 * Internal actions can only be sent by {@link Client clients} and can usually be {@link #reverseOnClient() reversed}.
 * They are always signed identity-based and an audit request or trail is appended during {@link Package packaging}.
 * <p>
 * <em>Important:</em> Do not execute internal actions directly but always pass them to the {@link Synchronizer#execute(net.digitalid.service.core.handler.InternalAction) Synchronizer}!
 */
@Immutable
public abstract class InternalAction extends Action implements InternalMethod {
    
    /* -------------------------------------------------- Similarity -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean isSimilarTo(@Nonnull Method<?> other) {
        return super.isSimilarTo(other) && other instanceof InternalAction;
    }
    
    /* -------------------------------------------------- Requirements -------------------------------------------------- */
    
    @Pure
    @Override
    public final boolean canBeSentByHosts() {
        return false;
    }
    
    @Pure
    @Override
    public final boolean canBeSentByClients() {
        return true;
    }
    
    /* -------------------------------------------------- Execution -------------------------------------------------- */
    
    @Pure
    @Override
    public final boolean matches(@Nullable Reply<NonHostEntity> reply) {
        return reply == null;
    }
    
    /**
     * Executes this internal action on the host.
     * 
     * @throws RequestException if the authorization is not sufficient.
     * 
     * @require hasBeenReceived() : "This method has been received.";
     */
    @NonCommitting
    @OnHostRecipient
    @PureWithSideEffects
    protected abstract void executeOnHostInternalAction() throws RequestException, DatabaseException;
    
    @Override
    @NonCommitting
    @OnHostRecipient
    @PureWithSideEffects
    public final @Nullable ActionReply executeOnHost() throws RequestException, DatabaseException {
        executeOnHostInternalAction();
        return null;
    }
    
    /* -------------------------------------------------- Reversion -------------------------------------------------- */
    
    /**
     * Returns whether this internal action interferes with the given action.
     * 
     * @require action.getEntity().equals(getEntity()) : "The entity of the given and this action is the same.";
     * @require action.getService().equals(getService()) : "The service of the given and this action is the same.";
     */
    @Pure
    public abstract boolean interferesWith(@Nonnull Action action);
    
    /**
     * Returns the reverse of this action or null if this action cannot be reversed.
     */
    @Pure
    @OnClientRecipient
    public abstract @Nullable InternalAction getReverse() throws DatabaseException;
    
    /**
     * Reverses this internal action on the client if this action can be reversed.
     */
    @NonCommitting
    @OnClientRecipient
    @PureWithSideEffects
    public void reverseOnClient() throws DatabaseException {
        final @Nullable InternalAction reverse = getReverse();
        if (reverse != null) { reverse.executeOnClient(); }
    }
    
}
