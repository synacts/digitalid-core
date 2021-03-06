/*
 * Copyright (C) 2017 Synacts GmbH, Switzerland (info@synacts.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.digitalid.core.handler.method.action;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.collaboration.enumerations.Priority;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.handler.annotations.Matching;
import net.digitalid.core.handler.annotations.MethodHasBeenReceived;
import net.digitalid.core.handler.method.InternalMethod;
import net.digitalid.core.handler.method.Method;
import net.digitalid.core.handler.reply.ActionReply;
import net.digitalid.core.signature.Signature;
import net.digitalid.core.unit.annotations.OnClientRecipient;
import net.digitalid.core.unit.annotations.OnHostRecipient;

/**
 * Internal actions can only be sent by clients and can usually be {@link #reverseOnClient() reversed}.
 * They are always signed identity-based and an audit request or trail is appended during {@link Package packaging}.
 * <p>
 * <em>Important:</em> Do not execute internal actions directly but always pass them to the synchronizer!
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
    
    /**
     * Returns the agent required to execute this internal action.
     */
    @Pure
    public @Nullable Agent getRequiredAgentToExecuteMethod() {
        return null;
    }
    
    /* -------------------------------------------------- Execution -------------------------------------------------- */
    
    /**
     * Executes this internal action on both the host and client.
     */
    @NonCommitting
    @PureWithSideEffects
    protected abstract void executeOnBoth() throws DatabaseException, RecoveryException;
    
    /**
     * Executes this action on the client.
     */
    @NonCommitting
    @OnClientRecipient
    @PureWithSideEffects
    public void executeOnClient() throws DatabaseException, RecoveryException {
        executeOnBoth();
    }
    
    @Override
    @NonCommitting
    @OnHostRecipient
    @PureWithSideEffects
    @MethodHasBeenReceived
    @TODO(task = "Implement the checks.", date = "2017-08-15", author = Author.KASPAR_ETTER, priority = Priority.HIGH)
    public @Nullable @Matching ActionReply executeOnHost() throws RequestException, DatabaseException, RecoveryException {
        final @Nullable Signature<?> signature = getSignature();
//        if (signature instanceof CredentialsSignature<?> && !((CredentialsSignature<?>) signature).isLodged()) {
//            throw RequestExceptionBuilder.withCode(RequestErrorCode.SIGNATURE).withMessage("The credentials signature of internal actions has to be lodged.").build();
//        }
        
//        final @Nonnull ReadOnlyAgentPermissions presentPermissions;
//        final @Nonnull Restrictions presentRestrictions;
//        
//        if (getService() == CoreService.INSTANCE) {
//            final @Nonnull Agent presentAgent = null; // TODO: = signature.getAgentCheckedAndRestricted(getEntity());
//            presentPermissions = presentAgent.permissions().get();
//            presentRestrictions = presentAgent.restrictions().get();
//            
//            final @Nullable Agent requiredAgent = getRequiredAgentToExecuteMethod();
//            if (requiredAgent != null) { presentAgent.checkCovers(requiredAgent); }
//        } else {
//            if (!(signature instanceof CredentialsSignature<?>)) { throw RequestExceptionBuilder.withCode(RequestErrorCode.SIGNATURE).withMessage("Internal actions of a non-core service have to be signed with credentials.").build(); }
//            final @Nonnull CredentialsSignature<?> credentialsSignature = (CredentialsSignature<?>) signature;
//            presentPermissions = null; // TODO: Get the permissions from the credentials signature.
//            presentRestrictions = null; // TODO: Get the restrictions from the credentials signature.
//        }
//        
//        final @Nonnull ReadOnlyAgentPermissions requiredPermissions = getRequiredPermissionsToExecuteMethod();
//        if (!requiredPermissions.equals(ReadOnlyAgentPermissions.NONE)) { presentPermissions.checkCover(requiredPermissions); }
//        
//        final @Nonnull Restrictions requiredRestrictions = getRequiredRestrictionsToExecuteMethod();
//        if (!requiredRestrictions.equals(Restrictions.MIN)) { presentRestrictions.checkCover(requiredRestrictions); }
        
        executeOnBoth();
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
    public abstract @Nullable InternalAction getReverse();
    
    /**
     * Reverses this internal action on the client if this action can be reversed.
     */
    @NonCommitting
    @OnClientRecipient
    @PureWithSideEffects
    public void reverseOnClient() throws DatabaseException, RecoveryException {
        final @Nullable InternalAction reverse = getReverse();
        if (reverse != null) { reverse.executeOnClient(); }
    }
    
}
