package net.digitalid.core.identity;

import net.digitalid.core.interfaces.Immutable;

/**
 * This class models a non-host identity.
 * 
 * @see Type
 * @see Person
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
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
