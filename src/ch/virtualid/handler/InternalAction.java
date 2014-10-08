package ch.virtualid.handler;

import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Synchronizer;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.external.IdentityNotFoundException;
import ch.virtualid.exceptions.external.InvalidDeclarationException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.action.internal.CoreServiceInternalAction;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.util.FreezableLinkedList;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.SignatureWrapper;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Internal actions can only be sent by {@link Client clients} and can usually be {@link #reverseOnClient() reversed}.
 * They are always signed identity-based and an audit request or trail is appended during {@link Package packaging}.
 * <p>
 * <em>Important:</em> Do not execute internal actions directly but always pass them to the {@link Synchronizer#execute(ch.virtualid.handler.InternalAction) Synchronizer}!
 * 
 * @invariant getEntity() != null : "The entity of this internal action is not null.";
 * @invariant getEntityNotNull().getIdentity().equals(getSubject().getIdentity()) : "The identity of the entity and the subject are the same.";
 * 
 * @see CoreServiceInternalAction
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class InternalAction extends Action implements InternalMethod {
    
    /**
     * Creates an internal action that encodes the content of a packet for the given recipient.
     * 
     * @param role the role to which this handler belongs.
     * @param recipient the recipient of this method.
     */
    protected InternalAction(@Nonnull Role role, @Nonnull HostIdentifier recipient) {
        super(role, role.getIdentity().getAddress(), recipient);
    }
    
    /**
     * Creates an internal action that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param recipient the recipient of this method.
     * 
     * @require signature.getSubject() != null : "The subject of the signature is not null.";
     * 
     * @ensure getSignature() != null : "The signature of this handler is not null.";
     */
    protected InternalAction(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient) throws InvalidEncodingException, SQLException, IdentityNotFoundException, InvalidDeclarationException {
        super(entity, signature, recipient);
        
        if (!getEntityNotNull().getIdentity().equals(getSubject().getIdentity())) throw new InvalidEncodingException("The identity of the entity and the subject have to be the same for internal actions.");
    }
    
    
    @Pure
    @Override
    public boolean isSimilarTo(@Nonnull Method other) {
        return super.isSimilarTo(other) && other instanceof InternalAction;
    }
    
    @Pure
    @Override
    public final boolean canBeSentByHosts() {
        return false;
    }
    
    @Pure
    @Override
    public final boolean canOnlyBeSentByHosts() {
        return false;
    }
    
    
    /**
     * Executes this internal action on the host.
     * 
     * @throws PacketException if the authorization is not sufficient.
     * 
     * @require isOnHost() : "This method is called on a host.";
     * @require getSignature() != null : "The signature of this handler is not null.";
     */
    protected abstract void executeOnHostInternalAction() throws PacketException, SQLException;
    
    @Pure
    @Override
    public final @Nullable Class<? extends ActionReply> getReplyClass() {
        return null;
    }
    
    @Override
    public final @Nullable ActionReply executeOnHost() throws PacketException, SQLException {
        executeOnHostInternalAction();
        return null;
    }
    
    /**
     * Returns the reverse of this action or null if this action cannot be reversed.
     * 
     * @return the reverse of this action or null if this action cannot be reversed.
     * 
     * @require isOnClient() : "This method is called on a client.";
     */
    @Pure
    public abstract @Nullable InternalAction getReverse();
    
    /**
     * Reverses this internal action on the client if this action can be reversed.
     * 
     * @require isOnClient() : "This method is called on a client.";
     */
    public final void reverseOnClient() throws SQLException {
        final @Nullable InternalAction reverse = getReverse();
        if (reverse != null) reverse.executeOnClient();
    }
    
    
    /**
     * Returns the module on which this action operates.
     * 
     * @return the module on which this action operates.
     */
    @Pure
    public abstract @Nonnull SemanticType getModule();
    
    /**
     * Stores an empty list of semantic types.
     */
    private static final @Nonnull ReadonlyList<SemanticType> emptyList = new FreezableLinkedList<SemanticType>().freeze();
    
    /**
     * Returns the modules that need to be reloaded and are thus suspended.
     * 
     * @return the modules that need to be reloaded and are thus suspended.
     */
    @Pure
    public @Nonnull ReadonlyList<SemanticType> suspendModules() {
        return emptyList;
    }
    
}
