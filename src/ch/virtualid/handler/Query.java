package ch.virtualid.handler;

import ch.virtualid.annotations.NonCommitting;
import ch.virtualid.annotations.OnlyForHosts;
import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identifier.InternalIdentifier;
import ch.xdf.SignatureWrapper;
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
 * @version 1.0
 */
public abstract class Query extends Method {
    
    /**
     * Creates a query that encodes the content of a packet for the given recipient about the given subject.
     * 
     * @param role the role to which this handler belongs.
     * @param subject the subject of this handler.
     * @param recipient the recipient of this method.
     */
    protected Query(@Nullable Role role, @Nonnull InternalIdentifier subject, @Nonnull HostIdentifier recipient) {
        super(role, subject, recipient);
    }
    
    /**
     * Creates a query that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler.
     * @param recipient the recipient of this method.
     * 
     * @require signature.hasSubject() : "The signature has a subject.";
     * 
     * @ensure hasEntity() : "This method has an entity.";
     * @ensure hasSignature() : "This handler has a signature.";
     * @ensure isOnHost() : "Queries are only decoded on hosts.";
     */
    protected Query(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient) throws InvalidEncodingException {
        super(entity, signature, recipient);
        
        if (!isOnHost()) throw new InvalidEncodingException("Queries are only decoded on hosts.");
    }
    
    
    @Pure
    @Override
    public boolean isLodged() {
        return false;
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
    @OnlyForHosts
    @NonCommitting
    public abstract @Nonnull QueryReply executeOnHost() throws PacketException, SQLException;
    
}
