package ch.virtualid.handler;

import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
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
     * @param role the role to which this handler belongs.
     * @param subject the subject of this handler.
     * @param recipient the recipient of this method.
     */
    protected Query(@Nullable Role role, @Nonnull Identifier subject, @Nonnull HostIdentifier recipient) {
        super(role, subject, recipient);
    }
    
    /**
     * Creates a query that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler.
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
    
    
    @Pure
    @Override
    public final boolean canBeSentByHosts() {
        return false;
    }
    
    @Pure
    @Override
    public final boolean canOnlyBeSentByHosts() {
        return false;
    }
    
    
    @Override
    public abstract @Nonnull QueryReply executeOnHost() throws PacketException, SQLException;
    
}
