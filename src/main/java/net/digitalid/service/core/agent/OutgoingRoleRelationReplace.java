package net.digitalid.core.agent;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.annotations.state.Immutable;
import net.digitalid.database.annotations.NonCommitting;
import net.digitalid.annotations.state.Pure;
import net.digitalid.collections.readonly.ReadOnlyArray;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.handler.Action;
import net.digitalid.core.handler.Method;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identity.IdentityClass;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.data.StateModule;
import net.digitalid.core.service.CoreServiceInternalAction;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.SignatureWrapper;
import net.digitalid.core.wrappers.TupleWrapper;

/**
 * Replaces the relation of a {@link OutgoingRole outgoing role}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
final class OutgoingRoleRelationReplace extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code old.relation.outgoing.role@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType OLD_RELATION = SemanticType.map("old.relation.outgoing.role@core.digitalid.net").load(SemanticType.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code new.relation.outgoing.role@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType NEW_RELATION = SemanticType.map("new.relation.outgoing.role@core.digitalid.net").load(SemanticType.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code replace.relation.outgoing.role@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.map("replace.relation.outgoing.role@core.digitalid.net").load(TupleWrapper.TYPE, Agent.TYPE, OLD_RELATION, NEW_RELATION);
    
    
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
    OutgoingRoleRelationReplace(@Nonnull OutgoingRole outgoingRole, @Nonnull SemanticType oldRelation, @Nonnull SemanticType newRelation) {
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
    @NonCommitting
    private OutgoingRoleRelationReplace(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        final @Nonnull ReadOnlyArray<Block> elements = new TupleWrapper(block).getNonNullableElements(3);
        this.outgoingRole = Agent.get(entity.toNonHostEntity(), elements.getNonNullable(0)).toOutgoingRole();
        this.oldRelation = IdentityClass.create(elements.getNonNullable(1)).toSemanticType().checkIsRoleType();
        this.newRelation = IdentityClass.create(elements.getNonNullable(2)).toSemanticType().checkIsRoleType();
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new TupleWrapper(TYPE, outgoingRole.toBlock(), oldRelation.toBlock().setType(OLD_RELATION), newRelation.toBlock().setType(NEW_RELATION)).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Replaces the relation " + oldRelation.getAddress() + " with " + newRelation.getAddress() + " of the outgoing role with the number " + outgoingRole + ".";
    }
    
    
    @Pure
    @Override
    public boolean isSimilarTo(@Nonnull Method other) {
        return super.isSimilarTo(other) && (!isOnClient() || !getRole().getAgent().equals(outgoingRole));
    }
    
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissions() {
        return new FreezableAgentPermissions(newRelation, true).freeze();
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
    @NonCommitting
    protected void executeOnBoth() throws SQLException {
        outgoingRole.replaceRelation(oldRelation, newRelation);
    }
    
    @Pure
    @Override
    public boolean interferesWith(@Nonnull Action action) {
        return action instanceof OutgoingRoleRelationReplace && ((OutgoingRoleRelationReplace) action).outgoingRole.equals(outgoingRole);
    }
    
    @Pure
    @Override
    public @Nonnull OutgoingRoleRelationReplace getReverse() {
        return new OutgoingRoleRelationReplace(outgoingRole, newRelation, oldRelation);
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (protectedEquals(object) && object instanceof OutgoingRoleContextReplace) {
            final @Nonnull OutgoingRoleRelationReplace other = (OutgoingRoleRelationReplace) object;
            return this.outgoingRole.equals(other.outgoingRole) && this.oldRelation.equals(other.oldRelation) && this.newRelation.equals(other.newRelation);
        }
        return false;
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = protectedHashCode();
        hash = 89 * hash + outgoingRole.hashCode();
        hash = 89 * hash + oldRelation.hashCode();
        hash = 89 * hash + newRelation.hashCode();
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
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException  {
            return new OutgoingRoleRelationReplace(entity, signature, recipient, block);
        }
        
    }
    
}
