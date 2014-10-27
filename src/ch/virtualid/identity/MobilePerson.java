package ch.virtualid.identity;

import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class MobilePerson extends ExternalPerson implements Immutable {
    
    /**
     * Stores the semantic type {@code mobile.person@virtualid.ch}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.create("mobile.person@virtualid.ch").load(Person.IDENTIFIER);
    
    
    public MobilePerson() {
        
    }
    
}
