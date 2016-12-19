package net.digitalid.core.server.handlers;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.circumfixes.Quotes;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.handler.CoreHandler;
import net.digitalid.core.handler.reply.QueryReply;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.selfcontained.Selfcontained;

@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class TestReply extends QueryReply<Entity<?>> implements CoreHandler<Entity<?>> {
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    /**
     * Returns the message that was replied.
     */
    @Pure
    public abstract @Nonnull String getMessage();
    
    /* -------------------------------------------------- Description -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Replies the message " + Quotes.inSingle(getMessage()) + ".";
    }
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return SemanticType.map("testreply@core.digitalid.net");
    }
    
    /* -------------------------------------------------- Conversion -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Selfcontained convert() throws ExternalException {
        return Selfcontained.convert(this, TestReplyConverter.INSTANCE);
    }
    
}
