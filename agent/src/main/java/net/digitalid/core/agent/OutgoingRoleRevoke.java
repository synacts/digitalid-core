package net.digitalid.core.agent;

import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.method.Pure;

import net.digitalid.database.core.annotations.NonCommitting;

import net.digitalid.core.conversion.Block;

import net.digitalid.core.conversion.wrappers.signature.HostSignatureWrapper;
import net.digitalid.core.conversion.wrappers.signature.SignatureWrapper;

import net.digitalid.service.core.dataservice.StateModule;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonNativeRole;
import net.digitalid.core.exceptions.RequestErrorCode;
import net.digitalid.core.exceptions.RequestException;

import net.digitalid.core.handler.Method;
import net.digitalid.core.handler.Reply;

import net.digitalid.core.handler.core.CoreServiceActionReply;
import net.digitalid.core.handler.core.CoreServiceExternalAction;

import net.digitalid.core.identifier.HostIdentifier;

import net.digitalid.core.identity.IdentityImplementation;
import net.digitalid.core.identity.InternalNonHostIdentity;
import net.digitalid.core.identity.InternalPerson;
import net.digitalid.core.identity.SemanticType;

/**
 * Revokes the given role from the given subject.
 */
@Immutable
final class OutgoingRoleRevoke extends CoreServiceExternalAction {
    
    /**
     * Stores the semantic type {@code revocaton.role@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.map("revocaton.role@core.digitalid.net").load(SemanticType.IDENTIFIER);
    
    
    /**
     * Stores the issuer of the role which is revoked.
     */
    private final @Nonnull InternalNonHostIdentity issuer;
    
    /**
     * Stores the relation of the role which is revoked.
     * 
     * @invariant relation.isRoleType() : "The relation is a role type.";
     */
    private final @Nonnull SemanticType relation;
    
    /**
     * Creates an external action to revoke the given role to the given subject.
     * 
     * @param outgoingRole the outgoing role which is to be revoked.
     * @param subject the subject of this external action.
     * 
     * @require outgoingRole.isOnHost() : "The outgoing role is on a host.";
     */
    @NonCommitting
    OutgoingRoleRevoke(@Nonnull OutgoingRole outgoingRole, @Nonnull InternalPerson subject) throws DatabaseException {
        super(outgoingRole.getAccount(), subject);
        
        this.issuer = outgoingRole.getAccount().getIdentity();
        this.relation = outgoingRole.getRelation();
    }
    
    /**
     * Creates an external action that decodes the given block.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param recipient the recipient of this method.
     * @param block the content which is to be decoded.
     * 
     * @require signature.hasSubject() : "The signature has a subject.";
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     * 
     * @ensure hasSignature() : "This handler has a signature.";
     */
    @NonCommitting
    private OutgoingRoleRevoke(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        super(entity, signature, recipient);
        
        if (signature instanceof HostSignatureWrapper) {
            this.issuer = signature.toHostSignatureWrapper().getSigner().getIdentity().castTo(InternalNonHostIdentity.class);
        } else {
            this.issuer = entity.getIdentity().castTo(InternalNonHostIdentity.class);
        }
        
        this.relation = IdentityImplementation.create(block).castTo(SemanticType.class).checkIsRoleType();
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return relation.toBlock().setType(TYPE);
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Revokes the role with the relation " + relation.getAddress() + ".";
    }
    
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictionsToSeeAudit() {
        return new Restrictions(false, true, false);
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nullable OutgoingRole getFailedAuditAgent() throws DatabaseException {
        return AgentModule.getOutgoingRole(getNonHostEntity(), relation, false);
    }
    
    
    /**
     * Executes this action on both hosts and clients.
     */
    @NonCommitting
    private void executeOnBoth() throws DatabaseException {
        AgentModule.removeIncomingRole(getNonHostEntity(), issuer, relation);
    }
    
    @Override
    @NonCommitting
    public @Nullable CoreServiceActionReply executeOnHost() throws RequestException, SQLException {
        if (!getSignatureNotNull().isSigned()) { throw RequestException.get(RequestErrorCode.AUTHORIZATION, "The revocation of a role has to be signed."); }
        executeOnBoth();
        return null;
    }
    
    @Pure
    @Override
    public boolean matches(@Nullable Reply reply) {
        return reply == null;
    }
    
    @Override
    @NonCommitting
    public void executeOnClient() throws DatabaseException {
        executeOnBoth();
        for (final @Nonnull NonNativeRole role : getRole().getRoles()) {
            if (role.getIssuer().equals(issuer) && relation.equals(role.getRelation())) { role.remove(); }
        }
    }
    
    @Override
    @NonCommitting
    public void executeOnFailure() throws DatabaseException {
        // TODO: Add this role issuance to a list of failed external actions.
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (protectedEquals(object) && object instanceof OutgoingRoleRevoke) {
            final @Nonnull OutgoingRoleRevoke other = (OutgoingRoleRevoke) object;
            return this.issuer.equals(other.issuer) && this.relation.equals(other.relation);
        }
        return false;
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = protectedHashCode();
        hash = 89 * hash + issuer.hashCode();
        hash = 89 * hash + relation.hashCode();
        return hash;
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull StateModule getModule() {
        return AgentModule.MODULE;
    }
    
    /**
     * The factory class for the surrounding method.
     */
    private static final class Factory extends Method.Factory {
        
        static { Method.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        @NonCommitting
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
            return new OutgoingRoleRevoke(entity, signature, recipient, block);
        }
        
    }
    
}
