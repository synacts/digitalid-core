package ch.virtualid.identity;

import ch.virtualid.interfaces.Immutable;

/**
 * Description.
 * 
 * @see EmailPerson
 * @see MobilePerson
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public abstract class ExternalPerson extends Person implements ExternalIdentity, Immutable {
    
    public ExternalPerson() {
        
    }
    
}
