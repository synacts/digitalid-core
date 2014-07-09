package ch.virtualid.credential;

import ch.virtualid.identity.FailedIdentityException;
import ch.xdf.Block;
import ch.xdf.exceptions.InvalidEncodingException;
import javax.annotation.Nonnull;

/**
 * This class models host credentials (i.e. credentials on the host-side).
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class HostCredential extends Credential {
    
    /**
     * Creates a new credential from the given blocks for hosts.
     * 
     * @param exposed the block containing the exposed argument of the credential.
     * @param randomizedPermissions the block containing the client's randomized permissions.
     * @param restrictions the block containing the client's restrictions.
     * @param i the block containing the credential's serial number.
     */
    public HostCredential(@Nonnull Block exposed, @Nonnull Block randomizedPermissions, @Nonnull Block restrictions, @Nonnull Block i) throws InvalidEncodingException, FailedIdentityException {
        super(exposed, randomizedPermissions, restrictions, i);
    }
    
}
