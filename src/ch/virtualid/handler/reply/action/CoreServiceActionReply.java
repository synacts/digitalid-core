package ch.virtualid.handler.reply.action;

import ch.virtualid.annotations.Pure;
import ch.virtualid.handler.ActionReply;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.CoreService;
import javax.annotation.Nonnull;

/**
 * This class models the {@link ActionReply action replies} of the {@link CoreService core service}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class CoreServiceActionReply extends ActionReply {
    
    public CoreServiceActionReply() {
        
    }
    
    
    @Pure
    @Override
    public final @Nonnull SemanticType getService() {
        return CoreService.TYPE;
    }
    
}
