package net.digitalid.core.parameters;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.annotation.Nonnull;

import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.errors.SupportErrorBuilder;
import net.digitalid.utility.functional.interfaces.Producer;
import net.digitalid.utility.validation.annotations.type.Utility;

/**
 * This class specifies the cryptographic parameters.
 */
@Utility
public abstract class Parameters {
    
    /**
     * The bit-length of the prime factors.
     */
    public static final @Nonnull Configuration<Integer> FACTOR = Configuration.with(1_024);
    
    /**
     * The bit-length of the hash function and certain random values.
     */
    public static final @Nonnull Configuration<Integer> EXPONENT = Configuration.with(256);
    
    /**
     * The bit-length of the random commitment exponent.
     */
    public static final @Nonnull Configuration<Integer> RANDOM_EXPONENT = Configuration.with(512);
    
    /**
     * The bit-length of the credential exponent.
     */
    public static final @Nonnull Configuration<Integer> CREDENTIAL_EXPONENT = Configuration.with(512);
    
    /**
     * The bit-length of the credential exponent.
     */
    public static final @Nonnull Configuration<Integer> RANDOM_CREDENTIAL_EXPONENT = Configuration.with(768);
    
    /**
     * The bit-length of the blinding exponent.
     */
    public static final @Nonnull Configuration<Integer> BLINDING_EXPONENT = Configuration.with(768);
    
    /**
     * The bit-length of the random blinding exponent.
     */
    public static final @Nonnull Configuration<Integer> RANDOM_BLINDING_EXPONENT = Configuration.with(1_024);
    
    /**
     * The bit-length of the verifiable encryption modulus divided by 2.
     */
    public static final @Nonnull Configuration<Integer> VERIFIABLE_ENCRYPTION = Configuration.with(1_024);
    
    /**
     * The bit-length of the symmetric encryption key.
     */
    public static final @Nonnull Configuration<Integer> SYMMETRIC_KEY = Configuration.with(192);
    
    /**
     * The hash function used throughout the library.
     */
    public static final @Nonnull Configuration<Producer<MessageDigest>> HASH_FUNCTION = Configuration.withUnknownProvider();
    
    static {
        HASH_FUNCTION.set(() -> {
            try {
                return MessageDigest.getInstance("SHA-256");
            } catch (@Nonnull NoSuchAlgorithmException exception) {
                throw SupportErrorBuilder.withMessage("The hashing algorithm 'SHA-256' is not supported on this platform.").withCause(exception).build();
            }
        });
    }
    
    /**
     * The size of the computed hash. Must fit the hash function defined above.
     */
    public static final @Nonnull Configuration<Integer> HASH_SIZE = Configuration.with(256);
    
}
