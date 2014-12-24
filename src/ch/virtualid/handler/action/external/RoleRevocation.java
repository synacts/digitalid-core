package ch.virtualid.handler.action.external;

import ch.virtualid.agent.OutgoingRole;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.NonNativeRole;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.ActionReply;
import ch.virtualid.handler.Method;
import ch.virtualid.handler.reply.action.CoreServiceActionReply;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identity.IdentityClass;
import ch.virtualid.identity.InternalNonHostIdentity;
import ch.virtualid.identity.InternalPerson;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.both.Agents;
import ch.xdf.Block;
import ch.xdf.HostSignatureWrapper;
import ch.xdf.SignatureWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Revokes the given role from the given subject.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class RoleRevocation extends CoreServiceExternalAction {
    
    /**
     * Stores the semantic type {@code revocaton.role@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("revocaton.role@virtualid.ch").load(SemanticType.IDENTIFIER);
    
    
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
    public RoleRevocation(@Nonnull OutgoingRole outgoingRole, @Nonnull InternalPerson subject) throws SQLException {
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
    private RoleRevocation(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
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
    public @Nonnull String toString() {
        return "Revokes the role with the relation " + relation.getAddress() + ".";
    }
    
    
    @Pure
    @Override
    public @Nonnull Restrictions getAuditRestrictions() {
        return new Restrictions(false, true, false);
    }
    
    @Pure
    @Override
    public @Nullable OutgoingRole getFailedAuditAgent() throws SQLException {
        return Agents.getOutgoingRole(getNonHostEntity(), relation, false);
    }
    
    
    @Pure
    @Override
    public @Nullable Class<CoreServiceActionReply> getReplyClass() {
        return null;
    }
    
    /**
     * Executes this action on both hosts and clients.
     */
    private void executeOnBoth() throws SQLException {
        Agents.removeIncomingRole(getNonHostEntity(), issuer, relation);
    }
    
    @Override
    public @Nullable ActionReply executeOnHost() throws PacketException, SQLException {
        if (getSignatureNotNull().isNotSigned()) throw new PacketException(PacketError.AUTHORIZATION, "The revocation of a role has to be signed.");
        executeOnBoth();
        return null;
    }
    
    @Override
    public void executeOnClient() throws SQLException {
        executeOnBoth();
        for (final @Nonnull NonNativeRole role : getRole().getRoles()) {
            if (role.getIssuer().equals(issuer) && relation.equals(role.getRelation())) role.remove();
        }
    }
    
    @Override
    public void executeOnFailure() throws SQLException {
        // TODO: Add this role issuance to a list of failed external actions.
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    /**
     * The factory class for the surrounding method.
     */
    private static final class Factory extends Method.Factory {
        
        static { Method.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
            return new RoleRevocation(entity, signature, recipient, block);
        }
        
    }
    
}
