package net.digitalid.service.core.handler.core;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.wrappers.SignatureWrapper;
import net.digitalid.service.core.storage.Service;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.entity.Role;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.handler.ExternalQuery;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.identifier.InternalIdentifier;
import net.digitalid.service.core.service.CoreService;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * This class models the {@link ExternalQuery external queries} of the {@link CoreService core service}.
 * 
 * @invariant getSubject().getHostIdentifier().equals(getRecipient()) : "The host of the subject and the recipient are the same for external queries of the core service.");
 */
public abstract class CoreServiceExternalQuery extends ExternalQuery {
    
    /**
     * Creates an external query that encodes the content of a packet about the given subject.
     * 
     * @param role the role to which this handler belongs.
     * @param subject the subject of this handler.
     */
    protected CoreServiceExternalQuery(@Nullable Role role, @Nonnull InternalIdentifier subject) {
        super(role, subject, subject.getHostIdentifier());
    }
    
    /**
     * Creates an external query that decodes a packet with the given signature for the given entity.
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
    protected CoreServiceExternalQuery(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient) throws InvalidEncodingException {
        super(entity, signature, recipient);
        
        if (!getSubject().getHostIdentifier().equals(getRecipient())) throw new InvalidEncodingException("The host of the subject and the recipient have to be the same for external queries of the core service.");
    }
    
    
    @Pure
    @Override
    public final @Nonnull Service getService() {
        return CoreService.SERVICE;
    }
    
    
    @Override
    @NonCommitting
    public abstract @Nonnull CoreServiceQueryReply executeOnHost() throws PacketException, SQLException;
    
}
