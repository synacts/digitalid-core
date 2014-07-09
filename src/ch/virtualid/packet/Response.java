package ch.virtualid.packet;

import ch.virtualid.cryptography.SymmetricKey;
import ch.xdf.SelfcontainedWrapper;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class decrypts, verifies and decompresses responses on the client-side.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class Response extends Packet {
    
    /**
     * Unpacks the given response with the given symmetric key.
     * 
     * @param packet the packet to unpack, with the selfcontained wrapper containing an element of type {@code packet@virtualid.ch}.
     * @param symmetricKey the symmetric key to decrypt the response or null if the request was not encrypted.
     * @param verification determines whether the signature is verified (if not, it needs to be checked explicitly).
     * 
     * @ensure for (SignatureWrapper signature : getSignatures()) signature.getSubject() != null : "The subjects of the signatures are not null.";
     * @ensure for (SignatureWrapper signature : getSignatures()) signature.isSignedLike(reference) : "All signatures that are signed are signed alike.";
     * @ensure for (SignatureWrapper signature : getSignatures()) signature instanceof HostSignatureWrapper : "All signatures that are signed are signed by a host.";
     */
    public Response(@Nonnull SelfcontainedWrapper packet, @Nullable SymmetricKey symmetricKey, boolean verification) throws PacketException {
        super(packet, symmetricKey, verification, true);
    }
    
}
