package net.digitalid.core.host;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.ExternalException;

import net.digitalid.core.asymmetrickey.KeyPair;
import net.digitalid.core.asymmetrickey.PrivateKeyRetriever;
import net.digitalid.core.asymmetrickey.PublicKeyRetriever;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.testing.CoreTest;
import net.digitalid.core.testing.providers.TestPrivateKeyRetrieverBuilder;
import net.digitalid.core.testing.providers.TestPublicKeyRetrieverBuilder;

import org.junit.Test;

public class HostTest extends CoreTest {
    
    @Test
    public void testHostCreation() throws ExternalException {
        // TODO: Remove the following three lines as soon as the cache works.
        final @Nonnull KeyPair keyPair = KeyPair.withRandomValues();
        PublicKeyRetriever.configuration.set(TestPublicKeyRetrieverBuilder.withKeyPair(keyPair).build());
        PrivateKeyRetriever.configuration.set(TestPrivateKeyRetrieverBuilder.withKeyPair(keyPair).build());
        
        final @Nonnull Host host = HostBuilder.withIdentifier(HostIdentifier.with("test.digitalid.net")).build();
        assertThat(host.getIdentifier().getString()).isEqualTo("test.digitalid.net");
    }
    
}
