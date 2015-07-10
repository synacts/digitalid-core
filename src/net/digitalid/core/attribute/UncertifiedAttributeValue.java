package net.digitalid.core.attribute;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.interfaces.Blockable;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.SelfcontainedWrapper;
import net.digitalid.core.wrappers.SignatureWrapper;

/**
 * This class facilitates the encoding and decoding of uncertified attribute values.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
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
        
        this.signature = new SignatureWrapper(AttributeValue.TYPE, new SelfcontainedWrapper(AttributeValue.CONTENT, content), null);
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
    public UncertifiedAttributeValue(@Nonnull Blockable content) {
        this(content.toBlock());
    }
    
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
    public void verify() throws SQLException, IOException, PacketException, ExternalException {
        signature.verify();
    }
    
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Uncertified attribute value of type " + getContent().getType().getAddress();
    }
    
}
