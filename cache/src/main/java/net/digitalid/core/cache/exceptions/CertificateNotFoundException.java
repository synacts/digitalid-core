package net.digitalid.core.cache.exceptions;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.identification.annotations.type.kind.AttributeType;
import net.digitalid.core.identification.identity.InternalIdentity;
import net.digitalid.core.identification.identity.SemanticType;

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
    public static @Nonnull CertificateNotFoundException with(@Nonnull InternalIdentity identity, @Nonnull @AttributeType SemanticType type) {
        return new CertificateNotFoundException(identity, type);
    }
    
}
