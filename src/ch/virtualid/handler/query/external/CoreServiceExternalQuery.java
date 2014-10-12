package ch.virtualid.handler.query.external;

import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.handler.ExternalQuery;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.Identifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.CoreService;
import ch.xdf.SignatureWrapper;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.handler.reply.query.CoreServiceQueryReply;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models the {@link ExternalQuery external queries} of the {@link CoreService core service}.
 * 
 * @invariant getSubject().getHostIdentifier().equals(getRecipient()) : "The host of the subject and the recipient are the same for external queries of the core service.");
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class CoreServiceExternalQuery extends ExternalQuery {
    
    /**
     * Creates an external query that encodes the content of a packet about the given subject.
     * 
     * @param role the role to which this handler belongs.
     * @param subject the subject of this handler.
     */
    protected CoreServiceExternalQuery(@Nullable Role role, @Nonnull Identifier subject) {
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
    public final @Nonnull SemanticType getService() {
        return CoreService.TYPE;
    }
    
    
    @Pure
    @Override
    public abstract @Nonnull Class<? extends CoreServiceQueryReply> getReplyClass();
    
}
