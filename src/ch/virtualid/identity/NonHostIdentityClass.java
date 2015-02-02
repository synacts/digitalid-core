package ch.virtualid.identity;

import ch.virtualid.interfaces.Immutable;

/**
 * This class models a non-host identity.
 * 
 * @see Type
 * @see Person
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public abstract class NonHostIdentityClass extends IdentityClass implements NonHostIdentity, Immutable {
    
    /**
     * Creates a new non-host identity with the given number.
     * 
     * @param number the number that represents this identity.
     */
    NonHostIdentityClass(long number) {
        super(number);
    }
    
}
