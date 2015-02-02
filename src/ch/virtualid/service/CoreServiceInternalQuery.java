package ch.virtualid.service;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.DoesNotCommit;
import ch.virtualid.annotations.Pure;
import ch.virtualid.cache.Cache;
import ch.virtualid.cryptography.PublicKey;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.InternalQuery;
import ch.virtualid.identifier.HostIdentifier;
import ch.xdf.CredentialsSignatureWrapper;
import ch.xdf.SignatureWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models the {@link InternalQuery internal queries} of the {@link CoreService core service}.
 * 
 * @invariant getSubject().getHostIdentifier().equals(getRecipient()) : "The host of the subject has to match the recipient for internal queries of the core service.";
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public abstract class CoreServiceInternalQuery extends InternalQuery {
    
    /**
     * Stores the active public key of the recipient.
     */
    private final @Nullable PublicKey publicKey;
    
    /**
     * Creates an internal query that encodes the content of a packet.
     * 
     * @param role the role to which this handler belongs.
     */
    protected CoreServiceInternalQuery(@Nonnull Role role) {
        super(role, role.getIdentity().getAddress().getHostIdentifier());
        
        this.publicKey = null;
    }
    
    /**
     * Creates an internal query that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler.
     * @param recipient the recipient of this method.
     * 
     * @require signature.hasSubject() : "The signature has a subject.";
     * 
     * @ensure hasSignature() : "This handler has a signature.";
     * @ensure isOnHost() : "Queries are only decoded on hosts.";
     */
    @DoesNotCommit
    protected CoreServiceInternalQuery(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        if (!getSubject().getHostIdentifier().equals(getRecipient())) throw new PacketException(PacketError.IDENTIFIER, "The host of the subject has to match the recipient for internal queries of the core service.");
        
        this.publicKey = Cache.getPublicKey(getRecipient(), signature.getTimeNotNull());
    }
    
    
    @Pure
    @Override
    public final @Nonnull Service getService() {
        return CoreService.SERVICE;
    }
    
    
    /**
     * Executes this internal query on the host.
     * 
     * @param agent the agent that signed the query.
     * 
     * @return the reply to this internal query.
     * 
     * @require isOnHost() : "This method is called on a host.";
     * @require hasSignature() : "This handler has a signature.";
     */
    @DoesNotCommit
    protected abstract @Nonnull CoreServiceQueryReply executeOnHost(@Nonnull Agent agent) throws SQLException;
    
    @Override
    @DoesNotCommit
    public @Nonnull CoreServiceQueryReply executeOnHost() throws PacketException, SQLException {
        final @Nonnull SignatureWrapper signature = getSignatureNotNull();
        if (isLodged() && signature instanceof CredentialsSignatureWrapper) ((CredentialsSignatureWrapper) signature).checkIsLogded();
        final @Nonnull Agent agent = signature.getAgentCheckedAndRestricted(getNonHostAccount(), publicKey);
        
        final @Nonnull ReadonlyAgentPermissions permissions = getRequiredPermissions();
        if (!permissions.equals(AgentPermissions.NONE)) agent.getPermissions().checkCover(permissions);
        
        final @Nonnull Restrictions restrictions = getRequiredRestrictions();
        if (!restrictions.equals(Restrictions.MIN)) agent.getRestrictions().checkCover(restrictions);
        
        return executeOnHost(agent);
    }
    
}
