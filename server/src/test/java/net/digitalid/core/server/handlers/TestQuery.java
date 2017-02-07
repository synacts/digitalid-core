package net.digitalid.core.server.handlers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.logging.Log;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.annotations.OnHostRecipient;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.handler.method.CoreMethod;
import net.digitalid.core.handler.method.query.ExternalQuery;
import net.digitalid.core.handler.reply.Reply;

@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class TestQuery extends ExternalQuery<Entity<?>> implements CoreMethod<Entity<?>> {
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    /**
     * Returns the message that is sent.
     */
    @Pure
    public abstract @Nonnull String getMessage();
    
    /* -------------------------------------------------- Execution -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean matches(@Nullable Reply<Entity<?>> reply) {
        return reply instanceof TestReply;
    }
    
    @Override
    @NonCommitting
    @OnHostRecipient
    @PureWithSideEffects
    public @Nonnull TestReply executeOnHost() throws RequestException, DatabaseException {
        Log.information("Received the message $.", getMessage());
        return TestReplyBuilder.withEntity(getEntity()).withMessage("Hi there!").build();
    }
    
}
