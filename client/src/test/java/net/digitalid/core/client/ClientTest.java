package net.digitalid.core.client;

import javax.annotation.Nonnull;

import net.digitalid.core.asymmetrickey.CryptographyTestBase;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;

import org.junit.Test;

public class ClientTest extends CryptographyTestBase {
    
    @Test
    public void testClientCreation() {
        final @Nonnull Client client = ClientBuilder.withName("Test Client").withIdentifier("net.digitalid.test").withPreferredPermissions(ReadOnlyAgentPermissions.GENERAL_WRITE).build();
        assertEquals("Test Client", client.getName());
        assertEquals("net.digitalid.test", client.getIdentifier());
        assertEquals("net_digitalid_test", client.getSchemaName());
    }
    
}
