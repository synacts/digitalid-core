package ch.virtualid.packet;

import ch.virtualid.client.Commitment;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identity.Identifier;
import ch.xdf.SelfcontainedWrapper;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * This class compresses, signs and encrypts requests by clients.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class ClientRequest extends Request {
    
    /**
     * Determines how often the cached symmetric keys are rotated (currently once an hour).
     */
    private static final long ROTATION = 1000 * 60 * 60;
    
    /**
     * Packs the given content with the given arguments signed by the given client.
     * 
     * @param contents the contents of this request.
     * @param subject the subject of this request.
     * @param audit the audit with the time of the last retrieval.
     * @param commitment the commitment containing the client secret.
     * 
     * @require methods.isFrozen() : "The methods are frozen.";
     * @require methods.isNotEmpty() : "The methods are not empty.";
     * @require methods.doesNotContainNull() : "The methods do not contain null.";
     * 
     * @require !contents.isEmpty() : "The list of contents is not empty.";
     * @require subject.getHostIdentifier().exists() : "The host of the given subject has to exist.";
     * @ensure getSize() == contents.size() : "The size of this request equals the size of the contents.";
     */
    public ClientRequest(@Nonnull List<SelfcontainedWrapper> contents, @Nonnull Identifier subject, @Nonnull Audit audit, @Nonnull Commitment commitment) throws SQLException, IOException, PacketException, ExternalException {
        super(contents, subject.getHostIdentifier(), getSymmetricKey(subject.getHostIdentifier(), ROTATION), subject, audit, null, commitment, null, null, false, null);
    }
    
}
