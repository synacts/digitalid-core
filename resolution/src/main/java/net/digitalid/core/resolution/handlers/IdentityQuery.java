package net.digitalid.core.resolution.handlers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.entity.annotations.OnHostRecipient;
import net.digitalid.core.exceptions.request.RequestErrorCode;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.handler.CoreHandler;
import net.digitalid.core.handler.method.query.ExternalQuery;
import net.digitalid.core.handler.reply.Reply;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;

/**
 * Queries the identity of the given subject.
 * 
 * @see IdentityReply
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class IdentityQuery extends ExternalQuery<NonHostEntity<?>> implements CoreHandler<NonHostEntity<?>> {
    
    /* -------------------------------------------------- Execution -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean matches(@Nullable Reply<NonHostEntity<?>> reply) {
        return reply instanceof IdentityReply;
    }
    
    @Override
    @NonCommitting
    @OnHostRecipient
    @PureWithSideEffects
    public @Nonnull IdentityReply executeOnHost() throws RequestException, DatabaseException {
        final @Nonnull InternalIdentifier subject = getSubject(); // The following exception should never be thrown as the condition is already checked in the packet class.
        if (!(subject instanceof InternalNonHostIdentifier)) { throw RequestException.with(RequestErrorCode.IDENTIFIER, "The identity may only be queried of non-host identities."); }
        // TODO: return IdentityReplySubclass((InternalNonHostIdentifier) subject);
        throw new UnsupportedOperationException();
    }
    
}
