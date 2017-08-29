package net.digitalid.core.signature.credentials;

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
public abstract class VerifiableEncryptionExponentPair {
    
    @Pure
    public abstract Exponent getElement0();
    
    @Pure
    public abstract Exponent getElement1();
    
}
