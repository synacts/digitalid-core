package net.digitalid.core.asymmetrickey;

import java.math.BigInteger;
import java.security.MessageDigest;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.core.group.Element;
import net.digitalid.core.parameters.Parameters;

/**
 * Generates cryptographic hashes.
 */
@Utility
public abstract class HashGenerator {

    /**
     * Generates and returns a cryptographic hash using the SHA-256 hash algorithm on the values of the given elements.
     */
    @Pure
    public static @Nonnull BigInteger generateHash(@NonCaptured @Unmodified @Nonnull @NonNullableElements Element... elements) {
        final @Nonnull MessageDigest instance = Parameters.HASH_FUNCTION.get().produce();
        for (@Nonnull Element element : elements) {
            final @Nonnull byte[] bytes = element.getValue().toByteArray();
            instance.update(bytes); // TODO: Verify that this works!
            instance.update((byte) 0);
        }
        return new BigInteger(1, instance.digest());
    }
    
}
