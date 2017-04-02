package net.digitalid.core.signature.attribute;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Invariant;

import net.digitalid.core.pack.Pack;
import net.digitalid.core.signature.Signature;

/**
 * This class facilitates the encoding and decoding of uncertified attribute values.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class UncertifiedAttributeValue extends AttributeValue {
    
    // TODO: Check somewhere that:
    // @invariant signature.isNotSigned() : "The signature is not signed.";
    
    // TODO: The signature needs to be created somewhere:
//        this.signature = SignatureWrapper.encodeWithoutSigning(AttributeValue.TYPE, SelfcontainedWrapper.encodeNonNullable(AttributeValue.CONTENT, content), null);
    
    /* -------------------------------------------------- Verification -------------------------------------------------- */
    
    @Pure
    @Override
    public void verify() {}
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    @Pure
    @Recover
    public static @Nonnull UncertifiedAttributeValue with(@Nonnull @Invariant(condition = "signature.getObject().getType().isAttributeType()", message = "The type of the packed value denotes an attribute.") Signature<Pack> signature) {
        return new UncertifiedAttributeValueSubclass(signature);
    }
    
}
