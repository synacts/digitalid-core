package net.digitalid.core.certificate;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.agent.AgentPermissions;
import net.digitalid.core.agent.ReadonlyAgentPermissions;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.attribute.AttributeModule;
import net.digitalid.core.attribute.AttributeValue;
import net.digitalid.core.attribute.CertifiedAttributeValue;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostAccount;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketError;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.handler.Method;
import net.digitalid.core.handler.Reply;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identity.InternalIdentity;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.module.BothModule;
import net.digitalid.core.service.CoreServiceActionReply;
import net.digitalid.core.service.CoreServiceExternalAction;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.HostSignatureWrapper;
import net.digitalid.core.wrappers.SignatureWrapper;

/**
 * Issues the given certificate to the given subject.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.6
 */
public final class CertificateIssue extends CoreServiceExternalAction {
    
    /**
     * Stores the semantic type {@code issuance.certificate@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("issuance.certificate@core.digitalid.net").load(AttributeValue.TYPE);
    
    
    /**
     * Stores the certificate that is issued.
     */
    private final @Nonnull CertifiedAttributeValue certificate;
    
    /**
     * Creates an external action to issue the given certificate to the given subject.
     * 
     * @param account the account to which this handler belongs.
     * @param subject the subject of this external action.
     * @param content the attribute content to certify.
     * 
     * @require content.getType().isAttributeFor(subject.getCategory()) : "The block is an attribute for the subject.";
     */
    public CertificateIssue(@Nonnull NonHostAccount account, @Nonnull InternalIdentity subject, @Nonnull Block content) {
        super(account, subject); // TODO: Replace the parameters with an instance of {@link Certificate}.
        
        this.certificate = new CertifiedAttributeValue(content, subject, account.getIdentity());
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
    private CertificateIssue(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        this.certificate = AttributeValue.get(block, false).toCertifiedAttributeValue();
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return certificate.toBlock().setType(TYPE);
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Issues a certificate for " + certificate.getContent().getType().getAddress() + ".";
    }
    
    
    /**
     * Returns the certificate that is issued.
     * 
     * @return the certificate that is issued.
     */
    @Pure
    public @Nonnull CertifiedAttributeValue getCertificate() {
        return certificate;
    }
    
    
    @Pure
    @Override
    public @Nonnull ReadonlyAgentPermissions getAuditPermissions() {
        return new AgentPermissions(certificate.getContent().getType(), false).freeze();
    }
    
    
    /**
     * Executes this action on both hosts and clients.
     */
    @NonCommitting
    private void executeOnBoth() throws SQLException {
        // TODO: Replace the attribute with the certificate if they match. Otherwise, this should be reported as a packet error (at least on host).
    }
    
    @Override
    @NonCommitting
    public @Nullable CoreServiceActionReply executeOnHost() throws PacketException, SQLException {
        final @Nonnull SignatureWrapper signature = getSignatureNotNull();
        if (!(signature instanceof HostSignatureWrapper)) throw new PacketException(PacketError.AUTHORIZATION, "TODO");
        
        try {
            certificate.verify();
        } catch (@Nonnull IOException | PacketException | ExternalException exception) { // TODO: What to do with the packet exception?
            throw new PacketException(PacketError.METHOD, "TODO", exception);
        }
        
        // TODO: Check other things like whether the signer is the one in the certificate.
        
        executeOnBoth();
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
        // TODO: I'm not sure how far the checks should go on the client.
        
        executeOnBoth();
    }
    
    @Override
    @NonCommitting
    public void executeOnFailure() throws SQLException {
        // TODO!
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        return protectedEquals(object) && object instanceof CertificateIssue && this.certificate.equals(((CertificateIssue) object).certificate);
    }
    
    @Pure
    @Override
    public int hashCode() {
        return 89 * protectedHashCode() + certificate.hashCode();
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull BothModule getModule() {
        return AttributeModule.MODULE;
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
            return new CertificateIssue(entity, signature, recipient, block);
        }
        
    }
    
}
