package ch.virtualid.credential;

import ch.virtualid.agent.Restrictions;
import ch.virtualid.service.CoreServiceQueryReply;
import javax.annotation.Nonnull;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
final class CredentialReply extends CoreServiceQueryReply {
    
    /**
     * Stores the restrictions for which a credential is requested.
     */
    private final @Nonnull Restrictions restrictions; // TODO: Remove?
    
    public CredentialReply() {
        
    }
    
}
