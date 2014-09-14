package ch.virtualid.handler;

import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.packet.PacketException;
import ch.xdf.Block;
import ch.xdf.SignatureWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Description.
 * 
 * - Internal actions can be undone and reversed.
 * - Internal actions have to be signed by the client directly or with credentials in a role. -> Only true for the core service!
 * 
 * => Internal actions have an audit request or trail appended.
 * 
 * Not all internal actions can be reversed? Consider, for example, SendMessage in a messaging service. -> method isReversible()
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public abstract class InternalAction extends Action implements InternalMethod {
    
    /**
     * Creates an internal action that decodes the given signature and block for the given entity.
     * 
     * @param connection an open connection to the database.
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param block the element of the content.
     * @param recipient the recipient of this handler.
     * 
     * @require !connection.isOnBoth() : "The decoding of sendable handlers is site-specific.";
     * @require !connection.isOnClient() || entity instanceof Role : "On the client-side, the entity is a role.";
     * @require !connection.isOnHost() || entity instanceof Identity : "On the host-side, the entity is an identity.";
     * @require !connection.isOnClient() || ((Role) entity).isOnSame(connection) : "On the client-side, the role is on the same site.";
     * @require signature.getSubject() != null : "The subject of the signature is not null.";
     * 
     * @ensure getEntity() != null : "The entity of this handler is not null.";
     * @ensure getSignature() != null : "The signature of this handler is not null.";
     * @ensure (!getSubject().getIdentity().equals(entity.getIdentity())) : "The identity of the entity and the subject are the same.";
     */
    protected InternalAction(@Nonnull ConnecSitection, @Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull Block block, @Nonnull HostIdentifier recipient) throws InvalidEncodingException, FailedIdentityException {
        super(connection, entity, signature, block, recipient);
        
        if (!getSubject().getIdentity().equals(entity.getIdentity())) throw new InvalidEncodingException("The identity of the entity and the subject have to be the same for internal actions.");
    }
    
    /**
     * Creates an internal action that encodes the content of a packet to the given recipient about the given subject.
     * 
     * @param connection an open connection to the database.
     * @param role the role to which this handler belongs.
     * @param recipient the recipient of this handler.
     * 
     * @ensure getEntity() != null : "The entity of this handler is not null.";
     * @ensure isOnClient() : "This internal action is on the client-side.";
     */
    protected InternalAction(@Nonnull ClientClientSitection, @Nonnull Role role, @Nonnull HostIdentifier recipient) {
        super(connection, role, role.getIdentity().getAddress(), recipient);
        // TODO: Ensure that the connection's client is the same as the one in the role; @require !connection.isOnClient() || ((Role) entity).isOnSame(connection) : "On the client-side, the role is on the same site.";
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
     * @require getEntity() instanceof Account : "This method is called on a host.";
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
     * @require getEntity() instanceof Role : "This method is called on a client.";
     */
    public abstract void executeOnClient() throws SQLException;
    
    /**
     * Returns the reverse of this action.
     * 
     * @return the reverse of this action.
     */
    public abstract @Nonnull InternalAction getReverse();
    
    /**
     * Reverses this internal action on the client.
     */
    public final void reverseOnClient() throws SQLException {
        getReverse().executeOnClient();
    }
    
}
