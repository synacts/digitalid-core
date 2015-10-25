package net.digitalid.service.core.identity;

import net.digitalid.utility.annotations.state.Immutable;

/**
 * This class models a non-host identity.
 * 
 * @see Type
 * @see Person
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
