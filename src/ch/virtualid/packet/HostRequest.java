package ch.virtualid.packet;

import ch.virtualid.auxiliary.Time;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Method;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.Identifier;
import ch.virtualid.util.FreezableList;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This class compresses, signs and encrypts requests by hosts.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class HostRequest extends Request {
    
    /**
     * Stores the identifier of the signing host.
     */
    private final @Nonnull Identifier signer;
    
    /**
     * Packs the given methods with the given arguments signed by the given host.
     * 
     * @param methods the methods of this request.
     * @param recipient the recipient of this request.
     * @param subject the subject of this request.
     * @param signer the identifier of the signing host.
     * 
     * @require methods.isFrozen() : "The methods are frozen.";
     * @require methods.isNotEmpty() : "The methods are not empty.";
     * @require Method.areSimilar(methods) : "All methods are similar and not null.";
     * @require Server.hasHost(signer.getHostIdentifier()) : "The host of the signer is running on this server.";
     */
    public HostRequest(@Nonnull FreezableList<Method> methods, @Nonnull HostIdentifier recipient, @Nonnull Identifier subject, @Nonnull Identifier signer) throws SQLException, IOException, PacketException, ExternalException {
        super(methods, recipient, getSymmetricKey(recipient, Time.TROPICAL_YEAR), subject, null, signer, null, null, null, false, null);
        
        this.signer = signer;
    }
    
    @Override
    @Nonnull Response resend(@Nonnull FreezableList<Method> methods, @Nonnull HostIdentifier recipient, @Nonnull Identifier subject, boolean verified) throws SQLException, IOException, PacketException, ExternalException {
        return new HostRequest(methods, recipient, subject, signer).send(verified);
    }
    
}
