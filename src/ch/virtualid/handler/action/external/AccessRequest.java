package ch.virtualid.handler.action.external;

import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.contact.Contact;
import ch.virtualid.contact.ContactPermissions;
import ch.virtualid.contact.ReadonlyContactPermissions;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.NonHostEntity;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.ActionReply;
import ch.virtualid.handler.Method;
import ch.virtualid.handler.reply.action.CoreServiceActionReply;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identity.InternalPerson;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.BothModule;
import ch.virtualid.module.CoreService;
import ch.xdf.Block;
import ch.xdf.ClientSignatureWrapper;
import ch.xdf.CredentialsSignatureWrapper;
import ch.xdf.SignatureWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Requests the given permissions of the given subject.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.9
 */
public final class AccessRequest extends CoreServiceExternalAction {
    
    /**
     * Stores the semantic type {@code request.access@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("request.access@virtualid.ch").load(ContactPermissions.TYPE);
    
    
    /**
     * Stores the person of this access request.
     */
    private final @Nonnull InternalPerson person;
    
    /**
     * Stores the permissions of this access request.
     * 
     * @invariant permissions.isFrozen() : "The permissions are frozen.";
     * @invariant permissions.isNotEmpty() : "The permissions are not empty.";
     */
    private final @Nonnull ReadonlyContactPermissions permissions;
    
    /**
     * Creates an external action to request the given permissions of the given subject.
     * 
     * @param entity the entity to which this handler belongs.
     * @param subject the subject of this handler.
     * @param permissions the requested permissions.
     * 
     * @require !(entity instanceof Account) || canBeSentByHosts() : "Methods encoded on hosts can be sent by hosts.";
     * @require !(entity instanceof Role) || !canOnlyBeSentByHosts() : "Methods encoded on clients cannot only be sent by hosts.";
     * 
     * @require permissions.isFrozen() : "The permissions are frozen.";
     * @require !permissions.isEmpty() : "The permissions are not empty.";
     */
    public AccessRequest(@Nonnull NonHostEntity entity, @Nonnull InternalPerson subject, @Nonnull ReadonlyContactPermissions permissions) {
        super(entity, subject);
        
        assert permissions.isFrozen() : "The permissions are frozen.";
        assert permissions.isNotEmpty() : "The permissions are not empty.";
        
        this.person = subject;
        this.permissions = permissions;
    }
    
    /**
     * Creates an external action that decodes the given block.
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
    private AccessRequest(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        this.person = entity.getIdentity().toInternalPerson();
        this.permissions = new ContactPermissions(block).freeze();
        if (permissions.isEmpty()) throw new InvalidEncodingException("The permissions may not be empty.");
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return permissions.toBlock().setType(TYPE);
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Requests access to " + permissions + ".";
    }
    
    
    /**
     * Returns the permissions of this access request.
     * 
     * @return the permissions of this access request.
     * 
     * @ensure return.isFrozen() : "The permissions are frozen.";
     * @ensure return.isNotEmpty() : "The permissions are not empty.";
     */
    public @Nonnull ReadonlyContactPermissions getPermissions() {
        return permissions;
    }
    
    
    @Pure
    @Override
    public boolean canOnlyBeSentByHosts() {
        return false;
    }
    
    @Pure
    @Override
    public @Nonnull ReadonlyAgentPermissions getRequiredPermissions() {
        return permissions.toAgentPermissions().freeze();
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getAuditRestrictions() {
        return new Restrictions(false, false, true);
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getFailedAuditRestrictions() {
        return new Restrictions(false, false, true, Contact.get(getNonHostEntity(), person));
    }
    
    
    @Pure
    @Override
    public @Nullable Class<CoreServiceActionReply> getReplyClass() {
        return null;
    }
    
    @Override
    public @Nullable ActionReply executeOnHost() throws PacketException, SQLException {
        final @Nonnull SignatureWrapper signature = getSignatureNotNull();
        if (signature instanceof CredentialsSignatureWrapper) {
            ((CredentialsSignatureWrapper) signature).checkCover(getRequiredPermissions());
        } else if (signature instanceof ClientSignatureWrapper) {
            throw new PacketException(PacketError.AUTHORIZATION, "Access requests may not be signed by clients.");
        }
        return null;
    }
    
    @Override
    public void executeOnClient() throws SQLException {
        // TODO: Add this access request to a list of pending access requests.
    }
    
    @Override
    public void executeOnFailure() throws SQLException {
        // TODO: Add this access request to a list of failed access requests.
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull BothModule getModule() {
        return CoreService.SERVICE;
    }
    
    /**
     * The factory class for the surrounding method.
     */
    private static final class Factory extends Method.Factory {
        
        static { Method.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
            return new AccessRequest(entity, signature, recipient, block);
        }
        
    }
    
}
