package ch.virtualid.identity;

import ch.virtualid.identifier.Identifier;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;

/**
 * This class models a non-host identity.
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
    NonHostIdentity(long number, @Nonnull Identifier address) {
        super(number, address);
    }
    
}
