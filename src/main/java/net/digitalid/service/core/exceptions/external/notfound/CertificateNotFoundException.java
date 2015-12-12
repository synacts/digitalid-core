package net.digitalid.service.core.exceptions.external.notfound;

import javax.annotation.Nonnull;
import net.digitalid.service.core.identity.InternalIdentity;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.AttributeType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

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
