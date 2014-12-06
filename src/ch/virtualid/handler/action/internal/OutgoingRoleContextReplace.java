package ch.virtualid.handler.action.internal;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.OutgoingRole;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.contact.Context;
import ch.virtualid.entity.Entity;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Method;
import ch.virtualid.identifier.HostIdentifier;
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
 * Replaces the context of a {@link OutgoingRole outgoing role}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class OutgoingRoleContextReplace extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code old.context.outgoing.role@virtualid.ch}.
     */
    private static final @Nonnull SemanticType OLD_CONTEXT = SemanticType.create("old.context.outgoing.role@virtualid.ch").load(SemanticType.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code new.context.outgoing.role@virtualid.ch}.
     */
    private static final @Nonnull SemanticType NEW_CONTEXT = SemanticType.create("new.context.outgoing.role@virtualid.ch").load(SemanticType.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code replace.context.outgoing.role@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("replace.context.outgoing.role@virtualid.ch").load(TupleWrapper.TYPE, Agent.TYPE, OLD_CONTEXT, NEW_CONTEXT);
    
    
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
    public OutgoingRoleContextReplace(@Nonnull OutgoingRole outgoingRole, @Nonnull Context oldContext, @Nonnull Context newContext) {
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
    private OutgoingRoleContextReplace(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(3);
        this.outgoingRole = Agent.get(entity, elements.getNotNull(0)).toOutgoingRole();
        this.oldContext = Context.get(entity, elements.getNotNull(1));
        this.newContext = Context.get(entity, elements.getNotNull(2));
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new TupleWrapper(TYPE, new FreezableArray<Block>(outgoingRole.toBlock(), oldContext.toBlock().setType(OLD_CONTEXT), newContext.toBlock().setType(NEW_CONTEXT)).freeze()).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
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
    protected void executeOnBoth() throws SQLException {
        outgoingRole.replaceContext(oldContext, newContext);
    }
    
    @Pure
    @Override
    public @Nonnull OutgoingRoleContextReplace getReverse() {
        return new OutgoingRoleContextReplace(outgoingRole, newContext, oldContext);
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
            return new OutgoingRoleContextReplace(entity, signature, recipient, block);
        }
        
    }
    
}
