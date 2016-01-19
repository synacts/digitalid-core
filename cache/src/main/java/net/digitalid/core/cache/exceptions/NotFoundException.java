package net.digitalid.core.cache.exceptions;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.external.ExternalException;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;

import net.digitalid.service.core.identity.InternalIdentity;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.AttributeType;

/**
 * This exception is thrown when something cannot be found.
 * 
 * @see AttributeNotFoundException
 * @see CertificateNotFoundException
 */
@Immutable
public abstract class NotFoundException extends ExternalException {
    
    /* -------------------------------------------------- Identity -------------------------------------------------- */
    
    /**
     * Stores the identity whose something could not be found.
     */
    private final @Nonnull InternalIdentity identity;
    
    /**
     * Returns the identity whose something could not be found.
     * 
     * @return the identity whose something could not be found.
     */
    @Pure
    public final @Nonnull InternalIdentity getIdentity() {
        return identity;
    }
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    /**
     * Stores the type of the something that could not be found.
     */
    private final @Nonnull @AttributeType SemanticType type;
    
    /**
     * Returns the type of the something that could not be found.
     * 
     * @return the type of the something that could not be found.
     */
    @Pure
    public final @Nonnull SemanticType getType() {
        return type;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new something not found exception with the given identity and type.
     * 
     * @param something a string stating what the something actually is.
     * @param identity the identity whose something could not be found.
     * @param type the type of the something that could not be found.
     */
    protected NotFoundException(@Nonnull String something, @Nonnull InternalIdentity identity, @Nonnull @AttributeType SemanticType type) {
        super("The " + something + " " + type.getAddress() + " of " + identity.getAddress() + " could not be found.");
        
        assert type.isAttributeType() : "The type is an attribute type.";
        
        this.identity = identity;
        this.type = type;
    }
    
}
