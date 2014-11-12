package ch.virtualid.handler.action.internal;

import ch.virtualid.identity.SemanticType;
import ch.xdf.TupleWrapper;
import javax.annotation.Nonnull;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class AccountInitialize {
    
    /**
     * Stores the semantic type {@code initialize.account@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("initialize.account@virtualid.ch").load(TupleWrapper.TYPE, ch.virtualid.identity.SemanticType.UNKNOWN); //TODO
    
    
    public AccountInitialize() {
        
    }
    
//    @Pure
//    @Override
//    public @Nullable PublicKey getPublicKey() {
//        return null; // The commitment does not have to be with the active public key of the recipient.
//    }
    
}
