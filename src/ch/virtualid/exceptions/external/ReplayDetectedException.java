package ch.virtualid.exceptions.external;

import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.packet.Replay;
import ch.xdf.EncryptionWrapper;
import javax.annotation.Nonnull;

/**
 * This exception is thrown when a replay of an encryption is detected.
 * 
 * @see Replay
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class ReplayDetectedException extends ExternalException implements Immutable {
    
    /**
     * Stores the encryption that has been replayed.
     */
    private final @Nonnull EncryptionWrapper encryption;
    
    /**
     * Creates a new replay detected exception with the given encryption.
     * 
     * @param encryption the encryption that has been replayed.
     */
    public ReplayDetectedException(@Nonnull EncryptionWrapper encryption) {
        super("The replay of an encryption has been detected.");
        
        this.encryption = encryption;
    }
    
    /**
     * Returns the encryption that has been replayed.
     * 
     * @return the encryption that has been replayed.
     */
    @Pure
    public @Nonnull EncryptionWrapper getEncryption() {
        return encryption;
    }
    
}
