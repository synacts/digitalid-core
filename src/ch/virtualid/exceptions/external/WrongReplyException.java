package ch.virtualid.exceptions.external;

import ch.virtualid.annotations.Pure;
import ch.virtualid.handler.Reply;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;

/**
 * This exception is thrown when a reply has the wrong type.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class WrongReplyException extends ExternalException implements Immutable {
    
    /**
     * Stores the reply with the wrong type.
     */
    private final @Nonnull Reply reply;
    
    /**
     * Creates a new wrong reply exception with the reply.
     * 
     * @param reply the reply with the wrong type.
     */
    public WrongReplyException(@Nonnull Reply reply) {
        super("A reply had the wrong type.");
        
        this.reply = reply;
    }
    
    /**
     * Returns the reply with the wrong type.
     * 
     * @return the reply with the wrong type.
     */
    @Pure
    public @Nonnull Reply getReply() {
        return reply;
    }
    
}
