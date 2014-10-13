package ch.virtualid.handler.action.internal;

import ch.virtualid.annotations.Pure;
import ch.virtualid.cryptography.PublicKey;
import javax.annotation.Nullable;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class CommitmentReplace extends CoreServiceInternalAction {
    
    public CommitmentReplace() {
        
    }
    
    @Pure
    @Override
    public @Nullable PublicKey getPublicKey() {
        return null; // The commitment does not have to be with the active public key of the recipient.
    }
    
}
