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
 * External actions can be sent by both hosts and clients.
 * Depending on whether the reply is needed immediately, external actions can be either sent directly or passed to the pusher!
 * 
 * @invariant hasEntity() : "This external action has an entity.";
 */
@Immutable
public abstract class ExternalAction extends Action {
    
    /* -------------------------------------------------- Similarity -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean isSimilarTo(@Nonnull Method<?> other) {
        return super.isSimilarTo(other) && other instanceof ExternalAction;
    }
    
    /* -------------------------------------------------- Requirements -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Execution -------------------------------------------------- */
    
    /**
     * This method is executed after successful transmission.
     */
    @NonCommitting
    @PureWithSideEffects
    public void executeOnSuccess() throws DatabaseException {}
    
    /**
     * This method is executed if an error occurred during pushing.
     */
    @NonCommitting
    @PureWithSideEffects
    public abstract void executeOnFailure() throws DatabaseException;
    
    /* -------------------------------------------------- Audit on Failure -------------------------------------------------- */
    
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
