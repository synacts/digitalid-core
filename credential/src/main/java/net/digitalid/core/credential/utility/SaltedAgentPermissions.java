package net.digitalid.core.credential.utility;

import java.math.BigInteger;
import java.security.SecureRandom;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.parameters.Parameters;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;

/**
 * This class models the salted {@link ReadOnlyAgentPermissions permissions} of outgoing roles.
 */
@Immutable
@GenerateSubclass
@GenerateConverter
public abstract class SaltedAgentPermissions extends RootClass {
    
    /* -------------------------------------------------- Salt -------------------------------------------------- */
    
    /**
     * Returns the salt.
     */
    @Pure
    protected abstract @Nonnull BigInteger getSalt();
    
    /* -------------------------------------------------- Permissions -------------------------------------------------- */
    
    /**
     * Returns the permissions.
     */
    @Pure
    public abstract @Nonnull @Frozen ReadOnlyAgentPermissions getPermissions();
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    /**
     * Creates new salted permissions with the given permissions.
     */
    @Pure
    public static @Nonnull SaltedAgentPermissions with(@Nonnull @Frozen ReadOnlyAgentPermissions permissions) {
        return new SaltedAgentPermissionsSubclass(new BigInteger(Parameters.HASH.get(), new SecureRandom()), permissions);
    }
    
}
