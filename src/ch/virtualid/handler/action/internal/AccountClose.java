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
public final class AccountClose {
    
    /**
     * Stores the semantic type {@code close.account@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("close.account@virtualid.ch").load(TupleWrapper.TYPE, ch.virtualid.identity.SemanticType.UNKNOWN); //TODO
    
    
    public AccountClose() {
        
    }
    
}
