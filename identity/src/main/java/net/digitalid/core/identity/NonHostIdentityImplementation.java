package net.digitalid.core.identity;

import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This class models a non-host identity.
 * 
 * @see Type
 * @see Person
 */
@Immutable
abstract class NonHostIdentityImplementation extends IdentityImplementation implements NonHostIdentity {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new non-host identity with the given key.
     * 
     * @param key the number that represents this identity.
     */
    NonHostIdentityImplementation(long key) {
        super(key);
    }
    
}
