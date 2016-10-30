package net.digitalid.core.asymmetrickey;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;

import net.digitalid.core.group.Group;

import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This class models an asymmetric key.
 * 
 * @see PublicKey
 * @see PrivateKey
 */
@Immutable
public abstract class AsymmetricKey extends RootClass {
    
    /* -------------------------------------------------- Groups -------------------------------------------------- */
    
    /**
     * Returns the composite group for encryption and signing.
     */
    @Pure
    public abstract @Nonnull Group getCompositeGroup();
    
    /**
     * Returns the square group for verifiable encryption.
     */
    @Pure
    public abstract @Nonnull Group getSquareGroup();
    
}
