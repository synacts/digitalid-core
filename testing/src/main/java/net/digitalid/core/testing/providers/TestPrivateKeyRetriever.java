package net.digitalid.core.testing.providers;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.asymmetrickey.KeyPair;
import net.digitalid.core.asymmetrickey.PrivateKey;
import net.digitalid.core.asymmetrickey.PrivateKeyRetriever;
import net.digitalid.core.identification.identifier.HostIdentifier;

/**
 * This class implements the {@link PrivateKeyRetriever} for unit tests.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class TestPrivateKeyRetriever implements PrivateKeyRetriever {
    
    @Pure
    protected abstract @Nonnull KeyPair getKeyPair();
    
    @Pure
    @Override
    public @Nonnull PrivateKey getPrivateKey(@Nonnull HostIdentifier host, @Nonnull Time time) {
        return getKeyPair().getPrivateKey();
    }
    
}
