package net.digitalid.core.group;

import java.math.BigInteger;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.math.Positive;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This class models a multiplicative group with known order.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class GroupWithKnownOrder extends Group {
    
    /* -------------------------------------------------- Order -------------------------------------------------- */
    
    /**
     * Returns the order of this group.
     */
    @Pure
    public abstract @Nonnull @Positive BigInteger getOrder();
    
    /**
     * Returns a new group with the same modulus but without the order.
     */
    @Pure
    public @Nonnull GroupWithUnknownOrder dropOrder() {
        return new GroupWithUnknownOrderSubclass(getModulus());
    }
    
}
