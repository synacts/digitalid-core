package net.digitalid.core.cache.exceptions;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.exceptions.response.ResponseException;
import net.digitalid.core.identification.annotations.type.kind.AttributeType;
import net.digitalid.core.identification.identity.SemanticType;

/**
 * This exception is thrown when something could not be found.
 * 
 * @see AttributeNotFoundException
 * @see CertificateNotFoundException
 */
@Immutable
public abstract class NotFoundException extends ResponseException {
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    /**
     * Returns the type of the something that could not be found.
     */
    @Pure
    public abstract @Nonnull @AttributeType SemanticType getType();
    
    /* -------------------------------------------------- Message -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull String getMessage() {
        return getType().getAddress() + " of " + getIdentity().getAddress() + " could not be found.";
    }
    
}
