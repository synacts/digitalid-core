package ch.virtualid.handler.query.internal;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.InvalidDeclarationException;
import ch.virtualid.handler.InternalQuery;
import ch.virtualid.handler.QueryReply;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.CoreService;
import ch.virtualid.packet.PacketException;
import ch.xdf.SignatureWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This class models the {@link InternalQuery internal queries} of the {@link CoreService core service}.
 * 
 * @invariant getEntityNotNull().getIdentity().getAddress().getHostIdentifier().equals(getRecipient()) : "The host of the entity and the recipient are the same for internal queries of the core service.");
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class CoreServiceInternalQuery extends InternalQuery {
    
    /**
     * Creates an internal query that encodes the content of a packet.
     * 
     * @param role the role to which this handler belongs.
     */
    protected CoreServiceInternalQuery(@Nonnull Role role) {
        super(role, role.getIdentity().getAddress().getHostIdentifier());
    }
    
    /**
     * Creates an internal query that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler.
     * @param recipient the recipient of this method.
     * 
     * @require signature.getSubject() != null : "The subject of the signature is not null.";
     * 
     * @ensure getSignature() != null : "The signature of this handler is not null.";
     * @ensure isOnHost() : "Queries are only decoded on hosts.";
     */
    protected CoreServiceInternalQuery(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient) throws InvalidEncodingException, SQLException, FailedIdentityException, InvalidDeclarationException {
        super(entity, signature, recipient);
        
        if (!getEntityNotNull().getIdentity().getAddress().getHostIdentifier().equals(getRecipient())) throw new InvalidEncodingException("The host of the entity and the recipient have to be the same for internal queries of the core service.");
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
     * Executes this internal query on the host.
     * 
     * @param agent the agent that signed the query.
     * 
     * @return the reply to this internal query.
     * 
     * @require isOnHost() : "This method is called on a host.";
     * @require getSignature() != null : "The signature of this handler is not null.";
     */
    protected abstract @Nonnull QueryReply executeOnHost(@Nonnull Agent agent) throws SQLException;
    
    @Override
    public @Nonnull QueryReply executeOnHost() throws PacketException, SQLException {
        assert isOnHost() : "This method is called on a host.";
        assert getSignature() != null : "The signature of this handler is not null.";
        
        final @Nonnull Agent agent = getSignatureNotNull().getAgentCheckedAndRestricted(getEntityNotNull());
        
        final @Nonnull ReadonlyAgentPermissions permissions = getRequiredPermissions();
        if (!permissions.equals(AgentPermissions.NONE)) agent.getPermissions().checkCover(permissions);
        
        final @Nonnull Restrictions restrictions = getRequiredRestrictions();
        if (!restrictions.equals(Restrictions.NONE)) agent.getRestrictions().checkCover(restrictions);
        
        return executeOnHost(agent);
    }
    
}
