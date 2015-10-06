package net.digitalid.service.core.identity;

import net.digitalid.utility.annotations.state.Immutable;

/**
 * This class models a non-host identity.
 * 
 * @see Type
 * @see Person
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public abstract class NonHostIdentityClass extends IdentityClass implements NonHostIdentity {
    
    /**
     * Creates a new non-host identity with the given number.
     * 
     * @param number the number that represents this identity.
     */
    NonHostIdentityClass(long number) {
        super(number);
    }
    
}
