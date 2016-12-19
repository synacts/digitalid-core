package net.digitalid.core.server.handlers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.circumfixes.Quotes;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.logging.Log;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.handler.CoreHandler;
import net.digitalid.core.handler.method.query.ExternalQuery;
import net.digitalid.core.handler.reply.Reply;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.selfcontained.Selfcontained;

@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class TestQuery extends ExternalQuery<Entity<?>> implements CoreHandler<Entity<?>> {
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    /**
     * Returns the message that is sent.
     */
    @Pure
    public abstract @Nonnull String getMessage();
    
    /* -------------------------------------------------- Description -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "This is a test query with the message " + Quotes.inSingle(getMessage()) + ".";
    }
    
    /* -------------------------------------------------- Execution -------------------------------------------------- */
    
    @Override
    @NonCommitting
    // TODO: Recover the entity properly; @OnHostRecipient
    @PureWithSideEffects
    public @Nonnull TestReply executeOnHost() throws RequestException, DatabaseException {
        Log.information("Received the message $.", getMessage());
        return TestReplyBuilder.withMessage("Hi there!").withProvidedEntity(getEntity()).build();
    }
    
    /* -------------------------------------------------- Match -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean matches(@Nullable Reply reply) {
        return reply instanceof TestReply;
    }
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    public static final @Nonnull SemanticType TYPE = SemanticType.map("testquery@core.digitalid.net");
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    /* -------------------------------------------------- Conversion -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Selfcontained convert() throws ExternalException {
        return Selfcontained.convert(this, TestQueryConverter.INSTANCE);
    }
    
}
