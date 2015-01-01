package ch.virtualid.handler.action.external;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.OutgoingRole;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
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
import ch.virtualid.module.BothModule;
import ch.virtualid.module.both.Agents;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.ReadonlyArray;
import ch.xdf.Block;
import ch.xdf.HostSignatureWrapper;
import ch.xdf.Int64Wrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Issues the given role to the given subject.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class RoleIssuance extends CoreServiceExternalAction {
    
    /**
     * Stores the semantic type {@code issuance.role@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("issuance.role@virtualid.ch").load(TupleWrapper.TYPE, SemanticType.IDENTIFIER, Agent.NUMBER);
    
    
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
    public RoleIssuance(@Nonnull OutgoingRole outgoingRole, @Nonnull InternalPerson subject) throws SQLException {
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
    private RoleIssuance(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        if (signature instanceof HostSignatureWrapper) {
            this.issuer = signature.toHostSignatureWrapper().getSigner().getIdentity().toInternalNonHostIdentity();
        } else {
            this.issuer = entity.getIdentity().toInternalNonHostIdentity();
        }
        
        final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(2);
        this.relation = IdentityClass.create(elements.getNotNull(0)).toSemanticType().checkIsRoleType();
        this.agentNumber = new Int64Wrapper(elements.getNotNull(1)).getValue();
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new TupleWrapper(TYPE, new FreezableArray<Block>(relation.toBlock().setType(SemanticType.IDENTIFIER), new Int64Wrapper(Agent.NUMBER, agentNumber).toBlock()).freeze()).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
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
    
    
    @Pure
    @Override
    public @Nullable Class<CoreServiceActionReply> getReplyClass() {
        return null;
    }
    
    /**
     * Executes this action on both hosts and clients.
     */
    private void executeOnBoth() throws SQLException {
        Agents.addIncomingRole(getNonHostEntity(), issuer, relation, agentNumber);
    }
    
    @Override
    public @Nullable ActionReply executeOnHost() throws PacketException, SQLException {
        if (getSignatureNotNull().isNotSigned()) throw new PacketException(PacketError.AUTHORIZATION, "The issuance of a role has to be signed.");
        executeOnBoth();
        return null;
    }
    
    @Override
    public void executeOnClient() throws SQLException {
        executeOnBoth();
        getRole().addRole(issuer, relation, agentNumber);
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
    
    @Pure
    @Override
    public @Nonnull BothModule getModule() {
        return Agents.MODULE;
    }
    
    /**
     * The factory class for the surrounding method.
     */
    private static final class Factory extends Method.Factory {
        
        static { Method.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
            return new RoleIssuance(entity, signature, recipient, block);
        }
        
    }
    
}
