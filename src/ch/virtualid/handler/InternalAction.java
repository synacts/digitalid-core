package ch.virtualid.handler;

import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Client;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.action.internal.CoreServiceInternalAction;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.synchronizer.Synchronizer;
import ch.xdf.SignatureWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Internal actions can only be sent by {@link Client clients} and can usually be {@link #reverseOnClient() reversed}.
 * They are always signed identity-based and an audit request or trail is appended during {@link Package packaging}.
 * <p>
 * <em>Important:</em> Do not execute internal actions directly but always pass them to the {@link Synchronizer#execute(ch.virtualid.handler.InternalAction) Synchronizer}!
 * 
 * @invariant hasEntity() : "This internal action has an entity.";
 * @invariant isNonHost() : "This internal action belongs to a non-host.";
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
     * @require signature.hasSubject() : "The signature has a subject.";
     * 
     * @ensure hasSignature() : "This handler has a signature.";
     */
    protected InternalAction(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        if (!isNonHost()) throw new PacketException(PacketError.IDENTIFIER, "Internal actions have to belong to a non-host.");
        if (!getEntityNotNull().getIdentity().equals(getSubject().getIdentity())) throw new PacketException(PacketError.IDENTIFIER, "The identity of the entity and the subject have to be the same for internal actions.");
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
     * @require hasSignature() : "This handler has a signature.";
     */
    protected abstract void executeOnHostInternalAction() throws PacketException, SQLException;
    
    @Override
    public final @Nullable ActionReply executeOnHost() throws PacketException, SQLException {
        executeOnHostInternalAction();
        return null;
    }
    
    @Pure
    @Override
    public final boolean matches(@Nullable Reply reply) {
        return reply == null;
    }
    
    
    /**
     * Returns whether this internal action interferes with the given action.
     * 
     * @param action the action which is to be checked for interference.
     * 
     * @return whether this internal action interferes with the given action.
     * 
     * @require action.getRole().equals(getRole()) : "The role of the given and this action is the same.";
     * @require action.getService().equals(getService()) : "The service of the given and this action is the same.";
     */
    public abstract boolean interferesWith(@Nonnull Action action);
    
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
        assert isOnClient() : "This method is called on a client.";
        
        final @Nullable InternalAction reverse = getReverse();
        if (reverse != null) reverse.executeOnClient();
    }
    
}
