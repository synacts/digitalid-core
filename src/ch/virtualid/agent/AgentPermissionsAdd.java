package ch.virtualid.agent;

import ch.virtualid.annotations.NonCommitting;
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
import ch.virtualid.collections.ReadonlyArray;
import ch.xdf.Block;
import ch.xdf.SignatureWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Adds {@link AgentPermissions permissions} to an {@link Agent agent}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
final class AgentPermissionsAdd extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code add.permissions.agent@virtualid.ch}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.create("add.permissions.agent@virtualid.ch").load(TupleWrapper.TYPE, Agent.TYPE, AgentPermissions.TYPE);
    
    
    /**
     * Stores the agent of this action.
     */
    final @Nonnull Agent agent;
    
    /**
     * Stores the permissions which are to be added.
     * 
     * @invariant permissions.isFrozen() : "The permissions are frozen.";
     */
    private final @Nonnull ReadonlyAgentPermissions permissions;
    
    /**
     * Creates an internal action to add the given permissions to the given agent.
     * 
     * @param agent the agent whose permissions are to be extended.
     * @param permissions the permissions to be added to the given agent.
     * 
     * @require agent.isOnClient() : "The agent is on a client.";
     * @require permissions.isFrozen() : "The permissions are frozen.";
     */
    AgentPermissionsAdd(@Nonnull Agent agent, @Nonnull ReadonlyAgentPermissions permissions) {
        super(agent.getRole());
        
        assert permissions.isFrozen() : "The permissions are frozen.";
        
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
    private AgentPermissionsAdd(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(2);
        this.agent = Agent.get(entity.toNonHostEntity(), elements.getNotNull(0));
        this.permissions = new AgentPermissions(elements.getNotNull(1)).freeze();
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new TupleWrapper(TYPE, agent, permissions).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Adds the permissions " + permissions + " to the agent with the number " + agent + ".";
    }
    
    
    @Pure
    @Override
    public @Nonnull ReadonlyAgentPermissions getRequiredPermissions() {
        return permissions;
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
    @NonCommitting
    protected void executeOnBoth() throws SQLException {
        agent.addPermissionsForActions(permissions);
    }
    
    @Pure
    @Override
    public boolean interferesWith(@Nonnull Action action) {
        return action instanceof AgentPermissionsAdd && ((AgentPermissionsAdd) action).agent.equals(agent) || action instanceof AgentPermissionsRemove && ((AgentPermissionsRemove) action).agent.equals(agent);
    }
    
    @Pure
    @Override
    public @Nonnull AgentPermissionsRemove getReverse() {
        return new AgentPermissionsRemove(agent, permissions);
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        return protectedEquals(object) && object instanceof AgentPermissionsAdd && this.agent.equals(((AgentPermissionsAdd) object).agent) && this.permissions.equals(((AgentPermissionsAdd) object).permissions);
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
            return new AgentPermissionsAdd(entity, signature, recipient, block);
        }
        
    }
    
}
