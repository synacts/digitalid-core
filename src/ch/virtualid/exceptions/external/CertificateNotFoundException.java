package ch.virtualid.exceptions.external;

import ch.virtualid.identity.IdentityClass;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;

/**
 * This exception is thrown when a certificate cannot be found.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
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
    public CertificateNotFoundException(@Nonnull IdentityClass identity, @Nonnull SemanticType type) {
        super("certificate", identity, type);
    }
    
}
