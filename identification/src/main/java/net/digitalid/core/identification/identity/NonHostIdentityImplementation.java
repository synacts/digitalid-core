package net.digitalid.core.identification.identity;

import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This class models a non-host identity.
 * 
 * @see Type
 * @see Person
 */
@Immutable
@Deprecated // TODO: No longer necessary in Java 1.8!
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
