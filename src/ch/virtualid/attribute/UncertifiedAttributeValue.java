package ch.virtualid.attribute;

import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.xdf.Block;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.SignatureWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This class facilitates the encoding and decoding of uncertified attribute values.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class UncertifiedAttributeValue extends AttributeValue implements Immutable, Blockable, SQLizable {
    
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
    public void verify() throws SQLException, IOException, PacketException, ExternalException {
        signature.verify();
    }
    
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Uncertified attribute value of type " + getContent().getType().getAddress();
    }
    
}
