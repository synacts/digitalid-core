package net.digitalid.core.resolution.handlers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
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
import net.digitalid.core.identification.identity.Identity;
import net.digitalid.core.resolution.IdentifierResolverImplementation;

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
    @TODO(task = "Check somewhere that the subject is indeed hosted on this server.", date = "2017-02-26", author = Author.KASPAR_ETTER)
    public @Nonnull @Matching IdentityReply executeOnHost() throws RequestException, DatabaseException, RecoveryException {
        final @Nullable Identity identity = IdentifierResolverImplementation.INSTANCE.load(getSubject());
        if (identity == null) { throw RequestExceptionBuilder.withCode(RequestErrorCode.IDENTITY).withMessage("There exists no identity with the identifier '" + getSubject() + "'.").build(); }
        return IdentityReplyBuilder.withEntity(getEntity()).withCategory(identity.getCategory()).build();
    }
    
}
