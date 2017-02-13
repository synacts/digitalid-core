package net.digitalid.core.server.handlers;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.handler.CoreHandler;
import net.digitalid.core.handler.method.Method;
import net.digitalid.core.handler.reply.QueryReply;

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
    
    /* -------------------------------------------------- Matching -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean matches(@Nonnull Method<Entity<?>> method) {
        return method instanceof TestQuery;
    }
    
}
