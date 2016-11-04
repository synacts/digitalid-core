package net.digitalid.core.signature.attribute;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This class facilitates the encoding and decoding of uncertified attribute values.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
//@GenerateConverter // The generic signature cannot yet be converted.
public abstract class UncertifiedAttributeValue extends AttributeValue {
    
    // TODO: Check somewhere that:
    // @invariant signature.isNotSigned() : "The signature is not signed.";
    
    // TODO: The signature needs to be created somewhere:
//        this.signature = SignatureWrapper.encodeWithoutSigning(AttributeValue.TYPE, SelfcontainedWrapper.encodeNonNullable(AttributeValue.CONTENT, content), null);
    
    /* -------------------------------------------------- Verification -------------------------------------------------- */
    
    @Pure
    @Override
    public void verify() {}
    
}
