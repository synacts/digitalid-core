package ch.virtualid.handler.action.internal;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.InvalidDeclarationException;
import ch.virtualid.handler.InternalAction;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.CoreService;
import ch.virtualid.packet.PacketException;
import ch.xdf.SignatureWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models the {@link InternalAction internal actions} of the {@link CoreService core service}.
 * 
 * @invariant getEntityNotNull().getIdentity().getAddress().getHostIdentifier().equals(getRecipient()) : "The host of the entity and the recipient are the same for internal actions of the core service.");
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class CoreServiceInternalAction extends InternalAction {
    
    /**
     * Creates an internal action that encodes the content of a packet.
     * 
     * @param role the role to which this handler belongs.
     */
    protected CoreServiceInternalAction(@Nonnull Role role) {
        super(role, role.getIdentity().getAddress().getHostIdentifier());
    }
    
    /**
     * Creates an internal action that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param recipient the recipient of this method.
     * 
     * @require signature.getSubject() != null : "The subject of the signature is not null.";
     * 
     * @ensure getSignature() != null : "The signature of this handler is not null.";
     */
    protected CoreServiceInternalAction(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient) throws InvalidEncodingException, SQLException, FailedIdentityException, InvalidDeclarationException {
        super(entity, signature, recipient);
        
        if (!getEntityNotNull().getIdentity().getAddress().getHostIdentifier().equals(getRecipient())) throw new InvalidEncodingException("The host of the entity and the recipient have to be the same for internal actions of the core service.");
    }
    
    
    @Pure
    @Override
    public final @Nonnull SemanticType getService() {
        return CoreService.TYPE;
    }
    
    
    @Pure
    @Override
    public @Nonnull ReadonlyAgentPermissions getRequiredPermissions() {
        return AgentPermissions.NONE;
    }
    
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictions() {
        return Restrictions.NONE;
    }
    
    
    /**
     * Returns the agent required for this internal action of the core service.
     * 
     * @return the agent required for this internal action of the core service.
     */
    @Pure
    public @Nullable Agent getRequiredAgent() {
        return null;
    }
    
    
    /**
     * Executes this internal action on both the host and client.
     */
    protected abstract void executeOnBoth() throws SQLException;
    
    @Override
    public void executeOnHostInternalAction() throws PacketException, SQLException {
        assert isOnHost() : "This method is called on a host.";
        assert getSignature() != null : "The signature of this handler is not null.";
        
        final @Nonnull Agent agent = getSignatureNotNull().getAgent();
        
        final @Nonnull ReadonlyAgentPermissions permissions = getRequiredPermissions();
        if (!permissions.equals(AgentPermissions.NONE)) agent.getPermissions().checkCover(permissions);
        
        final @Nonnull Restrictions restrictions = getRequiredRestrictions();
        if (!restrictions.equals(Restrictions.NONE)) agent.getRestrictions().checkCover(restrictions);
        
        final @Nullable Agent other = getRequiredAgent();
        if (other != null) agent.checkCovers(other);
        
        executeOnBoth();
    }
    
    @Override
    public void executeOnClient() throws SQLException {
        assert isOnClient() : "This method is called on a client.";
        
        executeOnBoth();
    }
    
}
