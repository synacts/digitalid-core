package ch.virtualid.handler.reply.query;

import ch.virtualid.handler.QueryReply;
import ch.virtualid.identity.SemanticType;
import javax.annotation.Nonnull;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public abstract class CoreServiceQueryReply extends QueryReply {
    
    public CoreServiceQueryReply() {
        
    }
    
    
    @Override
    public final @Nonnull SemanticType getService() {
        return SemanticType.CORE_SERVICE;
    }
    
}
