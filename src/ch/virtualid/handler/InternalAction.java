package ch.virtualid.handler;

import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Synchronizer;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.InvalidDeclarationException;
import ch.virtualid.handler.action.internal.CoreServiceInternalAction;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.packet.PacketException;
import ch.xdf.SignatureWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
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
    protected InternalAction(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient) throws InvalidEncodingException, SQLException, FailedIdentityException, InvalidDeclarationException {
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
     * Executes this method on the host.
     * 
     * @throws PacketException if the authorization is not sufficient.
     * 
     * @require isOnHost() : "This method is called on a host.";
     */
    public abstract void excecuteOnHost() throws PacketException, SQLException;
    
    @Override
    public @Nullable ActionReply excecute() throws PacketException, SQLException {
        excecuteOnHost();
        return null;
    }
    
    /**
     * Executes this internal action on the client.
     * 
     * @throws SQLException if this handler cannot be executed.
     * 
     * @require isOnClient() : "This method is called on a client.";
     */
    public abstract void executeOnClient() throws SQLException;
    
    /**
     * Returns the reverse of this action or null if this action cannot be reversed.
     * 
     * @return the reverse of this action or null if this action cannot be reversed.
     */
    public abstract @Nullable InternalAction getReverse();
    
    /**
     * Reverses this internal action on the client if this action can be reversed.
     */
    public final void reverseOnClient() throws SQLException {
        final @Nullable InternalAction reverse = getReverse();
        if (reverse != null) reverse.executeOnClient();
    }
    
}
