package net.digitalid.core.signature.attribute;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Invariant;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.pack.Pack;
import net.digitalid.core.signature.Signature;
import net.digitalid.core.signature.exceptions.InvalidSignatureException;
import net.digitalid.core.signature.host.HostSignature;

/**
 * This class facilitates the encoding and decoding of attribute values.
 * 
 * @see CertifiedAttributeValue
 * @see UncertifiedAttributeValue
 */
@Immutable
//@GenerateConverter // The generic signature cannot yet be converted.
public abstract class AttributeValue extends RootClass {
    
    /* -------------------------------------------------- Signature -------------------------------------------------- */
    
    /**
     * Returns the signature with the packed attribute value.
     */
    @Pure
    public abstract @Nonnull @Invariant(condition = "signature.getObject().getType().isAttributeType()", message = "The type of the packed value denotes an attribute.") Signature<Pack> getSignature();
    
    /**
     * Returns whether this attribute value is certified.
     */
    @Pure
    public boolean isCertified() {
        return this instanceof CertifiedAttributeValue;
    }
    
    /* -------------------------------------------------- Verification -------------------------------------------------- */
    
    /**
     * Verifies the signature of this attribute value.
     * 
     * @throws InvalidSignatureException if the signature is not valid.
     * 
     * @require !isVerified() : "The signature of this attribute value has not been verified.";
     * 
     * @ensure isVerified() : "The signature of this attribute value has been verified.";
     */
    @Pure
    @NonCommitting
    public abstract void verify() throws ExternalException;
    
    /**
     * Returns whether the signature of this attribute value has been verified.
     */
    @Pure
    public boolean isVerified() {
        return getSignature().isVerified();
    }
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    @Pure
    public static @Nonnull AttributeValue with(@Nonnull @Invariant(condition = "signature.getObject().getType().isAttributeType()", message = "The type of the packed value denotes an attribute.") Signature<Pack> signature) {
        if (signature instanceof HostSignature<?>) { return new CertifiedAttributeValueSubclass((HostSignature<Pack>) signature); }
        else { return new UncertifiedAttributeValueSubclass(signature); }
    }
    
}
