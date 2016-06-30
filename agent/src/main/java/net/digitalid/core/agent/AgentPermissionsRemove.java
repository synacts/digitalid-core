package net.digitalid.core.agent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.wrappers.signature.SignatureWrapper;
import net.digitalid.core.conversion.wrappers.structure.TupleWrapper;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.packet.exceptions.NetworkException;
import net.digitalid.core.packet.exceptions.RequestException;
import net.digitalid.core.handler.Action;
import net.digitalid.core.handler.Method;
import net.digitalid.core.service.handler.CoreServiceInternalAction;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identity.SemanticType;

import net.digitalid.service.core.dataservice.StateModule;

/**
 * Removes {@link FreezableAgentPermissions permissions} from an {@link Agent agent}.
 */
@Immutable
final class AgentPermissionsRemove extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code remove.permissions.agent@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.map("remove.permissions.agent@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Agent.TYPE, FreezableAgentPermissions.TYPE);
    
    
    /**
     * Stores the agent of this action.
     */
    final @Nonnull Agent agent;
    
    /**
     * Stores the permissions which are to be removed.
     * 
     * @invariant permissions.isFrozen() : "The permissions are frozen.";
     */
    private final @Nonnull ReadOnlyAgentPermissions permissions;
    
    /**
     * Creates an internal action to remove the given permissions from the given agent.
     * 
     * @param agent the agent whose permissions are to be reduced.
     * @param permissions the permissions to be removed from the given agent.
     * 
     * @require agent.isOnClient() : "The agent is on a client.";
     * @require permissions.isFrozen() : "The permissions are frozen.";
     */
    AgentPermissionsRemove(@Nonnull Agent agent, @Nonnull ReadOnlyAgentPermissions permissions) {
        super(agent.getRole());
        
        Require.that(permissions.isFrozen()).orThrow("The permissions are frozen.");
        
        this.agent = agent;
        this.permissions = permissions;
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
    private AgentPermissionsRemove(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws ExternalException {
        super(entity, signature, recipient);
        
        final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(block).getNonNullableElements(2);
        this.agent = Agent.get(entity.castTo(NonHostEntity.class), elements.getNonNullable(0));
        this.permissions = new FreezableAgentPermissions(elements.getNonNullable(1)).freeze();
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return TupleWrapper.encode(TYPE, agent, permissions);
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Removes the permissions " + permissions + " from the agent with the number " + agent + ".";
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
        agent.removePermissionsForActions(permissions);
    }
    
    @Pure
    @Override
    public boolean interferesWith(@Nonnull Action action) {
        return action instanceof AgentPermissionsRemove && ((AgentPermissionsRemove) action).agent.equals(agent) || action instanceof AgentPermissionsAdd && ((AgentPermissionsAdd) action).agent.equals(agent);
    }
    
    @Pure
    @Override
    public @Nonnull AgentPermissionsAdd getReverse() {
        return new AgentPermissionsAdd(agent, permissions);
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        return protectedEquals(object) && object instanceof AgentPermissionsRemove && this.agent.equals(((AgentPermissionsRemove) object).agent) && this.permissions.equals(((AgentPermissionsRemove) object).permissions);
    }
    
    @Pure
    @Override
    public int hashCode() {
        return 89 * (89 * protectedHashCode() + agent.hashCode()) + permissions.hashCode();
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
            return new AgentPermissionsRemove(entity, signature, recipient, block);
        }
        
    }
    
}
