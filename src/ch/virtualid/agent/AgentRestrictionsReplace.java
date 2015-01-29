package ch.virtualid.agent;

import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.NonHostEntity;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Action;
import ch.virtualid.handler.Method;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.BothModule;
import ch.virtualid.service.CoreServiceInternalAction;
import ch.virtualid.util.ReadonlyArray;
import ch.xdf.Block;
import ch.xdf.SignatureWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Replaces the {@Restrictions restrictions} of an {@link Agent agent}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
final class AgentRestrictionsReplace extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code old.restrictions.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType OLD_RESTRICTIONS = SemanticType.create("old.restrictions.agent@virtualid.ch").load(Restrictions.TYPE);
    
    /**
     * Stores the semantic type {@code new.restrictions.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType NEW_RESTRICTIONS = SemanticType.create("new.restrictions.agent@virtualid.ch").load(Restrictions.TYPE);
    
    /**
     * Stores the semantic type {@code replace.restrictions.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.create("replace.restrictions.agent@virtualid.ch").load(TupleWrapper.TYPE, Agent.TYPE, OLD_RESTRICTIONS, NEW_RESTRICTIONS);
    
    
    /**
     * Stores the agent of this action.
     */
    private final @Nonnull Agent agent;
    
    /**
     * Stores the old restrictions of the agent.
     * 
     * @invariant oldRestrictions.match(agent) : "The old restrictions match the agent.";
     */
    private final @Nonnull Restrictions oldRestrictions;
    
    /**
     * Stores the new restrictions of the agent.
     * 
     * @invariant newRestrictions.match(agent) : "The new restrictions match the agent.";
     */
    private final @Nonnull Restrictions newRestrictions;
    
    /**
     * Creates an internal action to replace the restrictions of the given agent.
     * 
     * @param agent the agent whose restrictions are to be replaced.
     * @param oldRestrictions the old restrictions of the given agent.
     * @param newRestrictions the new restrictions of the given agent.
     * 
     * @require agent.isOnClient() : "The agent is on a client.";
     * @require oldRestrictions.match(agent) : "The old restrictions match the agent.";
     * @require newRestrictions.match(agent) : "The new restrictions match the agent.";
     */
    AgentRestrictionsReplace(@Nonnull Agent agent, @Nonnull Restrictions oldRestrictions, @Nonnull Restrictions newRestrictions) {
        super(agent.getRole());
        
        assert oldRestrictions.match(agent) : "The old restrictions match the agent.";
        assert newRestrictions.match(agent) : "The new restrictions match the agent.";
        
        this.agent = agent;
        this.oldRestrictions = oldRestrictions;
        this.newRestrictions = newRestrictions;
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
    private AgentRestrictionsReplace(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        final @Nonnull NonHostEntity nonHostEntity = entity.toNonHostEntity();
        final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(3);
        this.agent = Agent.get(nonHostEntity, elements.getNotNull(0));
        this.oldRestrictions = new Restrictions(nonHostEntity, elements.getNotNull(1)).checkMatch(agent);
        this.newRestrictions = new Restrictions(nonHostEntity, elements.getNotNull(2)).checkMatch(agent);
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new TupleWrapper(TYPE, agent.toBlock(), oldRestrictions.toBlock().setType(OLD_RESTRICTIONS), newRestrictions.toBlock().setType(NEW_RESTRICTIONS)).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Replaces the restrictions " + oldRestrictions + " with " + newRestrictions + " of the agent with the number " + agent + ".";
    }
    
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictions() {
        return newRestrictions;
    }
    
    @Pure
    @Override
    public @Nonnull Agent getRequiredAgent() {
        return agent;
    }
    
    @Pure
    @Override
    public @Nonnull Agent getAuditAgent() {
        return agent;
    }
    
    
    @Override
    protected void executeOnBoth() throws SQLException {
        agent.replaceRestrictions(oldRestrictions, newRestrictions);
    }
    
    @Pure
    @Override
    public boolean interferesWith(@Nonnull Action action) {
        return action instanceof AgentRestrictionsReplace && ((AgentRestrictionsReplace) action).agent.equals(agent);
    }
    
    @Pure
    @Override
    public @Nonnull AgentRestrictionsReplace getReverse() {
        return new AgentRestrictionsReplace(agent, newRestrictions, oldRestrictions);
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        return protectedEquals(object) && object instanceof AgentRestrictionsReplace && this.agent.equals(((AgentRestrictionsReplace) object).agent) && this.oldRestrictions.equals(((AgentRestrictionsReplace) object).oldRestrictions) && this.newRestrictions.equals(((AgentRestrictionsReplace) object).newRestrictions);
    }
    
    @Pure
    @Override
    public int hashCode() {
        return 89 * (89 * (89 * protectedHashCode() + agent.hashCode()) + oldRestrictions.hashCode()) + newRestrictions.hashCode();
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
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException  {
            return new AgentRestrictionsReplace(entity, signature, recipient, block);
        }
        
    }
    
}
