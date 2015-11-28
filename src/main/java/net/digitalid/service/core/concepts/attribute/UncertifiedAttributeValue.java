package net.digitalid.service.core.concepts.attribute;

import javax.annotation.Nonnull;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.SelfcontainedWrapper;
import net.digitalid.service.core.block.wrappers.SignatureWrapper;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This class facilitates the encoding and decoding of uncertified attribute values.
 */
@Immutable
public final class UncertifiedAttributeValue extends AttributeValue {
    
    /**
     * Stores the signature of this uncertified attribute value.
     * 
     * @invariant signature.isNotSigned() : "The signature is not signed.";
     */
    private final @Nonnull SignatureWrapper signature;
    
    /**
     * Creates an uncertified attribute value with the given content.
     * 
     * @param content the content of the uncertified attribute value.
     * 
     * @require content.getType().isAttributeType() : "The type of the content denotes an attribute.";
     * 
     * @ensure isVerified() : "The signature of this attribute value is verified.";
     */
    public UncertifiedAttributeValue(@Nonnull Block content) {
        super(content);
        
        this.signature = SignatureWrapper.encodeWithoutSigning(AttributeValue.TYPE, SelfcontainedWrapper.encodeNonNullable(AttributeValue.CONTENT, content), null);
    }
    
    /**
     * Creates an uncertified attribute value with the given content.
     * 
     * @param content the content of the uncertified attribute value.
     * 
     * @require content.getType().isAttributeType() : "The type of the content denotes an attribute.";
     * 
     * @ensure isVerified() : "The signature of this attribute value is verified.";
     */
//    public UncertifiedAttributeValue(@Nonnull Blockable content) {
//        this(content.toBlock());
//    }
    
    /**
     * Creates an uncertified attribute value with the given content and signature.
     * 
     * @param content the content of the uncertified attribute value.
     * @param signature the signature of the uncertified attribute value.
     * 
     * @require content.getType().isAttributeType() : "The type of the content denotes an attribute.";
     * @require signature.isNotSigned() : "The signature is not signed.";
     */
    UncertifiedAttributeValue(@Nonnull Block content, @Nonnull SignatureWrapper signature) {
        super(content);
        
        this.signature = signature;
    }
    
    
    @Pure
    @Override
    public @Nonnull SignatureWrapper getSignature() {
        return signature;
    }
    
    @Pure
    @Override
    @NonCommitting
    public void verify() throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        signature.verify();
    }
    
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Uncertified attribute value of type " + getContent().getType().getAddress();
    }
    
}
