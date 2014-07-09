package ch.virtualid.client;

import ch.virtualid.annotation.Pure;
import ch.virtualid.concept.Time;
import ch.virtualid.cryptography.Exponent;
import ch.virtualid.identity.HostIdentity;
import ch.virtualid.interfaces.Immutable;
import ch.xdf.exceptions.InvalidEncodingException;
import java.math.BigInteger;
import javax.annotation.Nonnull;

/**
 * This class extends the commitment of a client with its secret.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class SecretCommitment extends Commitment implements Immutable {
    
    /**
     * Stores the secret of this commitment.
     */
    private final @Nonnull Exponent secret;
    
    /**
     * Creates a new commitment with the given host, time, value and secret.
     * 
     * @param host the host at which this commitment was made.
     * @param time the time at which this commitment was made.
     * @param value the value of this commitment.
     * @param secret the secret of this commitment.
     */
    public SecretCommitment(@Nonnull HostIdentity host, @Nonnull Time time, @Nonnull BigInteger value, @Nonnull Exponent secret) throws InvalidEncodingException {
        super(host, time, value);
        
        this.secret = secret;
    }
    
    /**
     * Returns the secret of this commitment.
     * 
     * @return the secret of this commitment.
     */
    @Pure
    public @Nonnull Exponent getSecret() {
        return secret;
    }
    
}
