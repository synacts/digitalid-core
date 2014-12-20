package ch.virtualid.attribute;

import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.concepts.Certificate;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identity.InternalIdentity;
import ch.virtualid.identity.InternalNonHostIdentity;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.xdf.Block;
import ch.xdf.HostSignatureWrapper;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.SignatureWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This class facilitates the encoding and decoding of certified attribute values.
 * 
 * @invariant getContent().getType().isAttributeFor(getSubject().getCategory()) : "The content is an attribute for the subject.";
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class CertifiedAttributeValue extends AttributeValue implements Immutable, Blockable, SQLizable {
    
    /**
     * Stores the signature of this attribute value.
     */
    private final @Nonnull HostSignatureWrapper signature;
    
    /**
     * Stores the subject of this attribute value.
     */
    private final @Nonnull InternalIdentity subject;
    
    /**
     * Stores the issuer of this attribute value.
     */
    private final @Nonnull InternalNonHostIdentity issuer;
    
    /**
     * Creates a certified attribute value with the given content.
     * 
     * @param content the content to be certified by the issuer.
     * @param subject the identity whose content is certified.
     * @param issuer the identity which issues the certificate.
     * 
     * @require content.getType().isAttributeFor(subject.getCategory()) : "The content is an attribute for the subject.";
     * @require Server.hasHost(issuer.getAddress().getHostIdentifier()) : "The host of the issuer is running on this server.";
     * 
     * @ensure isVerified() : "The signature of this attribute value is verified.";
     */
    public CertifiedAttributeValue(@Nonnull Block content, @Nonnull InternalIdentity subject, @Nonnull InternalNonHostIdentity issuer) {
        super(content);
        
        assert content.getType().isAttributeFor(subject.getCategory()) : "The content is an attribute for the subject.";
        
        this.signature = new HostSignatureWrapper(AttributeValue.TYPE, new SelfcontainedWrapper(AttributeValue.CONTENT, content), subject.getAddress(), null, issuer.getAddress());
        this.subject = subject;
        this.issuer = issuer;
    }
    
    /**
     * Creates a certified attribute value with the given content.
     * 
     * @param content the content to be certified by the issuer.
     * @param subject the identity whose content is certified.
     * @param issuer the identity which issues the certificate.
     * 
     * @require content.getType().isAttributeFor(subject.getCategory()) : "The content is an attribute for the subject.";
     * @require Server.hasHost(issuer.getAddress().getHostIdentifier()) : "The host of the issuer is running on this server.";
     * 
     * @ensure isVerified() : "The signature of this attribute value is verified.";
     */
    public CertifiedAttributeValue(@Nonnull Blockable content, @Nonnull InternalIdentity subject, @Nonnull InternalNonHostIdentity issuer) {
        this(content.toBlock(), subject, issuer);
    }
    
    /**
     * Creates a certified attribute value with the given content and signature.
     * 
     * @param content the content of the certified attribute value.
     * @param signature the signature of the certified attribute value.
     * 
     * @require content.getType().isAttributeType() : "The type of the content denotes an attribute.";
     * @require signature.isSigned() : "The signature is signed.";
     */
    CertifiedAttributeValue(@Nonnull Block content, @Nonnull SignatureWrapper signature) throws SQLException, IOException, PacketException, ExternalException {
        super(content);
        
        if (signature instanceof HostSignatureWrapper) this.signature = (HostSignatureWrapper) signature;
        else throw new InvalidEncodingException("Certified attribute values have to be signed by a host.");
        this.subject = this.signature.getSubjectNotNull().getIdentity();
        this.issuer = this.signature.getSigner().getIdentity().toInternalNonHostIdentity();
        if (!content.getType().isAttributeFor(subject.getCategory())) throw new InvalidEncodingException("The content has to be an attribute for the subject.");
    }
    
    
    @Pure
    @Override
    public boolean matches(@Nonnull Attribute attribute) {
        return super.matches(attribute) && attribute.getAccount().getIdentity().equals(subject);
    }
    
    
    @Pure
    @Override
    public @Nonnull HostSignatureWrapper getSignature() {
        return signature;
    }
    
    @Pure
    @Override
    public void verify() throws SQLException, IOException, PacketException, ExternalException {
        signature.verify();
        Certificate.isAuthorized(getIssuer(), getContent());
    }
    
    
    /**
     * Returns the time when this attribute value was certified.
     * 
     * @return the time when this attribute value was certified.
     */
    @Pure
    public @Nonnull Time getTime() {
        return signature.getTimeNotNull();
    }
    
    /**
     * Returns the subject of this attribute value's certificate.
     * 
     * @return the subject of this attribute value's certificate.
     */
    @Pure
    public @Nonnull InternalIdentity getSubject() {
        return subject;
    }
    
    /**
     * Returns the issuer of this attribute value's certificate.
     * 
     * @return the issuer of this attribute value's certificate.
     */
    @Pure
    public @Nonnull InternalNonHostIdentity getIssuer() {
        return issuer;
    }
    
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Attribute value of type " + getContent().getType().getAddress() + " certified by " + getIssuer().getAddress() + " for " + getSubject().getAddress();
    }
    
}
