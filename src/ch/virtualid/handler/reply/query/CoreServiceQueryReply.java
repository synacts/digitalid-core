package ch.virtualid.handler.reply.query;

import ch.virtualid.handler.QueryReply;
import ch.virtualid.identity.SemanticType;
import javax.annotation.Nonnull;

/**
 * This class models the {@link QueryReply query replies} of the {@link CoreService core service}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class CoreServiceQueryReply extends QueryReply {
    
    public CoreServiceQueryReply() {
        
    }
    
    
    @Override
    public final @Nonnull SemanticType getService() {
        return SemanticType.CORE_SERVICE;
    }
    
}
