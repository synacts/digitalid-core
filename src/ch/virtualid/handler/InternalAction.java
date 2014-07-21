package ch.virtualid.handler;

import ch.virtualid.contact.Authentications;
import ch.virtualid.entity.Role;
import ch.virtualid.entity.Entity;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.packet.PacketException;
import ch.xdf.Block;
import ch.xdf.CredentialsSignatureWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

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
public abstract class InternalAction extends Action implements InternalHandler {
    
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
    
    
    @Override
    public boolean isSimilarTo(@Nonnull SendableHandler other) {
        return super.isSimilarTo(other) && other instanceof InternalAction;
    }
    
    @Override
    public final boolean canBeSentByHost() {
        return false;
    }
    
    @Override
    public final boolean canOnlyBeSentByHost() {
        return false;
    }
    
    
    /**
     * @ensure return.equals(Authentications.IDENTITY_BASED) : "Internal actions are always identity-based.";
     */
    @Override
    public final @Nonnull Authentications getDesiredAuthentications() {
        return Authentications.IDENTITY_BASED;
    }
    
    
    /**
     * Executes this handler on the client.
     * 
     * @throws SQLException if this handler cannot be executed on the client or another problem occurs.
     * 
     * @require isOnClient() : "This method should only be called on the client-side.";
     */
    public abstract void executeOnClient() throws SQLException;
    
    
    /**
     * Executes this handler on the host.
     * 
     * @throws PacketException if this handler cannot be executed on the host or the authorization is insufficient.
     * 
     * @return a pair of reply and audit, where both of them can be null.
     * 
     * @require isOnHost() : "This method should only be called on the host-side.";
     */
    public abstract void excecuteOnHost(@Nonnull CredentialsSignatureWrapper credentialsSignature) throws PacketException;
    
    
    public final void reverseOnClient() throws SQLException {
        getReverse().executeOnClient();
    }
    
    public abstract @Nonnull InternalAction getReverse();
    
}
