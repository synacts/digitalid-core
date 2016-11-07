package net.digitalid.core.handler.method.action;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collections.list.FreezableLinkedList;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.storage.Storage;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.handler.Auditable;
import net.digitalid.core.handler.method.Method;
import net.digitalid.core.handler.reply.ActionReply;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.restrictions.Restrictions;

/**
 * Actions affect the state of a digital identity and are thus always audited.
 * The default is to sign them identity-based. If another behavior is desired, the method
 * {@link Method#send()} needs to be overridden. Actions are executed asynchronously.
 * 
 * @see InternalAction
 * @see ExternalAction
 */
@Immutable
public abstract class Action extends Method<NonHostEntity> implements Auditable {
    
    /* -------------------------------------------------- Lodged -------------------------------------------------- */
    
    @Pure
    @Override
    public final boolean isLodged() {
        return true;
    }
    
    /* -------------------------------------------------- Execution -------------------------------------------------- */
    
    @Pure // TODO
    @Override
    @NonCommitting
    public abstract @Nullable ActionReply executeOnHost() throws RequestException, DatabaseException;
    
    /**
     * Executes this action on the client.
     * 
     * @throws DatabaseException if this handler cannot be executed.
     * 
     * @require isOnClient() : "This method is called on a client.";
     */
    @Pure // TODO
    // @OnClient // TODO
    @NonCommitting
    public abstract void executeOnClient() throws DatabaseException;
    
    /**
     * This method is executed after successful transmission.
     * 
     * @throws DatabaseException if this handler cannot be executed.
     */
    @Pure // TODO
    @NonCommitting
    public void executeOnSuccess() throws DatabaseException {}
    
    /* -------------------------------------------------- Auditable -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Storage -------------------------------------------------- */
    
    /**
     * Returns the storage on which this action operates.
     */
    @Pure
    public abstract @Nonnull Storage getStorage();
    
    private static final @Nonnull @Frozen ReadOnlyList<Storage> EMPTY = FreezableLinkedList.<Storage>withNoElements().freeze();
    
    /**
     * Returns the storages that need to be reloaded and are thus suspended.
     */
    @Pure
    public @Nonnull @Frozen ReadOnlyList<Storage> getStoragesToBeSuspended() {
        return EMPTY;
    }
    
}
