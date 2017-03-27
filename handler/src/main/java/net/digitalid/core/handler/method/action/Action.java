package net.digitalid.core.handler.method.action;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.collections.list.FreezableLinkedList;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.storage.Storage;

import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.handler.Auditable;
import net.digitalid.core.handler.annotations.Matching;
import net.digitalid.core.handler.annotations.MethodHasBeenReceived;
import net.digitalid.core.handler.method.Method;
import net.digitalid.core.handler.method.MethodImplementation;
import net.digitalid.core.handler.reply.ActionReply;
import net.digitalid.core.unit.annotations.OnHostRecipient;

/**
 * Actions affect the state of a digital identity and are thus always audited.
 * The default is to sign them identity-based. If another behavior is desired, the method
 * {@link Method#send()} needs to be overridden. Actions are executed asynchronously.
 * 
 * @see InternalAction
 * @see ExternalAction
 */
@Immutable
public abstract class Action extends MethodImplementation<NonHostEntity<?>> implements Auditable {
    
    /* -------------------------------------------------- Lodged -------------------------------------------------- */
    
    @Pure
    @Override
    public final boolean isLodged() {
        return true;
    }
    
    /* -------------------------------------------------- Execution -------------------------------------------------- */
    
    @Override
    @NonCommitting
    @OnHostRecipient
    @PureWithSideEffects
    @MethodHasBeenReceived
    public abstract @Nullable @Matching ActionReply executeOnHost() throws RequestException, DatabaseException, RecoveryException;
    
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
