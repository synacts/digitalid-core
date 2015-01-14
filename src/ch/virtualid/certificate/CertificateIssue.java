package ch.virtualid.certificate;

import ch.virtualid.service.CoreServiceExternalAction;
import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.attribute.AttributeValue;
import ch.virtualid.attribute.CertifiedAttributeValue;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.NonHostAccount;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.ActionReply;
import ch.virtualid.handler.Method;
import ch.virtualid.service.CoreServiceActionReply;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identity.InternalIdentity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.BothModule;
import ch.virtualid.attribute.AttributeModule;
import ch.xdf.Block;
import ch.xdf.HostSignatureWrapper;
import ch.xdf.SignatureWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Issues the given certificate to the given subject.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.6
 */
public final class CertificateIssue extends CoreServiceExternalAction {
    
    /**
     * Stores the semantic type {@code issuance.certificate@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("issuance.certificate@virtualid.ch").load(AttributeValue.TYPE);
    
    
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
    public @Nonnull String toString() {
        return "Issues a certificate for " + certificate.getContent().getType().getAddress() + ".";
    }
    
    
    @Pure
    @Override
    public @Nonnull ReadonlyAgentPermissions getAuditPermissions() {
        return new AgentPermissions(certificate.getContent().getType(), false).freeze();
    }
    
    
    @Pure
    @Override
    public @Nullable Class<CoreServiceActionReply> getReplyClass() {
        return null;
    }
    
    /**
     * Executes this action on both hosts and clients.
     */
    private void executeOnBoth() throws SQLException {
        // TODO: Replace the attribute with the certificate if they match. Otherwise, this should be reported as a packet error (at least on host).
    }
    
    @Override
    public @Nullable ActionReply executeOnHost() throws PacketException, SQLException {
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
    
    @Override
    public void executeOnClient() throws SQLException {
        // TODO: I'm not sure how far the checks should go on the client.
        
        executeOnBoth();
    }
    
    @Override
    public void executeOnFailure() throws SQLException {
        // TODO!
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
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
            return new CertificateIssue(entity, signature, recipient, block);
        }
        
    }
    
}
