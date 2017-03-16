package net.digitalid.core.handler.reply.instances;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.handler.CoreHandler;
import net.digitalid.core.handler.method.Method;
import net.digitalid.core.handler.reply.QueryReply;
import net.digitalid.core.identification.identity.IdentifierResolver;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.identification.identity.TypeLoader;
import net.digitalid.core.packet.Request;

/**
 * The server replies with a {@link RequestException} in case a problem occurs.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class RequestExceptionReply extends QueryReply<Entity<?>> implements CoreHandler<Entity<?>> {
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    /**
     * Stores the semantic type of the {@link RequestExceptionReplyConverter}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map(RequestExceptionReplyConverter.INSTANCE);
    
    /**
     * Makes sure that this class is loaded in the main thread.
     */
    @PureWithSideEffects
    @Initialize(target = Request.class, dependencies = {IdentifierResolver.class, TypeLoader.class})
    public static void initializeType() throws ExternalException {
        TYPE.ensureLoaded();
    }
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    /**
     * Returns the request exception that was replied.
     */
    @Pure
    public abstract @Nonnull RequestException getRequestException();
    
    /* -------------------------------------------------- Matching -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean matches(@Nonnull Method<Entity<?>> method) {
        return true;
    }
    
}
