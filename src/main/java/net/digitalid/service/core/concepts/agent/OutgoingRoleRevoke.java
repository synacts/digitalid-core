package net.digitalid.service.core.concepts.agent;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.HostSignatureWrapper;
import net.digitalid.service.core.block.wrappers.SignatureWrapper;
import net.digitalid.service.core.dataservice.StateModule;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.entity.NonNativeRole;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketErrorCode;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.handler.Method;
import net.digitalid.service.core.handler.Reply;
import net.digitalid.service.core.handler.core.CoreServiceActionReply;
import net.digitalid.service.core.handler.core.CoreServiceExternalAction;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.identity.IdentityClass;
import net.digitalid.service.core.identity.InternalNonHostIdentity;
import net.digitalid.service.core.identity.InternalPerson;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;

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
    OutgoingRoleRevoke(@Nonnull OutgoingRole outgoingRole, @Nonnull InternalPerson subject) throws AbortException {
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
    private OutgoingRoleRevoke(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException {
        super(entity, signature, recipient);
        
        if (signature instanceof HostSignatureWrapper) {
            this.issuer = signature.toHostSignatureWrapper().getSigner().getIdentity().toInternalNonHostIdentity();
        } else {
            this.issuer = entity.getIdentity().toInternalNonHostIdentity();
        }
        
        this.relation = IdentityClass.create(block).toSemanticType().checkIsRoleType();
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
    public @Nullable OutgoingRole getFailedAuditAgent() throws AbortException {
        return AgentModule.getOutgoingRole(getNonHostEntity(), relation, false);
    }
    
    
    /**
     * Executes this action on both hosts and clients.
     */
    @NonCommitting
    private void executeOnBoth() throws AbortException {
        AgentModule.removeIncomingRole(getNonHostEntity(), issuer, relation);
    }
    
    @Override
    @NonCommitting
    public @Nullable CoreServiceActionReply executeOnHost() throws PacketException, SQLException {
        if (!getSignatureNotNull().isSigned()) throw new PacketException(PacketErrorCode.AUTHORIZATION, "The revocation of a role has to be signed.");
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
    public void executeOnClient() throws AbortException {
        executeOnBoth();
        for (final @Nonnull NonNativeRole role : getRole().getRoles()) {
            if (role.getIssuer().equals(issuer) && relation.equals(role.getRelation())) role.remove();
        }
    }
    
    @Override
    @NonCommitting
    public void executeOnFailure() throws AbortException {
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
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException {
            return new OutgoingRoleRevoke(entity, signature, recipient, block);
        }
        
    }
    
}
