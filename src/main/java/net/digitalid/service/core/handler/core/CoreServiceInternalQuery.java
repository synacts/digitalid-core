package net.digitalid.service.core.handler.core;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.wrappers.CredentialsSignatureWrapper;
import net.digitalid.service.core.block.wrappers.SignatureWrapper;
import net.digitalid.service.core.cache.Cache;
import net.digitalid.service.core.concepts.agent.Agent;
import net.digitalid.service.core.concepts.agent.FreezableAgentPermissions;
import net.digitalid.service.core.concepts.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.concepts.agent.Restrictions;
import net.digitalid.service.core.cryptography.PublicKey;
import net.digitalid.service.core.storage.Service;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.entity.Role;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketErrorCode;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.handler.InternalQuery;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.service.CoreService;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;

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
    protected CoreServiceInternalQuery(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient) throws AbortException, PacketException, ExternalException, NetworkException {
        super(entity, signature, recipient);
        
        if (!getSubject().getHostIdentifier().equals(getRecipient())) throw new PacketException(PacketErrorCode.IDENTIFIER, "The host of the subject has to match the recipient for internal queries of the core service.");
        
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
    protected abstract @Nonnull CoreServiceQueryReply executeOnHost(@Nonnull Agent agent) throws AbortException;
    
    @Override
    @NonCommitting
    public @Nonnull CoreServiceQueryReply executeOnHost() throws PacketException, SQLException {
        final @Nonnull SignatureWrapper signature = getSignatureNotNull();
        if (isLodged() && signature instanceof CredentialsSignatureWrapper) ((CredentialsSignatureWrapper) signature).checkIsLogded();
        final @Nonnull Agent agent = signature.getAgentCheckedAndRestricted(getNonHostAccount(), publicKey);
        
        final @Nonnull ReadOnlyAgentPermissions permissions = getRequiredPermissionsToExecuteMethod();
        if (!permissions.equals(FreezableAgentPermissions.NONE)) agent.getPermissions().checkCover(permissions);
        
        final @Nonnull Restrictions restrictions = getRequiredRestrictionsToExecuteMethod();
        if (!restrictions.equals(Restrictions.MIN)) agent.getRestrictions().checkCover(restrictions);
        
        return executeOnHost(agent);
    }
    
}
