package net.digitalid.core.handler.method.query;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.handler.annotations.Matching;
import net.digitalid.core.handler.annotations.MethodHasBeenReceived;
import net.digitalid.core.handler.method.MethodImplementation;
import net.digitalid.core.handler.reply.QueryReply;
import net.digitalid.core.unit.annotations.OnHostRecipient;

/**
 * Queries have to be sent by the caller and are thus executed synchronously.
 * 
 * @see InternalQuery
 * @see ExternalQuery
 */
@Immutable
public abstract class Query<ENTITY extends Entity<?>> extends MethodImplementation<ENTITY> {
    
    /* -------------------------------------------------- Lodged -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean isLodged() {
        return false;
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
    
    @Override
    @NonCommitting
    @OnHostRecipient
    @PureWithSideEffects
    @MethodHasBeenReceived
    public abstract @Nonnull @Matching QueryReply<ENTITY> executeOnHost() throws RequestException, DatabaseException, RecoveryException;
    
}
