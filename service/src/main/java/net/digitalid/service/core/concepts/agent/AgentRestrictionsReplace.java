package net.digitalid.service.core.concepts.agent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.exceptions.external.ExternalException;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;

import net.digitalid.database.core.annotations.NonCommitting;

import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.signature.SignatureWrapper;
import net.digitalid.service.core.block.wrappers.structure.TupleWrapper;
import net.digitalid.service.core.dataservice.StateModule;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.handler.Action;
import net.digitalid.service.core.handler.Method;
import net.digitalid.service.core.handler.core.CoreServiceInternalAction;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.identity.SemanticType;

/**
 * Replaces the {@link Restrictions restrictions} of an {@link Agent agent}.
 */
@Immutable
final class AgentRestrictionsReplace extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code old.restrictions.agent@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType OLD_RESTRICTIONS = SemanticType.map("old.restrictions.agent@core.digitalid.net").load(Restrictions.TYPE);
    
    /**
     * Stores the semantic type {@code new.restrictions.agent@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType NEW_RESTRICTIONS = SemanticType.map("new.restrictions.agent@core.digitalid.net").load(Restrictions.TYPE);
    
    /**
     * Stores the semantic type {@code replace.restrictions.agent@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.map("replace.restrictions.agent@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Agent.TYPE, OLD_RESTRICTIONS, NEW_RESTRICTIONS);
    
    
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
    @NonCommitting
    private AgentRestrictionsReplace(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        super(entity, signature, recipient);
        
        final @Nonnull NonHostEntity nonHostEntity = entity.castTo(NonHostEntity.class);
        final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(block).getNonNullableElements(3);
        this.agent = Agent.get(nonHostEntity, elements.getNonNullable(0));
        this.oldRestrictions = new Restrictions(nonHostEntity, elements.getNonNullable(1)).checkMatch(agent);
        this.newRestrictions = new Restrictions(nonHostEntity, elements.getNonNullable(2)).checkMatch(agent);
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return TupleWrapper.encode(TYPE, agent.toBlock(), oldRestrictions.toBlock().setType(OLD_RESTRICTIONS), newRestrictions.toBlock().setType(NEW_RESTRICTIONS));
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Replaces the restrictions " + oldRestrictions + " with " + newRestrictions + " of the agent with the number " + agent + ".";
    }
    
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictionsToExecuteMethod() {
        return newRestrictions;
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
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
            return new AgentRestrictionsReplace(entity, signature, recipient, block);
        }
        
    }
    
}
