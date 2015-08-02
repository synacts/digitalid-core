package net.digitalid.core.agent;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.ReadOnlyArray;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketError;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.handler.Method;
import net.digitalid.core.handler.Reply;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identity.IdentityClass;
import net.digitalid.core.identity.InternalNonHostIdentity;
import net.digitalid.core.identity.InternalPerson;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.module.BothModule;
import net.digitalid.core.service.CoreServiceActionReply;
import net.digitalid.core.service.CoreServiceExternalAction;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.HostSignatureWrapper;
import net.digitalid.core.wrappers.Int64Wrapper;
import net.digitalid.core.wrappers.SignatureWrapper;
import net.digitalid.core.wrappers.TupleWrapper;

/**
 * Issues the given role to the given subject.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
final class OutgoingRoleIssue extends CoreServiceExternalAction {
    
    /**
     * Stores the semantic type {@code issuance.role@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.map("issuance.role@core.digitalid.net").load(TupleWrapper.TYPE, SemanticType.IDENTIFIER, Agent.NUMBER);
    
    
    /**
     * Stores the issuer of the role which is issued.
     */
    private final @Nonnull InternalNonHostIdentity issuer;
    
    /**
     * Stores the relation of the role which is issued.
     * 
     * @invariant relation.isRoleType() : "The relation is a role type.";
     */
    private final @Nonnull SemanticType relation;
    
    /**
     * Stores the agent number of the role which is issued.
     */
    private final long agentNumber;
    
    /**
     * Creates an external action to issue the given role to the given subject.
     * 
     * @param outgoingRole the outgoing role which is to be issued.
     * @param subject the subject of this external action.
     * 
     * @require outgoingRole.isOnHost() : "The outgoing role is on a host.";
     */
    @NonCommitting
    OutgoingRoleIssue(@Nonnull OutgoingRole outgoingRole, @Nonnull InternalPerson subject) throws SQLException {
        super(outgoingRole.getAccount(), subject);
        
        this.issuer = outgoingRole.getAccount().getIdentity();
        this.relation = outgoingRole.getRelation();
        this.agentNumber = outgoingRole.getNumber();
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
    private OutgoingRoleIssue(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        if (signature instanceof HostSignatureWrapper) {
            this.issuer = signature.toHostSignatureWrapper().getSigner().getIdentity().toInternalNonHostIdentity();
        } else {
            this.issuer = entity.getIdentity().toInternalNonHostIdentity();
        }
        
        final @Nonnull ReadOnlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(2);
        this.relation = IdentityClass.create(elements.getNonNullable(0)).toSemanticType().checkIsRoleType();
        this.agentNumber = new Int64Wrapper(elements.getNonNullable(1)).getValue();
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new TupleWrapper(TYPE, relation.toBlock().setType(SemanticType.IDENTIFIER), new Int64Wrapper(Agent.NUMBER, agentNumber).toBlock()).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Issues a role with the relation " + relation.getAddress() + ".";
    }
    
    
    @Pure
    @Override
    public @Nonnull Restrictions getAuditRestrictions() {
        return new Restrictions(false, true, false);
    }
    
    @Pure
    @Override
    public @Nonnull OutgoingRole getFailedAuditAgent() {
        return OutgoingRole.get(getNonHostEntity(), agentNumber, false, false);
    }
    
    
    /**
     * Executes this action on both hosts and clients.
     */
    @NonCommitting
    private void executeOnBoth() throws SQLException {
        AgentModule.addIncomingRole(getNonHostEntity(), issuer, relation, agentNumber);
    }
    
    @Override
    @NonCommitting
    public @Nullable CoreServiceActionReply executeOnHost() throws PacketException, SQLException {
        if (!getSignatureNotNull().isSigned()) throw new PacketException(PacketError.AUTHORIZATION, "The issuance of a role has to be signed.");
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
    public void executeOnClient() throws SQLException {
        executeOnBoth();
        getRole().addRole(issuer, relation, agentNumber);
    }
    
    @Override
    @NonCommitting
    public void executeOnFailure() throws SQLException {
        // TODO: Add this role issuance to a list of failed external actions.
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (protectedEquals(object) && object instanceof OutgoingRoleIssue) {
            final @Nonnull OutgoingRoleIssue other = (OutgoingRoleIssue) object;
            return this.issuer.equals(other.issuer) && this.relation.equals(other.relation) && this.agentNumber == other.agentNumber;
        }
        return false;
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = protectedHashCode();
        hash = 89 * hash + issuer.hashCode();
        hash = 89 * hash + relation.hashCode();
        hash = 89 * hash + (int) (agentNumber ^ (agentNumber >>> 32));
        return hash;
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull BothModule getModule() {
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
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
            return new OutgoingRoleIssue(entity, signature, recipient, block);
        }
        
    }
    
}
