package net.digitalid.core.property;

import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.exceptions.external.ExternalException;
import net.digitalid.utility.exceptions.internal.InternalException;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;

import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.service.core.auxiliary.Time;

import net.digitalid.core.conversion.wrappers.signature.CredentialsSignatureWrapper;
import net.digitalid.core.conversion.wrappers.signature.SignatureWrapper;
import net.digitalid.core.cache.Cache;

import net.digitalid.core.concept.Concept;

import net.digitalid.service.core.concepts.agent.Agent;
import net.digitalid.service.core.concepts.agent.FreezableAgentPermissions;
import net.digitalid.service.core.concepts.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.concepts.agent.Restrictions;
import net.digitalid.service.core.cryptography.PublicKey;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.entity.Role;

import net.digitalid.core.exceptions.NetworkException;
import net.digitalid.core.exceptions.RequestException;

import net.digitalid.service.core.handler.InternalAction;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.site.annotations.Clients;
import net.digitalid.service.core.site.annotations.Hosts;
import net.digitalid.service.core.storage.Service;

/**
 * Description.
 */
@Immutable
public abstract class ConceptPropertyInternalAction<V, C extends Concept<C, E, ?>, E extends Entity> extends InternalAction {
    
    /* -------------------------------------------------- Types -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code old.time@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType OLD_TIME = SemanticType.map("old.time@core.digitalid.net").load(Time.TYPE);
    
    /**
     * Stores the semantic type {@code new.time@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType NEW_TIME = SemanticType.map("new.time@core.digitalid.net").load(Time.TYPE);
    
    /* -------------------------------------------------- Property -------------------------------------------------- */
    
    @Pure
    public abstract @Nonnull ConceptProperty<V, C, E> getProperty();
    
    /* -------------------------------------------------- Service -------------------------------------------------- */
    
    private final @Nonnull Service service;
    
    @Pure
    @Override
    public final @Nonnull Service getService() {
        return service;
    }
    
    /* -------------------------------------------------- Public Key -------------------------------------------------- */

    /**
     * Stores the active public key of the recipient.
     */
    private final @Nullable PublicKey publicKey;

    /**
     * Returns the active public key of the recipient.
     * 
     * @return the active public key of the recipient.
     */
    @Pure
    public final @Nullable PublicKey getPublicKey() {
        return publicKey;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    @Clients
    protected ConceptPropertyInternalAction(@Nonnull Role role, @Nonnull Service service) throws DatabaseException {
        super(role, service.getRecipient(role));
        
        this.service = service;
        // TODO: time?
        this.publicKey = null;
    }
    
    protected ConceptPropertyInternalAction(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Service service) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        super(entity, signature, recipient);
        
        this.service = service;
        this.publicKey = Cache.getPublicKey(getRecipient(), signature.getNonNullableTime());
    }
    
    /* -------------------------------------------------- Executing -------------------------------------------------- */
    
    /**
     * Executes this internal action on both the host and client.
     */
    @NonCommitting
    protected abstract void executeOnBoth() throws DatabaseException;
    
    @Override
    @Hosts
    @NonCommitting
    // TODO: Must be adapted to work for non-core service internal actions.
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
    
    @Override
    @NonCommitting
    @Clients
    public final void executeOnClient() throws DatabaseException {
        executeOnBoth();
    }
    
    /* -------------------------------------------------- Requirements -------------------------------------------------- */
    
    /**
     * Returns the agent required for this internal action of the core service.
     * 
     * @return the agent required for this internal action of the core service.
     */
    @Pure
    public @Nullable abstract Agent getRequiredAgentToExecuteMethod();
    
}
