package ch.virtualid.handler.reply.action;

import ch.virtualid.handler.ActionReply;
import ch.virtualid.identity.SemanticType;
import javax.annotation.Nonnull;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public abstract class CoreServiceActionReply extends ActionReply {
    
    public CoreServiceActionReply() {
        
    }
    
    
    @Override
    public final @Nonnull SemanticType getService() {
        return SemanticType.CORE_SERVICE;
    }
    
}
