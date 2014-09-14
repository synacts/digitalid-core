package ch.virtualid.handler.action.internal;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.concept.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.entity.ClientEntity;
import ch.virtualid.entity.Entity;
import ch.virtualid.handler.InternalAction;
import ch.virtualid.handler.Method;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.SemanticType;
import ch.xdf.Block;
import ch.xdf.SignatureWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public abstract class CoreServiceInternalAction extends InternalAction {
    
    /**
     * Creates an internal action of the core service that decodes the given signature and block for the given entity.
     * 
     * @param connection an open connection to the database.
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param block the element of the content.
     * @param recipient the recipient of this handler.
     * 
     * @require !connection.isOnBoth() : "The decoding of sendable handlers is site-specific.";
     * @require !connection.isOnClient() || entity instanceof Role : "On the client-side, the entity is a role.";
     * @require !connection.isOnHost() || entity instanceof Identity : "On the host-side, the entity is an identity.";
     * @require signature.getSubject() != null : "The subject of the signature is not null.";
     * 
     * @ensure getEntity() != null : "The entity of this handler is not null.";
     * @ensure getSignature() != null : "The signature of this handler is not null.";
     * @ensure (!getSubject().getIdentity().equals(entity.getIdentity())) : "The identity of the entity and the subject are the same.";
     */
    protected CoreServiceInternalAction(@Nonnull Entity connection, @Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull Block block, @Nonnull HostIdentifier recipient) throws InvalidEncodingException, FailedIdentityException {
        super(connection, entity, signature, block, recipient);
    }
    
    /**
     * Creates an internal action of the core service that encodes the content of a packet to the given recipient about the given subject.
     * 
     * @param connection an open connection to the database.
     * @param role the role to which this handler belongs.
     * 
     * @ensure getEntity() != null : "The entity of this handler is not null.";
     */
    protected CoreServiceInternalAction(@Nonnull ClientEntity connection, @Nonnull Role role) {
        super(connection, role, role.getIdentity().getAddress().getHostIdentifier());
    }
    
    
    @Override
    public final @Nonnull SemanticType getService() {
        return SemanticType.CORE_SERVICE;
    }
    
    
    @Override
    public final boolean isSimilarTo(@Nonnull Method other) {
        return super.isSimilarTo(other) && other instanceof CoreServiceInternalAction;
    }
    
    
    @Override
    public @Nonnull AgentPermissions getRequiredPermissions() {
        return AgentPermissions.NONE;
    }
    
    
    public @Nullable Agent getRequiredAuthorization() {
        return null;
    }
    
    public abstract void executeOnHost(@Nonnull Agent agent);
    
}
