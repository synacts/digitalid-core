package net.digitalid.core.agent;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.ReadOnlyArray;
import net.digitalid.core.contact.Context;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.handler.Action;
import net.digitalid.core.handler.Method;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.module.BothModule;
import net.digitalid.core.service.CoreServiceInternalAction;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.SignatureWrapper;
import net.digitalid.core.wrappers.TupleWrapper;

/**
 * Replaces the context of a {@link OutgoingRole outgoing role}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
final class OutgoingRoleContextReplace extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code old.context.outgoing.role@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType OLD_CONTEXT = SemanticType.create("old.context.outgoing.role@core.digitalid.net").load(SemanticType.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code new.context.outgoing.role@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType NEW_CONTEXT = SemanticType.create("new.context.outgoing.role@core.digitalid.net").load(SemanticType.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code replace.context.outgoing.role@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.create("replace.context.outgoing.role@core.digitalid.net").load(TupleWrapper.TYPE, Agent.TYPE, OLD_CONTEXT, NEW_CONTEXT);
    
    
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
    private OutgoingRoleContextReplace(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        final @Nonnull NonHostEntity nonHostEntity = entity.toNonHostEntity();
        final @Nonnull ReadOnlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(3);
        this.outgoingRole = Agent.get(nonHostEntity, elements.getNonNullable(0)).toOutgoingRole();
        this.oldContext = Context.get(nonHostEntity, elements.getNonNullable(1));
        this.newContext = Context.get(nonHostEntity, elements.getNonNullable(2));
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new TupleWrapper(TYPE, outgoingRole.toBlock(), oldContext.toBlock().setType(OLD_CONTEXT), newContext.toBlock().setType(NEW_CONTEXT)).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Replaces the context " + oldContext + " with " + newContext + " of the outgoing role with the number " + outgoingRole + ".";
    }
    
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictions() {
        return new Restrictions(false, false, true, newContext);
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
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException  {
            return new OutgoingRoleContextReplace(entity, signature, recipient, block);
        }
        
    }
    
}
