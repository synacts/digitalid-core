package net.digitalid.service.core.exceptions.external.notfound;

import javax.annotation.Nonnull;
import net.digitalid.service.core.identity.InternalIdentity;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.AttributeType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This exception is thrown when an attribute cannot be found.
 */
@Immutable
public class AttributeNotFoundException extends SomethingNotFoundException {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new attribute not found exception with the given identity and type.
     * 
     * @param identity the identity whose attribute could not be found.
     * @param type the type of the attribute that could not be found.
     */
    protected AttributeNotFoundException(@Nonnull InternalIdentity identity, @Nonnull @AttributeType SemanticType type) {
        super("attribute", identity, type);
    }
    
    /**
     * Returns a new attribute not found exception with the given identity and type.
     * 
     * @param identity the identity whose attribute could not be found.
     * @param type the type of the attribute that could not be found.
     * 
     * @return a new attribute not found exception with the given identity and type.
     */
    @Pure
    public static @Nonnull AttributeNotFoundException get(@Nonnull InternalIdentity identity, @Nonnull @AttributeType SemanticType type) {
        return new AttributeNotFoundException(identity, type);
    }
    
}
