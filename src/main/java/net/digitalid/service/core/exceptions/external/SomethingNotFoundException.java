package net.digitalid.service.core.exceptions.external;

import javax.annotation.Nonnull;
import net.digitalid.service.core.identity.InternalIdentity;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This exception is thrown when something cannot be found.
 * 
 * @see AttributeNotFoundException
 * @see CertificateNotFoundException
 */
@Immutable
public abstract class SomethingNotFoundException extends ExternalException {
    
    /**
     * Stores the identity whose something could not be found.
     */
    private final @Nonnull InternalIdentity identity;
    
    /**
     * Stores the type of the something that could not be found.
     * 
     * @invariant type.isAttributeType() : "The type is an attribute type.";
     */
    private final @Nonnull SemanticType type;
    
    /**
     * Creates a new something not found exception with the given identity and type.
     * 
     * @param something a string stating what the something actually is.
     * @param identity the identity whose something could not be found.
     * @param type the type of the something that could not be found.
     * 
     * @require type.isAttributeType() : "The type is an attribute type.";
     */
    protected SomethingNotFoundException(@Nonnull String something, @Nonnull InternalIdentity identity, @Nonnull SemanticType type) {
        super("The " + something + " " + type.getAddress() + " of " + identity.getAddress() + " could not be found.");
        
        assert type.isAttributeType() : "The type is an attribute type.";
        
        this.identity = identity;
        this.type = type;
    }
    
    /**
     * Returns the identity whose something could not be found.
     * 
     * @return the identity whose something could not be found.
     */
    @Pure
    public final @Nonnull InternalIdentity getIdentity() {
        return identity;
    }
    
    /**
     * Returns the type of the something that could not be found.
     * 
     * @return the type of the something that could not be found.
     */
    @Pure
    public final @Nonnull SemanticType getType() {
        return type;
    }
    
}
