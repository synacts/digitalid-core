package net.digitalid.core.signature.credentials;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;

import net.digitalid.core.group.Exponent;

/**
 *
 */
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class VerifiableEncryption {
    
    @Pure
    public abstract @Nonnull VerifiableEncryptionMessage getEncryptionForSerial();
    
    @Pure
    public abstract @Nonnull Exponent getSolutionForSerial();
    
    @Pure
    public abstract @Nonnull VerifiableEncryptionMessage getEncryptionForBlindingValue();
    
    @Pure
    public abstract @Nonnull Exponent getSolutionForBlindingValue();
    
}
