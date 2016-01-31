package net.digitalid.core.handler.core;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.validation.annotations.method.Pure;

import net.digitalid.core.service.CoreService;

import net.digitalid.core.conversion.wrappers.signature.HostSignatureWrapper;

import net.digitalid.core.entity.Account;
import net.digitalid.core.entity.NonHostEntity;

import net.digitalid.core.handler.QueryReply;

import net.digitalid.core.identifier.InternalIdentifier;

import net.digitalid.core.resolution.IdentityReply;

import net.digitalid.core.state.Service;

/**
 * This class models the {@link QueryReply query replies} of the {@link CoreService core service}.
 */
public abstract class CoreServiceQueryReply extends QueryReply {
    
    /**
     * Creates a query reply that encodes the content of a packet.
     * 
     * @param account the account to which this query reply belongs.
     */
    protected CoreServiceQueryReply(@Nonnull Account account) {
        super(account);
    }
    
    /**
     * Creates a query reply that encodes the content of a packet.
     * This constructor is only needed for {@link IdentityReply}.
     * 
     * @param subject the subject of this handler.
     */
    protected CoreServiceQueryReply(@Nonnull InternalIdentifier subject) {
        super(subject);
    }
    
    /**
     * Creates a query reply that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the host signature of this handler.
     * @param number the number that references this reply.
     * 
     * @ensure hasSignature() : "This handler has a signature.";
     * @ensure !isOnHost() : "Query replies are never decoded on hosts.";
     */
    protected CoreServiceQueryReply(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number) throws InvalidEncodingException, InternalException {
        super(entity, signature, number);
    }
    
    
    @Pure
    @Override
    public final @Nonnull Service getService() {
        return CoreService.SERVICE;
    }
    
}
