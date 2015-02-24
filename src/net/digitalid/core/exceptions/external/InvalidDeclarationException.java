package net.digitalid.core.exceptions.external;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.handler.Reply;
import net.digitalid.core.identifier.Identifier;
import net.digitalid.core.interfaces.Immutable;

/**
 * This exception is thrown when an identity has an invalid declaration.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class InvalidDeclarationException extends ExternalException implements Immutable {
    
    /**
     * Stores the identifier that has an invalid declaration.
     */
    private final @Nonnull Identifier identifier;
    
    /**
     * Stores the reply that contains the invalid declaration.
     */
    private final @Nonnull Reply reply;
    
    /**
     * Creates a new invalid declaration exception with the given message, identifier and reply.
     * 
     * @param message a string explaining the problem of the invalid declaration.
     * @param identifier the identifier that has an invalid declaration.
     * @param reply the reply that contains the invalid declaration.
     */
    public InvalidDeclarationException(@Nonnull String message, @Nonnull Identifier identifier, @Nonnull Reply reply) {
        super(identifier.toString() + " has an invalid declaration: " + message);
        
        this.identifier = identifier;
        this.reply = reply;
    }
    
    /**
     * Returns the identifier that has an invalid declaration.
     * 
     * @return the identifier that has an invalid declaration.
     */
    @Pure
    public @Nonnull Identifier getIdentifier() {
        return identifier;
    }
    
    /**
     * Returns the reply that contains the invalid declaration.
     * 
     * @return the reply that contains the invalid declaration.
     */
    @Pure
    public @Nonnull Reply getReply() {
        return reply;
    }
    
}
