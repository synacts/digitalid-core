package ch.virtualid.pusher;

import ch.virtualid.annotations.Pure;
import ch.virtualid.handler.ActionReply;
import ch.virtualid.handler.ExternalAction;
import ch.virtualid.identity.SemanticType;
import ch.xdf.SelfcontainedWrapper;
import javax.annotation.Nonnull;

/**
 * Description.
 * 
 * @see Pusher
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class PushReturned extends ExternalAction {
    
    /**
     * Stores the semantic type {@code returned.push@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("returned.push@virtualid.ch").load(SelfcontainedWrapper.TYPE);
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    
    /**
     * Stores the reply which was returned.
     */
    private final @Nonnull ActionReply reply;
    
    
}
