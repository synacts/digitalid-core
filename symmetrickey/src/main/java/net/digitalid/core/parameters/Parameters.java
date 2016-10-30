package net.digitalid.core.parameters;

import javax.annotation.Nonnull;

import net.digitalid.utility.configuration.Configuration;
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
    public static final @Nonnull Configuration<Integer> HASH = Configuration.with(256);
    
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
    public static final @Nonnull Configuration<Integer> ENCRYPTION_KEY = Configuration.with(192);
    
}
