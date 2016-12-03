package net.digitalid.core.cache.exceptions;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.identification.annotations.type.kind.AttributeType;
import net.digitalid.core.identification.identity.InternalIdentity;
import net.digitalid.core.identification.identity.SemanticType;

/**
 * This exception is thrown when an attribute cannot be found.
 */
@Immutable
public class AttributeNotFoundException extends NotFoundException {
    
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
    public static @Nonnull AttributeNotFoundException with(@Nonnull InternalIdentity identity, @Nonnull @AttributeType SemanticType type) {
        return new AttributeNotFoundException(identity, type);
    }
    
}
