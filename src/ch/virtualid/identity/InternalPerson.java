package ch.virtualid.identity;

import ch.virtualid.interfaces.Immutable;

/**
 * Description.
 * 
 * @see NaturalPerson
 * @see ArtificialPerson
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public abstract class InternalPerson extends Person implements Immutable, InternalNonHostIdentity {
    
    public InternalPerson() {
        
    }
    
}
