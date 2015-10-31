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
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.packet.PacketErrorCode;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.handler.InternalAction;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.service.CoreService;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.annotations.OnlyForClients;
import net.digitalid.utility.database.annotations.OnlyForHosts;

/**
 * This class models the {@link InternalAction internal actions} of the {@link CoreService core service}.
 * 
 * @invariant getSubject().getHostIdentifier().equals(getRecipient()) : "The host of the subject has to match the recipient for internal actions of the core service.";
 */
@Immutable
public abstract class CoreServiceInternalAction extends InternalAction {
    
    /**
     * Stores the active public key of the recipient.
     */
    private final @Nullable PublicKey publicKey;
    
    /**
     * Creates an internal action that encodes the content of a packet.
     * 
     * @param role the role to which this handler belongs.
     */
    protected CoreServiceInternalAction(@Nonnull Role role) {
        super(role, role.getIdentity().getAddress().getHostIdentifier());
        
        this.publicKey = null;
    }
    
    /**
     * Creates an internal action that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param recipient the recipient of this method.
     * 
     * @require signature.hasSubject() : "The signature has a subject.";
     * 
     * @ensure hasSignature() : "This handler has a signature.";
     */
    @NonCommitting
    protected CoreServiceInternalAction(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient) throws AbortException, PacketException, ExternalException, NetworkException {
        super(entity, signature, recipient);
        
        if (!getSubject().getHostIdentifier().equals(getRecipient())) throw new PacketException(PacketErrorCode.IDENTIFIER, "The host of the subject has to match the recipient for internal actions of the core service.");
        
        this.publicKey = Cache.getPublicKey(getRecipient(), signature.getNonNullableTime());
    }
    
    
    @Pure
    @Override
    public final @Nonnull Service getService() {
        return CoreService.SERVICE;
    }
    
    
    /**
     * Returns the agent required for this internal action of the core service.
     * 
     * @return the agent required for this internal action of the core service.
     */
    @Pure
    public @Nullable Agent getRequiredAgentToExecuteMethod() {
        return null;
    }
    
    /**
     * Returns the active public key of the recipient.
     * This method can be overridden to prevent a key
     * rotation exception by returning the value null.
     * 
     * @return the active public key of the recipient.
     */
    @Pure
    public @Nullable PublicKey getPublicKey() {
        return publicKey;
    }
    
    
    /**
     * Executes this internal action on both the host and client.
     */
    @NonCommitting
    protected abstract void executeOnBoth() throws AbortException;
    
    @Override
    @OnlyForHosts
    @NonCommitting
    public void executeOnHostInternalAction() throws PacketException, AbortException {
        final @Nonnull SignatureWrapper signature = getSignatureNotNull();
        if (signature instanceof CredentialsSignatureWrapper) ((CredentialsSignatureWrapper) signature).checkIsLogded();
        final @Nonnull Agent agent = signature.getAgentCheckedAndRestricted(getNonHostAccount(), getPublicKey());
        
        final @Nonnull ReadOnlyAgentPermissions permissions = getRequiredPermissionsToExecuteMethod();
        if (!permissions.equals(FreezableAgentPermissions.NONE)) agent.getPermissions().checkCover(permissions);
        
        final @Nonnull Restrictions restrictions = getRequiredRestrictionsToExecuteMethod();
        if (!restrictions.equals(Restrictions.MIN)) {
            try {
                agent.getRestrictions().checkCover(restrictions);
            } catch (SQLException exception) {
               throw AbortException.get(exception);
            }
        }
        final @Nullable Agent other = getRequiredAgentToExecuteMethod();
        if (other != null) {
            try {
                agent.checkCovers(other);
            } catch (SQLException exception) {
                throw AbortException.get(exception);
            }  
        }
        
        executeOnBoth();
    }
    
    @Override
    @NonCommitting
    @OnlyForClients
    public final void executeOnClient() throws AbortException {
        executeOnBoth();
    }
    
}
