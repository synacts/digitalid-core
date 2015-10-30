package net.digitalid.service.core.handler;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.action.synchronizer.Audit;
import net.digitalid.service.core.block.wrappers.SignatureWrapper;
import net.digitalid.service.core.concepts.agent.Agent;
import net.digitalid.service.core.concepts.agent.FreezableAgentPermissions;
import net.digitalid.service.core.concepts.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.concepts.agent.Restrictions;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.identifier.InternalIdentifier;
import net.digitalid.service.core.site.client.AccountOpen;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.freezable.FreezableLinkedList;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * Actions affect the state of a digital identity and are thus always {@link Audit audited}.
 * The default is to sign them identity-based. If another behavior is desired, the method
 * {@link Method#send()} needs to be overridden. Actions are executed asynchronously.
 * 
 * @see InternalAction
 * @see ExternalAction
 */
@Immutable
public abstract class Action extends Method implements Auditable {
    
    /**
     * Creates an action that encodes the content of a packet for the given recipient about the given subject.
     * The entity is only null for {@link AccountOpen} which inherits directly from this class.
     * 
     * @param entity the entity to which this handler belongs.
     * @param subject the subject of this handler.
     * @param recipient the recipient of this method.
     * 
     * @require !(entity instanceof Account) || canBeSentByHosts() : "Methods encoded on hosts can be sent by hosts.";
     * @require !(entity instanceof Role) || !canOnlyBeSentByHosts() : "Methods encoded on clients cannot only be sent by hosts.";
     */
    protected Action(@Nullable NonHostEntity entity, @Nonnull InternalIdentifier subject, @Nonnull HostIdentifier recipient) {
        super(entity, subject, recipient);
    }
    
    /**
     * Creates an action that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param recipient the recipient of this method.
     * 
     * @require signature.hasSubject() : "The signature has a subject.";
     * 
     * @ensure hasEntity() : "This method has an entity.";
     * @ensure hasSignature() : "This handler has a signature.";
     */
    protected Action(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient) {
        super(entity, signature, recipient);
    }
    
    
    @Pure
    @Override
    public final boolean isLodged() {
        return true;
    }
    
    
    @Override
    @NonCommitting
    public abstract @Nullable ActionReply executeOnHost() throws PacketException, SQLException;
    
    
    /**
     * Executes this action on the client.
     * 
     * @throws AbortException if this handler cannot be executed.
     * 
     * @require isOnClient() : "This method is called on a client.";
     */
    @NonCommitting
    public abstract void executeOnClient() throws AbortException;
    
    /**
     * This method is executed after successful transmission.
     * 
     * @throws AbortException if this handler cannot be executed.
     */
    @NonCommitting
    public void executeOnSuccess() throws AbortException {}
    
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToSeeAudit() {
        return FreezableAgentPermissions.NONE;
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictionsToSeeAudit() {
        return Restrictions.MIN;
    }
    
    @Pure
    @Override
    public @Nullable Agent getRequiredAgentToSeeAudit() {
        return null;
    }
    
    
    /**
     * Returns the module on which this action operates.
     * 
     * @return the module on which this action operates.
     */
    @Pure
    public abstract @Nonnull StateModule getModule();
    
    /**
     * Stores an empty list of modules.
     */
    private static final @Nonnull ReadOnlyList<StateModule> emptyList = new FreezableLinkedList<StateModule>().freeze();
    
    /**
     * Returns the modules that need to be reloaded and are thus suspended.
     * 
     * @return the modules that need to be reloaded and are thus suspended.
     */
    @Pure
    public @Nonnull ReadOnlyList<StateModule> suspendModules() {
        return emptyList;
    }
    
}
