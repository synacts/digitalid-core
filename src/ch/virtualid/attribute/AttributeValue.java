package ch.virtualid.attribute;

import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.external.InvalidSignatureException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.xdf.Block;
import ch.xdf.BooleanWrapper;
import ch.xdf.ListWrapper;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.SignatureWrapper;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class facilitates the encoding and decoding of attribute values.
 * 
 * @see Attribute
 * @see CertifiedAttributeValue
 * @see UncertifiedAttributeValue
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class AttributeValue implements Immutable, Blockable, SQLizable {
    
    /**
     * Stores the semantic type {@code published.attribute@virtualid.ch}.
     */
    public static final @Nonnull SemanticType PUBLISHED = SemanticType.create("published.attribute@virtualid.ch").load(BooleanWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code content.attribute@virtualid.ch}.
     */
    public static final @Nonnull SemanticType CONTENT = SemanticType.create("content.attribute@virtualid.ch").load(SelfcontainedWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code value.attribute@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("value.attribute@virtualid.ch").load(SignatureWrapper.TYPE, CONTENT);
    
    /**
     * Stores the semantic type {@code list.attribute@virtualid.ch}.
     */
    public static final @Nonnull SemanticType LIST = SemanticType.create("list.attribute@virtualid.ch").load(ListWrapper.TYPE, TYPE);
    
    
    /**
     * Stores the content of this attribute value.
     * 
     * @invariant content.getType().isAttributeType() : "The type of the content denotes an attribute.";
     */
    private final @Nonnull Block content;
    
    /**
     * Creates an attribute value with the given content.
     * 
     * @param content the content of the attribute value.
     * 
     * @require content.getType().isAttributeType() : "The type of the content denotes an attribute.";
     */
    AttributeValue(@Nonnull Block content) {
        assert content.getType().isAttributeType() : "The type of the content denotes an attribute.";
        
        this.content = content;
    }
    
    /**
     * Decodes the given block without verifying the signature.
     * 
     * @param block the block which is to be wrapped and decoded.
     * @param verified whether the signature is already verified.
     * 
     * @return the attribute value with the content given by the block.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    @Pure
    public static @Nonnull AttributeValue get(@Nonnull Block block, boolean verified) throws SQLException, IOException, PacketException, ExternalException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull SignatureWrapper signature = SignatureWrapper.decodeWithoutVerifying(block, verified, null);
        final @Nonnull Block content = new SelfcontainedWrapper(signature.getElementNotNull()).getElement();
        content.getType().checkIsAttributeType();
        
        if (signature.isSigned()) return new CertifiedAttributeValue(content, signature);
        else return new UncertifiedAttributeValue(content, signature);
    }
    
    @Pure
    @Override
    public final @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public final @Nonnull Block toBlock() {
        return getSignature().toBlock();
    }
    
    
    /**
     * Returns the content of this attribute value.
     * 
     * @return the content of this attribute value.
     * 
     * @ensure return.getType().isAttributeType() : "The type of the content denotes an attribute.";
     */
    @Pure
    public final @Nonnull Block getContent() {
        return content;
    }
    
    /**
     * Returns whether this attribute value is certified.
     * 
     * @return whether this attribute value is certified.
     */
    @Pure
    public final boolean isCertified() {
        return this instanceof CertifiedAttributeValue;
    }
    
    
    /**
     * Returns whether this value matches the given attribute.
     * 
     * @param attribute the attribute which needs to be matched.
     * 
     * @return whether this value matches the given attribute.
     */
    @Pure
    public boolean match(@Nonnull Attribute attribute) {
        return content.getType().isAttributeFor(attribute.getEntity().getIdentity().getCategory());
    }
    
    /**
     * Checks that this value matches the given attribute.
     * 
     * @param attribute the attribute which needs to be matched.
     * 
     * @return this attribute value.
     * 
     * @throws InvalidEncodingException otherwise.
     */
    @Pure
    public @Nonnull AttributeValue checkMatch(@Nonnull Attribute attribute) throws InvalidEncodingException {
        if (!match(attribute)) throw new InvalidEncodingException("This value does not match the given attribute.");
        return this;
    }
    
    
    /**
     * Returns the signature of this attribute value.
     * 
     * @return the signature of this attribute value.
     */
    @Pure
    public abstract @Nonnull SignatureWrapper getSignature();
    
    /**
     * Verifies the signature of this attribute value.
     * 
     * @throws InvalidSignatureException if the signature is not valid.
     * 
     * @require isNotVerified() : "The signature of this attribute value is not verified.";
     * 
     * @ensure isVerified() : "The signature of this attribute value is verified.";
     */
    @Pure
    public abstract void verify() throws SQLException, IOException, PacketException, ExternalException;
    
    /**
     * Returns whether the signature of this attribute value is verified.
     * 
     * @return whether the signature of this attribute value is verified.
     */
    @Pure
    public final boolean isVerified() {
        return getSignature().isVerified();
    }
    
    /**
     * Returns whether the signature of this attribute value is not verified.
     * 
     * @return whether the signature of this attribute value is not verified.
     */
    @Pure
    public final boolean isNotVerified() {
        return getSignature().isNotVerified();
    }
    
    
    /**
     * Stores the data type used to store instances of this class in the database.
     */
    public static final @Nonnull String FORMAT = Block.FORMAT;
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    public static @Nonnull AttributeValue get(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        try {
            return AttributeValue.get(Block.get(AttributeValue.TYPE, resultSet, columnIndex), true);
        } catch (@Nonnull IOException | PacketException | ExternalException exception) {
            throw new SQLException("The attribute value returned by the database is invalid.", exception);
        }
    }
    
    @Override
    public final void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        toBlock().set(preparedStatement, parameterIndex);
    }
    
    /**
     * Sets the parameter at the given index of the prepared statement to the given attribute value.
     * 
     * @param attributeValue the attribute value to which the parameter at the given index is to be set.
     * @param preparedStatement the prepared statement whose parameter is to be set.
     * @param parameterIndex the index of the parameter to set.
     */
    public static void set(@Nullable AttributeValue attributeValue, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        Block.set(Block.toBlock(attributeValue), preparedStatement, parameterIndex);
    }
    
    
    /**
     * Returns this attribute value as a {@link CertifiedAttributeValue}.
     * 
     * @return this attribute value as a {@link CertifiedAttributeValue}.
     * 
     * @throws InvalidEncodingException if this attribute value is not an instance of {@link CertifiedAttributeValue}.
     */
    @Pure
    public final @Nonnull CertifiedAttributeValue toCertifiedAttributeValue() throws InvalidEncodingException {
        if (this instanceof CertifiedAttributeValue) return (CertifiedAttributeValue) this;
        throw new InvalidEncodingException("This attribute value cannot be cast to CertifiedAttributeValue.");
    }
    
    /**
     * Returns this attribute value as an {@link UncertifiedAttributeValue}.
     * 
     * @return this attribute value as an {@link UncertifiedAttributeValue}.
     * 
     * @throws InvalidEncodingException if this attribute value is not an instance of {@link UncertifiedAttributeValue}.
     */
    @Pure
    public final @Nonnull UncertifiedAttributeValue toUncertifiedAttributeValue() throws InvalidEncodingException {
        if (this instanceof UncertifiedAttributeValue) return (UncertifiedAttributeValue) this;
        throw new InvalidEncodingException("This attribute value cannot be cast to UncertifiedAttributeValue.");
    }
    
    
    @Pure
    @Override
    public final boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof AttributeValue)) return false;
        final @Nonnull AttributeValue other = (AttributeValue) object;
        return this.getSignature().equals(other.getSignature());
    }
    
    @Pure
    @Override
    public final int hashCode() {
        return getSignature().hashCode();
    }
    
}
