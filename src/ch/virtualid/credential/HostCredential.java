package ch.virtualid.credential;

import ch.virtualid.entity.Entity;
import ch.virtualid.exceptions.InvalidDeclarationException;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.interfaces.Immutable;
import ch.xdf.Block;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models credentials on the host-side.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class HostCredential extends Credential implements Immutable {
    
    /**
     * Creates a new credential from the given blocks for hosts.
     * 
     * @param entity the entity to which the credential belongs.
     * @param exposed the block containing the exposed arguments of the credential.
     * @param randomizedPermissions the block containing the client's randomized permissions.
     * @param restrictions the block containing the client's restrictions.
     * @param i the block containing the credential's serial number.
     * 
     * @require exposed.getType().isBasedOn(Credential.EXPOSED) : "The exposed block is based on the indicated type.";
     * @require randomizedPermissions == null || randomizedPermissions.getType().isBasedOn(RandomizedAgentPermissions.TYPE) : "The randomized permissions are either null or based on the indicated type.";
     * @require restrictions == null || restrictions.getType().isBasedOn(Restrictions.TYPE) : "The restrictions are either null or based on the indicated type.";
     * @require i == null || i.getType().isBasedOn(Exponent.TYPE) : "The serial number is either null or based on the indicated type.";
     */
    public HostCredential(@Nonnull Entity entity, @Nonnull Block exposed, @Nullable Block randomizedPermissions, @Nullable Block restrictions, @Nullable Block i) throws InvalidEncodingException, FailedIdentityException, SQLException, InvalidDeclarationException {
        super(entity, exposed, randomizedPermissions, restrictions, i);
    }
    
}
