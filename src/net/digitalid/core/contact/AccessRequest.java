package net.digitalid.core.contact;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.core.agent.Restrictions;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.exceptions.packet.PacketError;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.handler.Method;
import net.digitalid.core.handler.Reply;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identity.InternalPerson;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.data.StateModule;
import net.digitalid.core.service.CoreServiceActionReply;
import net.digitalid.core.service.CoreServiceExternalAction;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.ClientSignatureWrapper;
import net.digitalid.core.wrappers.CredentialsSignatureWrapper;
import net.digitalid.core.wrappers.SignatureWrapper;

/**
 * Requests the given permissions of the given subject.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.9
 */
@Immutable
public final class AccessRequest extends CoreServiceExternalAction {
    
    /**
     * Stores the semantic type {@code request.access@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("request.access@core.digitalid.net").load(FreezableContactPermissions.TYPE);
    
    
    /**
     * Stores the person of this access request.
     */
    private final @Nonnull InternalPerson person;
    
    /**
     * Stores the permissions of this access request.
     * 
     * @invariant permissions.isFrozen() : "The permissions are frozen.";
     * @invariant !permissions.isEmpty() : "The permissions are not empty.";
     */
    private final @Nonnull ReadOnlyContactPermissions permissions;
    
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
    AccessRequest(@Nonnull NonHostEntity entity, @Nonnull InternalPerson subject, @Nonnull ReadOnlyContactPermissions permissions) {
        super(entity, subject);
        
        assert permissions.isFrozen() : "The permissions are frozen.";
        assert !permissions.isEmpty() : "The permissions are not empty.";
        
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
    @NonCommitting
    private AccessRequest(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        this.person = entity.getIdentity().toInternalPerson();
        this.permissions = new FreezableContactPermissions(block).freeze();
        if (permissions.isEmpty()) throw new InvalidEncodingException("The permissions may not be empty.");
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return permissions.toBlock().setType(TYPE);
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Requests access to " + permissions + ".";
    }
    
    
    /**
     * Returns the permissions of this access request.
     * 
     * @return the permissions of this access request.
     * 
     * @ensure return.isFrozen() : "The permissions are frozen.";
     * @ensure !return.isEmpty() : "The permissions are not empty.";
     */
    public @Nonnull ReadOnlyContactPermissions getPermissions() {
        return permissions;
    }
    
    
    @Pure
    @Override
    public boolean canOnlyBeSentByHosts() {
        return false;
    }
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissions() {
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
    
    
    @Override
    @NonCommitting
    public @Nullable CoreServiceActionReply executeOnHost() throws PacketException, SQLException {
        final @Nonnull SignatureWrapper signature = getSignatureNotNull();
        if (signature instanceof CredentialsSignatureWrapper) {
            ((CredentialsSignatureWrapper) signature).checkCover(getRequiredPermissions());
        } else if (signature instanceof ClientSignatureWrapper) {
            throw new PacketException(PacketError.AUTHORIZATION, "Access requests may not be signed by clients.");
        }
        executeOnClient();
        return null;
    }
    
    @Pure
    @Override
    public boolean matches(@Nullable Reply reply) {
        return reply == null;
    }
    
    
    @Override
    @NonCommitting
    public void executeOnClient() throws SQLException {
        // TODO: Add this access request to a list of pending access requests.
    }
    
    @Override
    @NonCommitting
    public void executeOnFailure() throws SQLException {
        // TODO: Add this access request to a list of failed access requests.
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        return protectedEquals(object) && object instanceof AccessRequest && this.permissions.equals(((AccessRequest) object).permissions);
    }
    
    @Pure
    @Override
    public int hashCode() {
        return 89 * protectedHashCode() + permissions.hashCode();
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull StateModule getModule() {
        return AccessModule.MODULE;
    }
    
    /**
     * The factory class for the surrounding method.
     */
    private static final class Factory extends Method.Factory {
        
        static { Method.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        @NonCommitting
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
            return new AccessRequest(entity, signature, recipient, block);
        }
        
    }
    
}
