package net.digitalid.core.signature.attribute;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Invariant;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.auxiliary.Time;

import net.digitalid.core.selfcontained.Selfcontained;
import net.digitalid.core.signature.exceptions.InactiveSignatureException;
import net.digitalid.core.signature.host.HostSignature;

/**
 * This class facilitates the encoding and decoding of certified attribute values.
 * 
 * @invariant getContent().getType().isAttributeFor(getSubject().getCategory()) : "The content is an attribute for the subject.";
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
//@GenerateConverter // The generic signature cannot yet be converted.
public abstract class CertifiedAttributeValue extends AttributeValue {
    
    /* -------------------------------------------------- Signature -------------------------------------------------- */
    
    @Pure
    @Override
    public abstract @Nonnull @Invariant(condition = "signature.getElement().getSemanticType().isAttributeType()", message = "The type of the selfcontained value denotes an attribute.") HostSignature<Selfcontained> getSignature();
    
    // TODO: Check somewhere that:
//        Require.that(content.getType().isAttributeFor(subject.getCategory())).orThrow("The content is an attribute for the subject.");
        
    // TODO: The signature needs to be created somewhere:
//     * @require Server.hasHost(issuer.getAddress().getHostIdentifier()) : "The host of the issuer is running on this server.";
//        this.signature = HostSignatureWrapper.sign(AttributeValue.TYPE, SelfcontainedWrapper.encodeNonNullable(AttributeValue.CONTENT, content), subject.getAddress(), null, issuer.getAddress());
    
    /* -------------------------------------------------- Verification -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    public void verify() throws ExternalException {
        // TODO:
//        getSignature().verify();
//        Certificate.isAuthorized(getIssuer(), getContent());
    }
    
    /* -------------------------------------------------- Validity -------------------------------------------------- */
    
    /**
     * Returns whether the certificate of this attribute value is valid at the given time.
     */
    @Pure
    public boolean isValid(@Nonnull Time time) {
        return getSignature().getTime().add(getSignature().getElement().getSemanticType().getCachingPeriodNotNull()).isGreaterThan(time);
    }
    
    /**
     * Checks that the certificate of this attribute value is valid at the given time.
     * 
     * @throws InactiveSignatureException if the certificate is not valid at the given time.
     */
    @Pure
    public void checkIsValid(@Nonnull Time time) throws InactiveSignatureException {
        if (!isValid(time)) { throw InactiveSignatureException.get(getSignature()); }
    }
    
}
