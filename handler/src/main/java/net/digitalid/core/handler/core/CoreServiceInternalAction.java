package net.digitalid.core.handler.core;

import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.method.Pure;

import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.service.CoreService;

import net.digitalid.core.conversion.wrappers.signature.CredentialsSignatureWrapper;
import net.digitalid.core.conversion.wrappers.signature.SignatureWrapper;

import net.digitalid.core.cache.Cache;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.agent.FreezableAgentPermissions;
import net.digitalid.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.core.agent.Restrictions;

import net.digitalid.service.core.cryptography.PublicKey;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.Role;
import net.digitalid.core.exceptions.NetworkException;
import net.digitalid.core.exceptions.RequestErrorCode;
import net.digitalid.core.exceptions.RequestException;

import net.digitalid.core.handler.InternalAction;

import net.digitalid.core.identifier.HostIdentifier;

import net.digitalid.core.client.annotations.Clients;
import net.digitalid.core.host.annotations.Hosts;

import net.digitalid.core.state.Service;

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
    protected CoreServiceInternalAction(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        super(entity, signature, recipient);
        
        if (!getSubject().getHostIdentifier().equals(getRecipient())) { throw RequestException.get(RequestErrorCode.IDENTIFIER, "The host of the subject has to match the recipient for internal actions of the core service."); }
        
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
    protected abstract void executeOnBoth() throws DatabaseException;
    
    @Hosts
    @Override
    @NonCommitting
    public void executeOnHostInternalAction() throws RequestException, DatabaseException {
        final @Nonnull SignatureWrapper signature = getSignatureNotNull();
        if (signature instanceof CredentialsSignatureWrapper) { ((CredentialsSignatureWrapper) signature).checkIsLogded(); }
        final @Nonnull Agent agent = signature.getAgentCheckedAndRestricted(getNonHostAccount(), getPublicKey());
        
        final @Nonnull ReadOnlyAgentPermissions permissions = getRequiredPermissionsToExecuteMethod();
        if (!permissions.equals(FreezableAgentPermissions.NONE)) { agent.getPermissions().checkCover(permissions); }
        
        final @Nonnull Restrictions restrictions = getRequiredRestrictionsToExecuteMethod();
        if (!restrictions.equals(Restrictions.MIN)) {
            try {
                agent.getRestrictions().checkCover(restrictions);
            } catch (SQLException exception) {
               throw DatabaseException.get(exception);
            }
        }
        final @Nullable Agent other = getRequiredAgentToExecuteMethod();
        if (other != null) {
            try {
                agent.checkCovers(other);
            } catch (SQLException exception) {
                throw DatabaseException.get(exception);
            }  
        }
        
        executeOnBoth();
    }
    
    @Clients
    @Override
    @NonCommitting
    public final void executeOnClient() throws DatabaseException {
        executeOnBoth();
    }
    
}
