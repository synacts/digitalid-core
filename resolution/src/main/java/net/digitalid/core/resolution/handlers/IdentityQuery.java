package net.digitalid.core.resolution.handlers;

import javax.annotation.Nonnull;

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
import net.digitalid.core.exceptions.request.RequestExceptionBuilder;
import net.digitalid.core.handler.annotations.Matching;
import net.digitalid.core.handler.annotations.MethodHasBeenReceived;
import net.digitalid.core.handler.method.CoreMethod;
import net.digitalid.core.handler.method.query.ExternalQuery;
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
public abstract class IdentityQuery extends ExternalQuery<NonHostEntity<?>> implements CoreMethod<NonHostEntity<?>> {
    
    /* -------------------------------------------------- Execution -------------------------------------------------- */
    
    @Override
    @NonCommitting
    @OnHostRecipient
    @PureWithSideEffects
    @MethodHasBeenReceived
    public @Nonnull @Matching IdentityReply executeOnHost() throws RequestException, DatabaseException {
        final @Nonnull InternalIdentifier subject = getSubject(); // The following exception should never be thrown as the condition is already checked in the packet class.
        if (!(subject instanceof InternalNonHostIdentifier)) { throw RequestExceptionBuilder.withCode(RequestErrorCode.IDENTITY).withMessage("The identity may only be queried of non-host identities.").build(); }
        // TODO: return IdentityReplySubclass((InternalNonHostIdentifier) subject);
        throw new UnsupportedOperationException();
    }
    
}
