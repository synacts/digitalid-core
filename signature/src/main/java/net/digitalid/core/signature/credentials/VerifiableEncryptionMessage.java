package net.digitalid.core.signature.credentials;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;

import net.digitalid.core.group.Element;

/**
 *
 */
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class VerifiableEncryptionMessage {
    
    @Pure
    public abstract @Nonnull Element getElement0();
    
    @Pure
    public abstract @Nonnull Element getElement1();
    
}
