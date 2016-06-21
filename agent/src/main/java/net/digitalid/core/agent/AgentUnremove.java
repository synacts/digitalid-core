package net.digitalid.core.agent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.wrappers.signature.SignatureWrapper;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.packet.exceptions.RequestException;
import net.digitalid.core.handler.Action;
import net.digitalid.core.handler.Method;
import net.digitalid.core.handler.core.CoreServiceInternalAction;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identity.SemanticType;

import net.digitalid.service.core.dataservice.StateModule;

/**
 * Unremoves the given {@link Agent agent}.
 */
@Immutable
final class AgentUnremove extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code unremove.agent@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.map("unremove.agent@core.digitalid.net").load(Agent.TYPE);
    
    
    /**
     * Stores the agent to be unremoved.
     */
    final @Nonnull Agent agent;
    
    /**
     * Creates an internal action to unremove the given agent.
     * 
     * @param agent the agent which is to be unremoved.
     * 
     * @require agent.isOnClient() : "The agent is on a client.";
     */
    AgentUnremove(@Nonnull Agent agent) {
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
    private AgentUnremove(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws ExternalException {
        super(entity, signature, recipient);
        
        this.agent = Agent.get(entity.castTo(NonHostEntity.class), block);
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return agent.toBlock().setType(TYPE);
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Unremoves the agent with the number " + agent + ".";
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
    protected void executeOnBoth() throws DatabaseException {
        agent.unremoveForActions();
    }
    
    @Pure
    @Override
    public boolean interferesWith(@Nonnull Action action) {
        return action instanceof AgentUnremove && ((AgentUnremove) action).agent.equals(agent) || action instanceof AgentRemove && ((AgentRemove) action).agent.equals(agent);
    }
    
    @Pure
    @Override
    public @Nonnull AgentRemove getReverse() {
        return new AgentRemove(agent);
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        return protectedEquals(object) && object instanceof AgentUnremove && this.agent.equals(((AgentUnremove) object).agent);
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
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws ExternalException {
            return new AgentUnremove(entity, signature, recipient, block);
        }
        
    }
    
}
