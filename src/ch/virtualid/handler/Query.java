package ch.virtualid.handler;

import ch.virtualid.entity.Entity;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.Identifier;
import ch.virtualid.packet.PacketException;
import ch.xdf.SignatureWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Queries have to be sent by the caller and are thus executed synchronously.
 * 
 * @see InternalQuery
 * @see ExternalQuery
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class Query extends Method {
    
    /**
     * Creates a query that encodes the content of a packet for the given recipient about the given subject.
     * 
     * @param entity the entity to which this handler belongs.
     * @param subject the subject of this handler.
     * @param recipient the recipient of this method.
     * 
     * @require !(entity instanceof Account) || canBeSentByHosts() : "Methods encoded on hosts can be sent by hosts.";
     * @require !(entity instanceof Role) || !canOnlyBeSentByHosts() : "Methods encoded on clients cannot only be sent by hosts.";
     */
    protected Query(@Nullable Entity entity, @Nonnull Identifier subject, @Nonnull HostIdentifier recipient) {
        super(entity, subject, recipient);
    }
    
    /**
     * Creates a query that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param recipient the recipient of this method.
     * 
     * @require signature.getSubject() != null : "The subject of the signature is not null.";
     * 
     * @ensure getEntity() != null : "The entity of this handler is not null.";
     * @ensure getSignature() != null : "The signature of this handler is not null.";
     * @ensure isOnHost() : "Queries are only decoded on hosts.";
     */
    protected Query(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient) throws InvalidEncodingException {
        super(entity, signature, recipient);
        
        if (!isOnHost()) throw new InvalidEncodingException("Queries are only decoded on hosts.");
    }
    
    
    @Override
    public abstract @Nonnull QueryReply excecute() throws PacketException, SQLException;
    
}
