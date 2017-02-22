package net.digitalid.core.client;

import javax.annotation.Nonnull;

import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.testing.CoreTest;

import org.junit.Test;

public class ClientTest extends CoreTest {
    
    @Test
    public void testClientCreation() {
        final @Nonnull Client client = ClientBuilder.withIdentifier("net.digitalid.test").withDisplayName("Test Client").withPreferredPermissions(ReadOnlyAgentPermissions.GENERAL_WRITE).build();
        assertThat(client.getDisplayName()).isEqualTo("Test Client");
        assertThat(client.getIdentifier()).isEqualTo("net.digitalid.test");
        assertThat(client.getName()).isEqualTo("net_digitalid_test");
    }
    
}
