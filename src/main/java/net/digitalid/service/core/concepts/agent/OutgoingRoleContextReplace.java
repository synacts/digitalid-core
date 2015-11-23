package net.digitalid.service.core.concepts.agent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.SignatureWrapper;
import net.digitalid.service.core.block.wrappers.TupleWrapper;
import net.digitalid.service.core.concepts.contact.Context;
import net.digitalid.service.core.dataservice.StateModule;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.handler.Action;
import net.digitalid.service.core.handler.Method;
import net.digitalid.service.core.handler.core.CoreServiceInternalAction;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * Replaces the context of a {@link OutgoingRole outgoing role}.
 */
@Immutable
final class OutgoingRoleContextReplace extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code old.context.outgoing.role@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType OLD_CONTEXT = SemanticType.map("old.context.outgoing.role@core.digitalid.net").load(SemanticType.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code new.context.outgoing.role@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType NEW_CONTEXT = SemanticType.map("new.context.outgoing.role@core.digitalid.net").load(SemanticType.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code replace.context.outgoing.role@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.map("replace.context.outgoing.role@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Agent.TYPE, OLD_CONTEXT, NEW_CONTEXT);
    
    
    /**
     * Stores the outgoing role of this action.
     */
    private final @Nonnull OutgoingRole outgoingRole;
    
    /**
     * Stores the old context of the outgoing role.
     * 
     * @invariant oldContext.getEntity().equals(outgoingRole.getEntity()) : "The old context belongs to the entity of the outgoing role.";
     */
    private final @Nonnull Context oldContext;
    
    /**
     * Stores the new context of the outgoing role.
     * 
     * @invariant newContext.getEntity().equals(outgoingRole.getEntity()) : "The new context belongs to the entity of the outgoing role.";
     */
    private final @Nonnull Context newContext;
    
    /**
     * Creates an internal action to replace the context of the given outgoing role.
     * 
     * @param outgoingRole the outgoing role whose context is to be replaced.
     * @param oldContext the old context of the given outgoing role.
     * @param newContext the new context of the given outgoing role.
     * 
     * @require outgoingRole.isOnClient() : "The outgoing role is on a client.";
     * @require oldContext.getEntity().equals(outgoingRole.getEntity()) : "The old context belongs to the entity of the outgoing role.";
     * @require newContext.getEntity().equals(outgoingRole.getEntity()) : "The new context belongs to the entity of the outgoing role.";
     */
    OutgoingRoleContextReplace(@Nonnull OutgoingRole outgoingRole, @Nonnull Context oldContext, @Nonnull Context newContext) {
        super(outgoingRole.getRole());
        
        assert oldContext.getEntity().equals(outgoingRole.getEntity()) : "The old context belongs to the entity of the outgoing role.";
        assert newContext.getEntity().equals(outgoingRole.getEntity()) : "The new context belongs to the entity of the outgoing role.";
        
        this.outgoingRole = outgoingRole;
        this.oldContext = oldContext;
        this.newContext = newContext;
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
    private OutgoingRoleContextReplace(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws DatabaseException, PacketException, ExternalException, NetworkException {
        super(entity, signature, recipient);
        
        final @Nonnull NonHostEntity nonHostEntity = entity.castTo(NonHostEntity.class);
        final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(block).getNonNullableElements(3);
        this.outgoingRole = Agent.get(nonHostEntity, elements.getNonNullable(0)).castTo(OutgoingRole.class);
        this.oldContext = Context.get(nonHostEntity, elements.getNonNullable(1));
        this.newContext = Context.get(nonHostEntity, elements.getNonNullable(2));
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return TupleWrapper.encode(TYPE, outgoingRole.toBlock(), oldContext.toBlock().setType(OLD_CONTEXT), newContext.toBlock().setType(NEW_CONTEXT));
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Replaces the context " + oldContext + " with " + newContext + " of the outgoing role with the number " + outgoingRole + ".";
    }
    
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictionsToExecuteMethod() {
        return new Restrictions(false, false, true, newContext);
    }
    
    @Pure
    @Override
    public @Nonnull Agent getRequiredAgentToExecuteMethod() {
        return outgoingRole;
    }
    
    @Pure
    @Override
    public @Nonnull Agent getRequiredAgentToSeeAudit() {
        return outgoingRole;
    }
    
    
    @Override
    @NonCommitting
    protected void executeOnBoth() throws DatabaseException {
        outgoingRole.replaceContext(oldContext, newContext);
    }
    
    @Pure
    @Override
    public boolean interferesWith(@Nonnull Action action) {
        return action instanceof OutgoingRoleContextReplace && ((OutgoingRoleContextReplace) action).outgoingRole.equals(outgoingRole);
    }
    
    @Pure
    @Override
    public @Nonnull OutgoingRoleContextReplace getReverse() {
        return new OutgoingRoleContextReplace(outgoingRole, newContext, oldContext);
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (protectedEquals(object) && object instanceof OutgoingRoleContextReplace) {
            final @Nonnull OutgoingRoleContextReplace other = (OutgoingRoleContextReplace) object;
            return this.outgoingRole.equals(other.outgoingRole) && this.oldContext.equals(other.oldContext) && this.newContext.equals(other.newContext);
        }
        return false;
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = protectedHashCode();
        hash = 89 * hash + outgoingRole.hashCode();
        hash = 89 * hash + oldContext.hashCode();
        hash = 89 * hash + newContext.hashCode();
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
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws DatabaseException, PacketException, ExternalException, NetworkException {
            return new OutgoingRoleContextReplace(entity, signature, recipient, block);
        }
        
    }
    
}
