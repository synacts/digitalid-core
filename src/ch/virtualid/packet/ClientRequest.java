package ch.virtualid.packet;

import ch.virtualid.auxiliary.Time;
import ch.virtualid.client.SecretCommitment;
import ch.virtualid.exceptions.external.ExternalException;
import static ch.virtualid.exceptions.packet.PacketError.INTERNAL;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Method;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.Identifier;
import ch.virtualid.util.FreezableList;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This class compresses, signs and encrypts requests by clients.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class ClientRequest extends Request {
    
    /**
     * Stores the commitment containing the client secret.
     */
    private final @Nonnull SecretCommitment commitment;
    
    /**
     * Packs the given methods with the given arguments signed by the given client.
     * 
     * @param methods the methods of this request.
     * @param subject the subject of this request.
     * @param audit the audit with the time of the last retrieval.
     * @param commitment the commitment containing the client secret.
     * 
     * @require methods.isFrozen() : "The methods are frozen.";
     * @require methods.isNotEmpty() : "The methods are not empty.";
     * @require Method.areSimilar(methods) : "All methods are similar and not null.";
     */
    public ClientRequest(@Nonnull FreezableList<Method> methods, @Nonnull Identifier subject, @Nonnull Audit audit, @Nonnull SecretCommitment commitment) throws SQLException, IOException, PacketException, ExternalException {
        super(methods, subject.getHostIdentifier(), getSymmetricKey(subject.getHostIdentifier(), Time.HOUR), subject, audit, null, commitment, null, null, false, null);
        
        this.commitment = commitment;
    }
    
    @Override
    @Nonnull Response resend(@Nonnull FreezableList<Method> methods, @Nonnull HostIdentifier recipient, @Nonnull Identifier subject, boolean verified) throws SQLException, IOException, PacketException, ExternalException {
        if (!subject.getHostIdentifier().equals(recipient)) throw new PacketException(INTERNAL, "The host of the subject " + subject + " does not match the recipient " + recipient + ".");
        return new ClientRequest(methods, subject, getAudit(), commitment).send(verified);
    }
    
    /**
     * Recommits the client secret to the current public key of the recipient and resends this request.
     * 
     * @param methods the methods of this request.
     * @param verified determines whether the signature of the response is verified (if not, it needs to be checked by the caller).
     * 
     * @return the response to the resent request with the new commitment.
     */
    @Nonnull Response recommit(@Nonnull FreezableList<Method> methods, boolean verified) throws SQLException, IOException, PacketException, ExternalException {
        // TODO: Rotate the commitment of the client.
        return new ClientRequest(methods, getSubject(), getAudit(), commitment).send(verified);
    }
    
}
