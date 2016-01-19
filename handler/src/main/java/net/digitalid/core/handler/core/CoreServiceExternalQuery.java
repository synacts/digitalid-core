package net.digitalid.core.handler.core;

import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.validation.state.Pure;

import net.digitalid.database.core.annotations.NonCommitting;

import net.digitalid.core.service.CoreService;

import net.digitalid.core.conversion.wrappers.signature.SignatureWrapper;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.Role;

import net.digitalid.service.core.exceptions.external.encoding.InvalidParameterValueCombinationException;

import net.digitalid.core.exceptions.RequestException;

import net.digitalid.core.handler.ExternalQuery;

import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identifier.InternalIdentifier;

import net.digitalid.core.state.Service;

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
    protected CoreServiceExternalQuery(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient) throws InvalidParameterValueCombinationException {
        super(entity, signature, recipient);
        
        if (!getSubject().getHostIdentifier().equals(getRecipient())) { throw InvalidParameterValueCombinationException.get("The host of the subject and the recipient have to be the same for external queries of the core service."); }
    }
    
    
    @Pure
    @Override
    public final @Nonnull Service getService() {
        return CoreService.SERVICE;
    }
    
    
    @Override
    @NonCommitting
    public abstract @Nonnull CoreServiceQueryReply executeOnHost() throws RequestException, SQLException;
    
}
