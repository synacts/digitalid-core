package net.digitalid.core.cache.exceptions;

import javax.annotation.Nonnull;

import net.digitalid.utility.validation.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.service.core.identity.InternalIdentity;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.AttributeType;

/**
 * This exception is thrown when a certificate cannot be found.
 */
@Immutable
public class CertificateNotFoundException extends NotFoundException {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new certificate not found exception with the given identity and type.
     * 
     * @param identity the identity whose certificate could not be found.
     * @param type the type of the certificate that could not be found.
     */
    protected CertificateNotFoundException(@Nonnull InternalIdentity identity, @Nonnull @AttributeType SemanticType type) {
        super("certificate", identity, type);
    }
    
    /**
     * Returns a new certificate not found exception with the given identity and type.
     * 
     * @param identity the identity whose certificate could not be found.
     * @param type the type of the certificate that could not be found.
     * 
     * @return a new certificate not found exception with the given identity and type.
     */
    @Pure
    public static @Nonnull CertificateNotFoundException get(@Nonnull InternalIdentity identity, @Nonnull @AttributeType SemanticType type) {
        return new CertificateNotFoundException(identity, type);
    }
    
}
