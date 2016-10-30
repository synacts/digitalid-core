package net.digitalid.core.service.handler;

import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.logging.exceptions.ExternalException;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.agent.FreezableAgentPermissions;
import net.digitalid.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.core.agent.Restrictions;
import net.digitalid.core.cache.Cache;
import net.digitalid.core.conversion.wrappers.signature.CredentialsSignatureWrapper;
import net.digitalid.core.conversion.wrappers.signature.SignatureWrapper;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.Role;
import net.digitalid.core.handler.InternalQuery;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.packet.exceptions.RequestErrorCode;
import net.digitalid.core.packet.exceptions.RequestException;
import net.digitalid.core.service.CoreService;
import net.digitalid.core.state.Service;

import net.digitalid.service.core.cryptography.PublicKey;

/**
 * This class models the {@link InternalQuery internal queries} of the {@link CoreService core service}.
 * 
 * @invariant getSubject().getHostIdentifier().equals(getRecipient()) : "The host of the subject has to match the recipient for internal queries of the core service.";
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
    @NonCommitting
    protected CoreServiceInternalQuery(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient) throws ExternalException {
        super(entity, signature, recipient);
        
        if (!getSubject().getHostIdentifier().equals(getRecipient())) { throw RequestException.get(RequestErrorCode.IDENTIFIER, "The host of the subject has to match the recipient for internal queries of the core service."); }
        
        this.publicKey = Cache.getPublicKey(getRecipient(), signature.getNonNullableTime());
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
    @NonCommitting
    protected abstract @Nonnull CoreServiceQueryReply executeOnHost(@Nonnull Agent agent) throws DatabaseException;
    
    @Override
    @NonCommitting
    public @Nonnull CoreServiceQueryReply executeOnHost() throws RequestException, SQLException {
        final @Nonnull SignatureWrapper signature = getSignatureNotNull();
        if (isLodged() && signature instanceof CredentialsSignatureWrapper) { ((CredentialsSignatureWrapper) signature).checkIsLogded(); }
        final @Nonnull Agent agent = signature.getAgentCheckedAndRestricted(getNonHostAccount(), publicKey);
        
        final @Nonnull ReadOnlyAgentPermissions permissions = getRequiredPermissionsToExecuteMethod();
        if (!permissions.equals(FreezableAgentPermissions.NONE)) { agent.getPermissions().checkCover(permissions); }
        
        final @Nonnull Restrictions restrictions = getRequiredRestrictionsToExecuteMethod();
        if (!restrictions.equals(Restrictions.MIN)) { agent.getRestrictions().checkCover(restrictions); }
        
        return executeOnHost(agent);
    }
    
}
