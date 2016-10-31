package net.digitalid.core.credential;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.restrictions.Restrictions;

/**
 * This class models credentials on the host-side.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class HostCredential extends Credential {
    
    /**
     * Creates a new credential from the given blocks for hosts.
     * 
     * @param exposed the block containing the exposed arguments of the credential.
     * @param randomizedPermissions the block containing the client's randomized permissions.
     * @param restrictions the client's restrictions or null if they are not shown.
     * @param i the block containing the credential's serial number.
     * 
     * @require exposed.getType().isBasedOn(Credential.EXPOSED) : "The exposed block is based on the indicated type.";
     * @require randomizedPermissions == null || randomizedPermissions.getType().isBasedOn(RandomizedAgentPermissions.TYPE) : "The randomized permissions are either null or based on the indicated type.";
     * @require i == null || i.getType().isBasedOn(Exponent.TYPE) : "The serial number is either null or based on the indicated type.";
     */
    @NonCommitting
    public HostCredential(@Nonnull Block exposed, @Nullable Block randomizedPermissions, @Nullable Restrictions restrictions, @Nullable Block i) throws ExternalException {
        super(exposed, randomizedPermissions, restrictions, i);
    }
    
}
