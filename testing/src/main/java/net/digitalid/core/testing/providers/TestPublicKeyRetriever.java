package net.digitalid.core.testing.providers;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.asymmetrickey.KeyPair;
import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.asymmetrickey.PublicKeyRetriever;
import net.digitalid.core.identification.identity.HostIdentity;

/**
 * This class implements the {@link PublicKeyRetriever} for unit tests.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class TestPublicKeyRetriever implements PublicKeyRetriever {
    
    @Pure
    protected abstract @Nonnull KeyPair getKeyPair();
    
    @Pure
    @Override
    public @Nonnull PublicKey getPublicKey(@Nonnull HostIdentity host, @Nonnull Time time) {
        return getKeyPair().getPublicKey();
    }
    
}
