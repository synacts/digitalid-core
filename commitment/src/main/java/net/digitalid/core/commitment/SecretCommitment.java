package net.digitalid.core.commitment;

import java.math.BigInteger;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.contracts.Validate;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.generation.OrderOfAssignment;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.group.Exponent;

/**
 * This class extends the {@link Commitment commitment} of a client with its secret.
 * 
 * @invariant getPublicKey().getAu().pow(getSecret()).equals(getElement()) : "The secret has to match the commitment.";
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public interface SecretCommitment extends Commitment {
    
    /* -------------------------------------------------- Secret -------------------------------------------------- */
    
    /**
     * Returns the secret of this commitment.
     */
    @Pure
    public @Nonnull Exponent getSecret();
    
    @Pure
    @Override
    @OrderOfAssignment(1)
    @Derive("getPublicKey().getAu().pow(getSecret()).getValue()")
    public @Nonnull BigInteger getValue();
    
    /* -------------------------------------------------- Validation -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    public default void validate() {
        Commitment.super.validate();
        Validate.that(getPublicKey().getAu().pow(getSecret()).equals(getElement())).orThrow("The secret has to match the commitment.");
    }
    
}
