package ch.virtualid.packet;

import ch.virtualid.auxiliary.Time;
import ch.virtualid.client.SecretCommitment;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Method;
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
     * Packs the given methods with the given arguments signed by the given client.
     * 
     * @param methods the methods of this request.
     * @param subject the subject of this request.
     * @param audit the audit with the time of the last retrieval.
     * @param commitment the commitment containing the client secret.
     * 
     * @require methods.isFrozen() : "The methods are frozen.";
     * @require methods.isNotEmpty() : "The methods are not empty.";
     * @require methods.doesNotContainNull() : "The methods do not contain null.";
     */
    public ClientRequest(@Nonnull FreezableList<Method> methods, @Nonnull Identifier subject, @Nonnull Audit audit, @Nonnull SecretCommitment commitment) throws SQLException, IOException, PacketException, ExternalException {
        super(methods, subject.getHostIdentifier(), getSymmetricKey(subject.getHostIdentifier(), Time.HOUR), subject, audit, null, commitment, null, null, false, null);
    }
    
}
