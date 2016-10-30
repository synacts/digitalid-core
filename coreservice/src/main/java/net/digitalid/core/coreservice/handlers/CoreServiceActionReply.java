package net.digitalid.core.service.handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;

import net.digitalid.core.conversion.wrappers.signature.HostSignatureWrapper;
import net.digitalid.core.entity.Account;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.handler.ActionReply;
import net.digitalid.core.service.CoreService;
import net.digitalid.core.state.Service;

/**
 * This class models the {@link ActionReply action replies} of the {@link CoreService core service}.
 */
public abstract class CoreServiceActionReply extends ActionReply {
    
    /**
     * Creates an action reply that encodes the content of a packet.
     * 
     * @param account the account to which this action reply belongs.
     */
    protected CoreServiceActionReply(@Nonnull Account account) {
        super(account);
    }
    
    /**
     * Creates an action reply that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the host signature of this handler.
     * @param number the number that references this reply.
     * 
     * @ensure hasSignature() : "This handler has a signature.";
     */
    protected CoreServiceActionReply(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number) throws InvalidEncodingException, InternalException {
        super(entity, signature, number);
    }
    
    
    @Pure
    @Override
    public final @Nonnull Service getService() {
        return CoreService.SERVICE;
    }
    
}
