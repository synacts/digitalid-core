package net.digitalid.core.handler.method.action;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.validation.annotations.size.EmptyOrSingle;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.handler.method.Method;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.restrictions.Restrictions;

/**
 * External actions can be sent by both {@link Host hosts} and {@link Client clients}.
 * Depending on whether the reply is needed immediately, external actions can be either sent 
 * directly or passed to the {@link Pusher#send(net.digitalid.service.core.handler.ExternalAction) Pusher}!
 * 
 * @invariant hasEntity() : "This external action has an entity.";
 */
@Immutable
public abstract class ExternalAction extends Action {
    
    @Pure
    @Override
    public boolean isSimilarTo(@Nonnull Method<?> other) {
        return super.isSimilarTo(other) && other instanceof ExternalAction;
    }
    
    
    @Pure
    @Override
    public boolean canBeSentByHosts() {
        return false;
    }
    
    @Pure
    @Override
    public boolean canBeSentByClients() {
        return true;
    }
    
    
    /**
     * Executes this action if an error occurred during pushing.
     */
    @NonCommitting
    @PureWithSideEffects
    public abstract void executeOnFailure() throws DatabaseException;
    
    /**
     * Returns the permission that an agent needs to cover in order to see the audit of this external action when the pushing failed.
     */
    @Pure
    public @Nonnull @EmptyOrSingle ReadOnlyAgentPermissions getFailedAuditPermissions() {
        return ReadOnlyAgentPermissions.NONE;
    }
    
    /**
     * Returns the restrictions that an agent needs to cover in order to see the audit of this external action when the pushing failed.
     */
    @Pure
    public @Nonnull Restrictions getFailedAuditRestrictions() {
        return Restrictions.MIN;
    }
    
    /**
     * Returns the agent that an agent needs to cover in order to see the audit of this external action when the pushing failed.
     */
    @Pure
    @NonCommitting
    public @Nullable Agent getFailedAuditAgent() throws DatabaseException {
        return null;
    }
    
}
