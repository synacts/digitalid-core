package ch.virtualid.client;

import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.cryptography.Exponent;
import ch.virtualid.exceptions.InvalidDeclarationException;
import ch.virtualid.identity.HostIdentity;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.packet.FailedRequestException;
import ch.xdf.ClientSignatureWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.math.BigInteger;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This class extends the commitment of a client with its secret.
 * 
 * @see ClientSignatureWrapper
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class SecretCommitment extends Commitment implements Immutable, Blockable {
    
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
    public SecretCommitment(@Nonnull HostIdentity host, @Nonnull Time time, @Nonnull BigInteger value, @Nonnull Exponent secret) throws SQLException, FailedRequestException, InvalidEncodingException, InvalidDeclarationException {
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
