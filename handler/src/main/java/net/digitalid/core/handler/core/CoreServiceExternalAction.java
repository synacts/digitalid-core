package net.digitalid.core.handler.core;

import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.validation.annotations.method.Pure;

import net.digitalid.database.core.annotations.NonCommitting;

import net.digitalid.core.service.CoreService;

import net.digitalid.core.conversion.wrappers.signature.SignatureWrapper;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;

import net.digitalid.service.core.exceptions.external.encoding.InvalidParameterValueCombinationException;

import net.digitalid.core.exceptions.RequestException;

import net.digitalid.core.handler.ExternalAction;

import net.digitalid.core.identifier.HostIdentifier;

import net.digitalid.core.identity.InternalIdentity;

import net.digitalid.core.state.Service;

/**
 * This class models the {@link ExternalAction external actions} of the {@link CoreService core service}.
 * 
 * @invariant getSubject().getHostIdentifier().equals(getRecipient()) : "The host of the subject and the recipient are the same for external actions of the core service.");
 */
public abstract class CoreServiceExternalAction extends ExternalAction {
    
    /**
     * Creates an external action that encodes the content of a packet about the given subject.
     * 
     * @param entity the entity to which this handler belongs.
     * @param subject the subject of this handler.
     * 
     * @require !(entity instanceof Account) || canBeSentByHosts() : "Methods encoded on hosts can be sent by hosts.";
     * @require !(entity instanceof Role) || !canOnlyBeSentByHosts() : "Methods encoded on clients cannot only be sent by hosts.";
     */
    protected CoreServiceExternalAction(@Nonnull NonHostEntity entity, @Nonnull InternalIdentity subject) {
        super(entity, subject.getAddress(), subject.getAddress().getHostIdentifier());
    }
    
    /**
     * Creates an external action that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param recipient the recipient of this method.
     * 
     * @require signature.hasSubject() : "The signature has a subject.";
     * 
     * @ensure hasSignature() : "This handler has a signature.";
     */
    protected CoreServiceExternalAction(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient) throws InvalidParameterValueCombinationException {
        super(entity, signature, recipient);
        
        if (!getSubject().getHostIdentifier().equals(getRecipient())) { throw InvalidParameterValueCombinationException.get("The host of the subject and the recipient have to be the same for external actions of the core service."); }
    }
    
    
    @Pure
    @Override
    public final @Nonnull Service getService() {
        return CoreService.SERVICE;
    }
    
    
    @Pure
    @Override
    public boolean canBeSentByHosts() {
        return true;
    }
    
    @Pure
    @Override
    public boolean canOnlyBeSentByHosts() {
        return true;
    }
    
    
    @Override
    @NonCommitting
    public abstract @Nullable CoreServiceActionReply executeOnHost() throws RequestException, SQLException;
    
}
