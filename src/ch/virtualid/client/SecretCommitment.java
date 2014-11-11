package ch.virtualid.client;

import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.cryptography.Element;
import ch.virtualid.cryptography.Exponent;
import ch.virtualid.cryptography.PublicKey;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identity.HostIdentity;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.xdf.ClientSignatureWrapper;
import javax.annotation.Nonnull;

/**
 * This class extends the {@link Commitment commitment} of a {@link Client client} with its {@link Client#getSecret() secret}.
 * 
 * @invariant getPublicKey().getAu().pow(getSecret()).equals(getValue()) : "The secret matches the commitment.";
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
     * Creates a new commitment with the given host, time, value, public key and secret.
     * 
     * @param host the host at which this commitment was made.
     * @param time the time at which this commitment was made.
     * @param value the value of this commitment.
     * @param publicKey the public key of this commitment.
     * @param secret the secret of this commitment.
     */
    SecretCommitment(@Nonnull HostIdentity host, @Nonnull Time time, @Nonnull Element value, @Nonnull PublicKey publicKey, @Nonnull Exponent secret) throws PacketException {
        super(host, time, value, publicKey);
        
        if (!publicKey.getAu().pow(secret).equals(value)) throw new PacketException(PacketError.INTERNAL, "The secret does not match the commitment.");
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
