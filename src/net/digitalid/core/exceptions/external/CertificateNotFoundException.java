package net.digitalid.core.exceptions.external;

import javax.annotation.Nonnull;
import net.digitalid.core.identity.InternalIdentity;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.interfaces.Immutable;

/**
 * This exception is thrown when a certificate cannot be found.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class CertificateNotFoundException extends SomethingNotFoundException implements Immutable {
    
    /**
     * Creates a new certificate not found exception with the given identity and type.
     * 
     * @param identity the identity whose certificate could not be found.
     * @param type the type of the certificate that could not be found.
     * 
     * @require type.isAttributeType() : "The type is an attribute type.";
     */
    public CertificateNotFoundException(@Nonnull InternalIdentity identity, @Nonnull SemanticType type) {
        super("certificate", identity, type);
    }
    
}
