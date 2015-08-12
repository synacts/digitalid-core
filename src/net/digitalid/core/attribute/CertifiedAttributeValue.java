package net.digitalid.core.attribute;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Locked;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.certificate.Certificate;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.exceptions.external.InvalidSignatureException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identity.InternalIdentity;
import net.digitalid.core.identity.InternalNonHostIdentity;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.Blockable;
import net.digitalid.core.wrappers.HostSignatureWrapper;
import net.digitalid.core.wrappers.SelfcontainedWrapper;
import net.digitalid.core.wrappers.SignatureWrapper;

/**
 * This class facilitates the encoding and decoding of certified attribute values.
 * 
 * @invariant getContent().getType().isAttributeFor(getSubject().getCategory()) : "The content is an attribute for the subject.";
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class CertifiedAttributeValue extends AttributeValue {
    
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
    @NonCommitting
    CertifiedAttributeValue(@Nonnull Block content, @Nonnull SignatureWrapper signature) throws SQLException, IOException, PacketException, ExternalException {
        super(content);
        
        if (signature instanceof HostSignatureWrapper) this.signature = (HostSignatureWrapper) signature;
        else throw new InvalidEncodingException("Certified attribute values have to be signed by a host.");
        this.subject = this.signature.getNonNullableSubject().getIdentity();
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
    @Locked
    @Override
    @NonCommitting
    public void verify() throws SQLException, IOException, PacketException, ExternalException {
        signature.verify();
        Certificate.isAuthorized(getIssuer(), getContent());
    }
    
    
    /**
     * Returns the time when this attribute value was certified.
     * 
     * @return the time when this attribute value was certified.
     * 
     * @ensure time.isPositive() : "The time is positive.";
     */
    @Pure
    public @Nonnull Time getTime() {
        return signature.getNonNullableTime();
    }
    
    /**
     * Returns whether the certificate of this attribute value is valid the given time.
     * 
     * @param time the time of interest.
     * 
     * @return whether the certificate of this attribute value is valid the given time.
     */
    @Pure
    public boolean isValid(@Nonnull Time time) {
        return getTime().add(getContent().getType().getCachingPeriodNotNull()).isGreaterThan(time);
    }
    
    /**
     * Checks that the certificate of this attribute value is valid the given time.
     * 
     * @param time the time of interest.
     * 
     * @throws InvalidSignatureException if the certificate is not valid the given time.
     */
    @Pure
    public void checkIsValid(@Nonnull Time time) throws InvalidSignatureException {
        if (!isValid(time)) throw new InvalidSignatureException("The certificate is no longer valid.");
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
     * Checks that the subject of this attribute value equals the given subject.
     * 
     * @param subject the subject of interest.
     * 
     * @throws InvalidSignatureException if the subject is not equal the given subject.
     */
    @Pure
    public void checkSubject(@Nonnull InternalIdentity subject) throws InvalidSignatureException {
        if (!this.subject.equals(subject)) throw new InvalidSignatureException("The attribute is certified for the wrong subject.");
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
