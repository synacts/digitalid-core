package ch.virtualid.handler.action.internal;

import ch.virtualid.service.CoreServiceInternalAction;
import ch.virtualid.agent.Agent;
import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Method;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.BothModule;
import ch.virtualid.module.both.Agents;
import ch.xdf.Block;
import ch.xdf.SignatureWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * Unremoves the given {@link Agent agent}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class AgentUnremove extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code unremove.agent@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("unremove.agent@virtualid.ch").load(Agent.TYPE);
    
    
    /**
     * Stores the agent to be unremoved.
     */
    private final @Nonnull Agent agent;
    
    /**
     * Creates an internal action to unremove the given agent.
     * 
     * @param agent the agent which is to be unremoved.
     * 
     * @require agent.isOnClient() : "The agent is on a client.";
     */
    public AgentUnremove(@Nonnull Agent agent) {
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
    private AgentUnremove(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
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
    public @Nonnull String toString() {
        return "Unremoves the agent with the number " + agent + ".";
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
        agent.unremoveForActions();
    }
    
    @Pure
    @Override
    public @Nonnull AgentRemove getReverse() {
        return new AgentRemove(agent);
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
            return new AgentUnremove(entity, signature, recipient, block);
        }
        
    }
    
}
