package ch.virtualid.exceptions.external;

import ch.virtualid.identity.InternalIdentity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;

/**
 * This exception is thrown when an attribute cannot be found.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class AttributeNotFoundException extends SomethingNotFoundException implements Immutable {
    
    /**
     * Creates a new attribute not found exception with the given identity and type.
     * 
     * @param identity the identity whose attribute could not be found.
     * @param type the type of the attribute that could not be found.
     * 
     * @require type.isAttributeType() : "The type is an attribute type.";
     */
    public AttributeNotFoundException(@Nonnull InternalIdentity identity, @Nonnull SemanticType type) {
        super("attribute", identity, type);
    }
    
}
