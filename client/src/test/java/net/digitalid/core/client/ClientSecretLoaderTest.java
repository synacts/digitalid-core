package net.digitalid.core.client;

import java.math.BigInteger;
import java.security.SecureRandom;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.ExternalException;

import net.digitalid.core.asymmetrickey.CryptographyTestBase;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.group.ExponentBuilder;
import net.digitalid.core.parameters.Parameters;

import org.junit.Test;

public class ClientSecretLoaderTest extends CryptographyTestBase {

    @Test
    public void testSecretPersistence() throws ExternalException {
        final @Nonnull Exponent secret = ExponentBuilder.withValue(new BigInteger(Parameters.EXPONENT.get(), new SecureRandom())).build();
        ClientSecretLoader.store("test.secret", secret);
        final @Nonnull Exponent actual = ClientSecretLoader.load("test.secret");
        assertEquals(secret, actual);
    }

}
