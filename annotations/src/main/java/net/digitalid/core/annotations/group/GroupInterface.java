package net.digitalid.core.annotations.group;

import java.math.BigInteger;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.rootclass.RootInterface;
import net.digitalid.utility.validation.annotations.math.Positive;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This interface models a multiplicative group.
 */
@Immutable
public interface GroupInterface extends RootInterface {
    
    /* -------------------------------------------------- Modulus -------------------------------------------------- */
    
    /**
     * Returns the modulus of this group.
     */
    @Pure
    public @Nonnull @Positive BigInteger getModulus();
    
}
