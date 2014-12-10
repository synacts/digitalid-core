package ch.virtualid.expression;

import ch.virtualid.identity.SemanticType;
import ch.xdf.StringWrapper;
import javax.annotation.Nonnull;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public class PassiveExpression {
    
    /**
     * Stores the semantic type {@code passive.expression@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("passive.expression@virtualid.ch").load(StringWrapper.TYPE);
    
    
    public PassiveExpression(String string) {
        
    }
    
}
