package net.digitalid.core.exceptions.external;

import javax.annotation.Nonnull;
import net.digitalid.annotations.state.Immutable;
import net.digitalid.core.identity.InternalIdentity;
import net.digitalid.core.identity.SemanticType;

/**
 * This exception is thrown when an attribute cannot be found.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public final class AttributeNotFoundException extends SomethingNotFoundException {
    
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
