package net.digitalid.service.core.attribute;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.database.SQLizable;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.exceptions.external.InvalidSignatureException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.service.core.wrappers.Blockable;
import net.digitalid.service.core.wrappers.ListWrapper;
import net.digitalid.service.core.wrappers.SelfcontainedWrapper;
import net.digitalid.service.core.wrappers.SignatureWrapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * This class facilitates the encoding and decoding of attribute values.
 * 
 * @see Attribute
 * @see CertifiedAttributeValue
 * @see UncertifiedAttributeValue
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public abstract class AttributeValue implements Blockable, SQLizable {
    
    /**
     * Stores the semantic type {@code content.attribute@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType CONTENT = SemanticType.map("content.attribute@core.digitalid.net").load(SelfcontainedWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code value.attribute@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("value.attribute@core.digitalid.net").load(SignatureWrapper.TYPE, CONTENT);
    
    /**
     * Stores the semantic type {@code list.attribute@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType LIST = SemanticType.map("list.attribute@core.digitalid.net").load(ListWrapper.TYPE, TYPE);
    
    
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
    @NonCommitting
    public static @Nonnull AttributeValue get(@Nonnull Block block, boolean verified) throws SQLException, IOException, PacketException, ExternalException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull SignatureWrapper signature = SignatureWrapper.decodeWithoutVerifying(block, verified, null);
        final @Nonnull Block content = new SelfcontainedWrapper(signature.getNonNullableElement()).getElement();
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
        return new Block(TYPE, getSignature().toBlock());
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
    public boolean matches(@Nonnull Attribute attribute) {
        return content.getType().equals(attribute.getType());
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
    public final @Nonnull AttributeValue checkMatches(@Nonnull Attribute attribute) throws InvalidEncodingException {
        if (!matches(attribute)) throw new InvalidEncodingException("This value does not match the given attribute.");
        return this;
    }
    
    /**
     * Checks that the content of this value matches the given type.
     * 
     * @param type the type which needs to be matched by the content.
     * 
     * @return this attribute value.
     * 
     * @throws InvalidEncodingException otherwise.
     * 
     * @ensure getContent().getType().equals(type) : "The content matches the given type.";
     */
    @Pure
    public final @Nonnull AttributeValue checkContentType(@Nonnull SemanticType type) throws InvalidEncodingException {
        assert type.isAttributeType() : "The type is an attribute type.";
        
        if (!content.getType().equals(type)) throw new InvalidEncodingException("The content of this value does not match the given type.");
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
    @NonCommitting
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
    @NonCommitting
    public static @Nonnull AttributeValue get(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        try {
            return AttributeValue.get(Block.getNotNull(TYPE, resultSet, columnIndex), true);
        } catch (@Nonnull IOException | PacketException | ExternalException exception) {
            throw new SQLException("The attribute value returned by the database is invalid.", exception);
        }
    }
    
    @Override
    @NonCommitting
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
    @NonCommitting
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
        return this.toBlock().equals(other.toBlock());
    }
    
    @Pure
    @Override
    public final int hashCode() {
        return getSignature().hashCode();
    }
    
}
