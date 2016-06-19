package net.digitalid.core.agent;

import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.wrappers.signature.HostSignatureWrapper;
import net.digitalid.core.conversion.wrappers.signature.SignatureWrapper;
import net.digitalid.core.conversion.wrappers.structure.TupleWrapper;
import net.digitalid.core.conversion.wrappers.value.integer.Integer64Wrapper;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.packet.exceptions.NetworkException;
import net.digitalid.core.packet.exceptions.RequestErrorCode;
import net.digitalid.core.packet.exceptions.RequestException;
import net.digitalid.core.handler.Method;
import net.digitalid.core.handler.Reply;
import net.digitalid.core.handler.core.CoreServiceActionReply;
import net.digitalid.core.handler.core.CoreServiceExternalAction;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identity.IdentityImplementation;
import net.digitalid.core.identity.InternalNonHostIdentity;
import net.digitalid.core.identity.InternalPerson;
import net.digitalid.core.identity.SemanticType;

import net.digitalid.service.core.dataservice.StateModule;

/**
 * Issues the given role to the given subject.
 */
@Immutable
final class OutgoingRoleIssue extends CoreServiceExternalAction {
    
    /**
     * Stores the semantic type {@code issuance.role@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.map("issuance.role@core.digitalid.net").load(TupleWrapper.XDF_TYPE, SemanticType.IDENTIFIER, Agent.NUMBER);
    
    
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
    OutgoingRoleIssue(@Nonnull OutgoingRole outgoingRole, @Nonnull InternalPerson subject) throws DatabaseException {
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
    private OutgoingRoleIssue(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws ExternalException {
        super(entity, signature, recipient);
        
        if (signature instanceof HostSignatureWrapper) {
            this.issuer = signature.toHostSignatureWrapper().getSigner().getIdentity().castTo(InternalNonHostIdentity.class);
        } else {
            this.issuer = entity.getIdentity().castTo(InternalNonHostIdentity.class);
        }
        
        final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(block).getNonNullableElements(2);
        this.relation = IdentityImplementation.create(elements.getNonNullable(0)).castTo(SemanticType.class).checkIsRoleType();
        this.agentNumber = Integer64Wrapper.decode(elements.getNonNullable(1));
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return TupleWrapper.encode(TYPE, relation.toBlock().setType(SemanticType.IDENTIFIER), Integer64Wrapper.encode(Agent.NUMBER, agentNumber));
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Issues a role with the relation " + relation.getAddress() + ".";
    }
    
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictionsToSeeAudit() {
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
    private void executeOnBoth() throws DatabaseException {
        AgentModule.addIncomingRole(getNonHostEntity(), issuer, relation, agentNumber);
    }
    
    @Override
    @NonCommitting
    public @Nullable CoreServiceActionReply executeOnHost() throws RequestException, SQLException {
        if (!getSignatureNotNull().isSigned()) { throw RequestException.get(RequestErrorCode.AUTHORIZATION, "The issuance of a role has to be signed."); }
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
        getRole().addRole(issuer, relation, agentNumber);
    }
    
    @Override
    @NonCommitting
    public void executeOnFailure() throws DatabaseException {
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
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws ExternalException {
            return new OutgoingRoleIssue(entity, signature, recipient, block);
        }
        
    }
    
}
