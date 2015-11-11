package net.digitalid.service.core.concepts.agent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.SignatureWrapper;
import net.digitalid.service.core.dataservice.StateModule;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.handler.Action;
import net.digitalid.service.core.handler.Method;
import net.digitalid.service.core.handler.core.CoreServiceInternalAction;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * Removes the given {@link Agent agent}.
 */
@Immutable
final class AgentRemove extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code remove.agent@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.map("remove.agent@core.digitalid.net").load(Agent.TYPE);
    
    
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
    @NonCommitting
    private AgentRemove(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException {
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
    public @Nonnull Agent getRequiredAgentToExecuteMethod() {
        return agent;
    }
    
    @Pure
    @Override
    public @Nonnull Agent getRequiredAgentToSeeAudit() {
        return agent;
    }
    
    
    @Override
    @NonCommitting
    protected void executeOnBoth() throws AbortException {
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
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException {
            return new AgentRemove(entity, signature, recipient, block);
        }
        
    }
    
}
