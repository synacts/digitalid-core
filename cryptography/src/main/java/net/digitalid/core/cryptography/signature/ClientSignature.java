package net.digitalid.core.cryptography.signature;

import java.math.BigInteger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.math.Exponent;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.generation.Derive;

import net.digitalid.core.commitment.SecretCommitment;

/**
 *
 */
@GenerateBuilder
@GenerateSubclass
public abstract class ClientSignature<T> extends Signature<T> {
    
    @Pure
    public abstract @Nonnull SecretCommitment getSecretCommitment();
    
    @Pure
    public abstract @Nonnull BigInteger getT();
    
    @Pure
    public abstract @Nonnull Exponent getS();
    
}
