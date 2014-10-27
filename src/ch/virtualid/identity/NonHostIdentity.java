package ch.virtualid.identity;

import ch.virtualid.identifier.NonHostIdentifier;
import ch.virtualid.errors.ShouldNeverHappenError;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;

/**
 * This class models the non-host virtual identities.
 * 
 * @see Type
 * @see Person
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class NonHostIdentity extends IdentityClass implements Immutable {
    
    /**
     * Stores the semantic type {@code nonhost@virtualid.ch}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SyntacticType.NONHOST_IDENTIFIER;
    
    
    /**
     * Creates a new identity with the given number and address.
     * 
     * @param number the number that represents this identity.
     * @param address the current address of this identity.
     */
    NonHostIdentity(long number, @Nonnull NonHostIdentifier address) {
        super(number, address);
    }
    
    
    /**
     * Returns the address of this non-host identity.
     * 
     * @return the address of this non-host identity.
     */
    public @Nonnull NonHostIdentifier getNonHostAddress() {
        try {
            return getAddress().toNonHostIdentifier();
        } catch (InvalidEncodingException exception) {
            throw new ShouldNeverHappenError("Could not cast the identifier " + getAddress() + " to a non-host identifier.", exception);
        }
    }
    
    
    /**
     * Sets the address of this non-host identity.
     * 
     * @param address the new address of this identity.
     */
    final void setAddress(@Nonnull NonHostIdentifier address) {
        this.address = address;
    }
    
}
