package ch.virtualid.attribute;

import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.concepts.Certificate;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identifier.InternalIdentifier;
import ch.virtualid.identifier.InternalNonHostIdentifier;
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
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class CertifiedAttributeValue extends AttributeValue implements Immutable, Blockable, SQLizable {
    
    /**
     * Stores the signature of this attribute value.
     * 
     * @invariant !isCertified() || signature instanceof HostSignatureWrapper : "If this attribute value is certified, it is signed by a host.";
     */
    private final @Nonnull HostSignatureWrapper signature;
    
    /**
     * Creates a certified attribute value with the given content.
     * 
     * @param content the content of the subject which is certified by the issuer.
     * @param subject the identifier of the identity whose content is certified.
     * @param issuer the identifier of the identity which issues the certificate.
     * 
     * @require content.getType().isAttributeType() : "The type of the content denotes an attribute.";
     * @require Server.hasHost(issuer.getHostIdentifier()) : "The host of the issuer is running on this server.";
     * 
     * @ensure isVerified() : "The signature of this attribute value is verified.";
     */
    public CertifiedAttributeValue(@Nonnull Block content, @Nonnull InternalIdentifier subject, @Nonnull InternalNonHostIdentifier issuer) {
        super(content);
        
        this.signature = new HostSignatureWrapper(AttributeValue.TYPE, new SelfcontainedWrapper(AttributeValue.CONTENT, content), subject, null, issuer);
    }
    
    /**
     * Creates a certified attribute value with the given content.
     * 
     * @param content the content of the subject which is certified by the issuer.
     * @param subject the identifier of the identity whose content is certified.
     * @param issuer the identifier of the identity which issues the certificate.
     * 
     * @require content.getType().isAttributeType() : "The type of the content denotes an attribute.";
     * @require Server.hasHost(issuer.getHostIdentifier()) : "The host of the issuer is running on this server.";
     * 
     * @ensure isVerified() : "The signature of this attribute value is verified.";
     */
    public CertifiedAttributeValue(@Nonnull Blockable content, @Nonnull InternalIdentifier subject, @Nonnull InternalNonHostIdentifier issuer) {
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
    CertifiedAttributeValue(@Nonnull Block content, @Nonnull SignatureWrapper signature) throws InvalidEncodingException {
        super(content);
        
        if (signature instanceof HostSignatureWrapper) this.signature = (HostSignatureWrapper) signature;
        else throw new InvalidEncodingException("Certified attribute values have to be signed by a host.");
        if (this.signature.getSigner() instanceof HostIdentifier) throw new InvalidEncodingException("The issuer of a certificate may not be a host.");
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
    public @Nonnull InternalIdentifier getSubject() {
        return signature.getSubjectNotNull();
    }
    
    /**
     * Returns the issuer of this attribute value's certificate.
     * 
     * @return the issuer of this attribute value's certificate.
     */
    @Pure
    public @Nonnull InternalNonHostIdentifier getIssuer() {
        return (InternalNonHostIdentifier) signature.getSigner();
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
        Certificate.isAuthorized(getIssuer().getIdentity(), getContent());
    }
    
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Attribute value of type " + getContent().getType().getAddress() + " certified by " + getIssuer() + " for " + getSubject();
    }
    
}
