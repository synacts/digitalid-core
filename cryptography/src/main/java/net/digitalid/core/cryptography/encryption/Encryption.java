package net.digitalid.core.cryptography.encryption;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.cryptography.InitializationVector;
import net.digitalid.utility.cryptography.SymmetricKey;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.time.Time;

import net.digitalid.core.identification.identifier.HostIdentifier;

/**
 *
 */
@GenerateBuilder
@GenerateSubclass
public abstract class Encryption<T> {
    
    @Pure
    public abstract @Nonnull Time getTime();
    
    @Pure
    public abstract @Nonnull HostIdentifier getRecipient();
    
    @Pure
    public abstract @Nonnull SymmetricKey getSymmetricKey();
    
    @Pure
    public abstract @Nonnull InitializationVector getInitializationVector();
    
    @Pure
    public abstract @Nonnull T getObject();
    
}
