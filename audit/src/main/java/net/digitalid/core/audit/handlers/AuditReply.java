package net.digitalid.core.audit.handlers;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.handler.reply.QueryReply;
import net.digitalid.core.identification.identity.SemanticType;

/**
 * Replies the audit of the given entity.
 * 
 * @see AuditQuery
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
@TODO(task = "Make sure that this query gets registered as a handler. Maybe with an annotation?", date = "2016-11-09", author = Author.KASPAR_ETTER)
public abstract class AuditReply extends QueryReply<NonHostEntity> {
    
    // TODO: So far, the response audit has been appended to the signature, which will probably no longer be the case.
    
    /* -------------------------------------------------- Other -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code reply.audit@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("reply.audit@core.digitalid.net").load(null /* TODO: SemanticType.IDENTIFIER */);
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Replies the audit.";
    }
    
}
