package ch.virtualid.handler.action.internal;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.OutgoingRole;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Method;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identity.IdentityClass;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.BothModule;
import ch.virtualid.module.both.Agents;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.ReadonlyArray;
import ch.xdf.Block;
import ch.xdf.SignatureWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * Replaces the relation of a {@link OutgoingRole outgoing role}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class OutgoingRoleRelationReplace extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code old.relation.outgoing.role@virtualid.ch}.
     */
    private static final @Nonnull SemanticType OLD_RELATION = SemanticType.create("old.relation.outgoing.role@virtualid.ch").load(SemanticType.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code new.relation.outgoing.role@virtualid.ch}.
     */
    private static final @Nonnull SemanticType NEW_RELATION = SemanticType.create("new.relation.outgoing.role@virtualid.ch").load(SemanticType.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code replace.relation.outgoing.role@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("replace.relation.outgoing.role@virtualid.ch").load(TupleWrapper.TYPE, Agent.TYPE, OLD_RELATION, NEW_RELATION);
    
    
    /**
     * Stores the outgoing role of this action.
     */
    private final @Nonnull OutgoingRole outgoingRole;
    
    /**
     * Stores the old relation of the outgoing role.
     * 
     * @invariant oldRelation.isRoleType() : "The old relation is a role type.";
     */
    private final @Nonnull SemanticType oldRelation;
    
    /**
     * Stores the new relation of the outgoing role.
     * 
     * @invariant newRelation.isRoleType() : "The new relation is a role type.";
     */
    private final @Nonnull SemanticType newRelation;
    
    /**
     * Creates an internal action to replace the relation of the given outgoing role.
     * 
     * @param outgoingRole the outgoing role whose relation is to be replaced.
     * @param oldRelation the old relation of the given outgoing role.
     * @param newRelation the new relation of the given outgoing role.
     * 
     * @require outgoingRole.isOnClient() : "The outgoing role is on a client.";
     * @require oldRelation.isRoleType() : "The old relation is a role type.";
     * @require newRelation.isRoleType() : "The new relation is a role type.";
     */
    public OutgoingRoleRelationReplace(@Nonnull OutgoingRole outgoingRole, @Nonnull SemanticType oldRelation, @Nonnull SemanticType newRelation) {
        super(outgoingRole.getRole());
        
        assert oldRelation.isRoleType() : "The old relation is a role type.";
        assert newRelation.isRoleType() : "The new relation is a role type.";
        
        this.outgoingRole = outgoingRole;
        this.oldRelation = oldRelation;
        this.newRelation = newRelation;
    }
    
    /**
     * Creates an internal action that decodes the given block.
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
    private OutgoingRoleRelationReplace(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(3);
        this.outgoingRole = Agent.get(entity, elements.getNotNull(0)).toOutgoingRole();
        this.oldRelation = IdentityClass.create(elements.getNotNull(1)).toSemanticType().checkIsRoleType();
        this.newRelation = IdentityClass.create(elements.getNotNull(2)).toSemanticType().checkIsRoleType();
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new TupleWrapper(TYPE, new FreezableArray<Block>(outgoingRole.toBlock(), oldRelation.toBlock().setType(OLD_RELATION), newRelation.toBlock().setType(NEW_RELATION)).freeze()).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Replaces the relation " + oldRelation.getAddress() + " with " + newRelation.getAddress() + " of the outgoing role with the number " + outgoingRole + ".";
    }
    
    
    @Pure
    @Override
    public @Nonnull ReadonlyAgentPermissions getRequiredPermissions() {
        return new AgentPermissions(newRelation, true).freeze();
    }
    
    @Pure
    @Override
    public @Nonnull Agent getRequiredAgent() {
        return outgoingRole;
    }
    
    @Pure
    @Override
    public @Nonnull Agent getAuditAgent() {
        return outgoingRole;
    }
    
    
    @Override
    protected void executeOnBoth() throws SQLException {
        outgoingRole.replaceRelation(oldRelation, newRelation);
    }
    
    @Pure
    @Override
    public @Nonnull OutgoingRoleRelationReplace getReverse() {
        return new OutgoingRoleRelationReplace(outgoingRole, newRelation, oldRelation);
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
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException  {
            return new OutgoingRoleRelationReplace(entity, signature, recipient, block);
        }
        
    }
    
}
