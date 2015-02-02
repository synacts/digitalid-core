package ch.virtualid.agent;

import ch.virtualid.annotations.DoesNotCommit;
import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Action;
import ch.virtualid.handler.Method;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.BothModule;
import ch.virtualid.service.CoreServiceInternalAction;
import ch.xdf.Block;
import ch.xdf.SignatureWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Removes the given {@link Agent agent}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
final class AgentRemove extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code remove.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.create("remove.agent@virtualid.ch").load(Agent.TYPE);
    
    
    /**
     * Stores the agent to be removed.
     */
    final @Nonnull Agent agent;
    
    /**
     * Creates an internal action to remove the given agent.
     * 
     * @param agent the agent which is to be removed.
     * 
     * @require agent.isOnClient() : "The agent is on a client.";
     */
    AgentRemove(@Nonnull Agent agent) {
        super(agent.getRole());
        
        this.agent = agent;
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
    @DoesNotCommit
    private AgentRemove(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        this.agent = Agent.get(entity.toNonHostEntity(), block);
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return agent.toBlock().setType(TYPE);
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Removes the agent with the number " + agent + ".";
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
    @DoesNotCommit
    protected void executeOnBoth() throws SQLException {
        agent.removeForActions();
    }
    
    @Pure
    @Override
    public boolean interferesWith(@Nonnull Action action) {
        return action instanceof AgentRemove && ((AgentRemove) action).agent.equals(agent) || action instanceof AgentUnremove && ((AgentUnremove) action).agent.equals(agent);
    }
    
    @Pure
    @Override
    public @Nonnull AgentUnremove getReverse() {
        return new AgentUnremove(agent);
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        return protectedEquals(object) && object instanceof AgentRemove && this.agent.equals(((AgentRemove) object).agent);
    }
    
    @Pure
    @Override
    public int hashCode() {
        return 89 * protectedHashCode() + agent.hashCode();
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
        @DoesNotCommit
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException  {
            return new AgentRemove(entity, signature, recipient, block);
        }
        
    }
    
}
